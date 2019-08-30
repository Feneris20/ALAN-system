package com.feneris.databasesynchronizer.controller;

import com.feneris.databasesynchronizer.databases.model.SynchronizedTable;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class HibernateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateController.class);

    private static AtomicBoolean sourceDatabaseUsing = new AtomicBoolean(false);
    private static AtomicBoolean targetDatabaseUsing = new AtomicBoolean(false);

    private static final SessionFactory sourceDatabase;
    private static final SessionFactory targetDatabase;

    static {

        try {
            Configuration configurationForSource = new Configuration();
            configurationForSource.configure("/hibernate-source.cfg.xml");
            sourceDatabase = configurationForSource.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
        try {
            Configuration configurationForTarget = new Configuration();
            configurationForTarget.configure("/hibernate-target.cfg.xml");
            targetDatabase = configurationForTarget.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void closeConnections() {
        sourceDatabase.close();
        targetDatabase.close();
    }

    public static Session getSourceDatabase() throws HibernateException {
        if (!sourceDatabaseUsing.get()) {
            sourceDatabaseUsing.set(true);
            return sourceDatabase.openSession();
        } else {
            return null;
        }
    }

    public static Session getTargetDatabase() throws HibernateException {
        if (!targetDatabaseUsing.get()) {
            targetDatabaseUsing.set(true);
            return targetDatabase.openSession();
        } else {
            return null;
        }
    }

    public void synchronizeTables(HashMap<SynchronizedTable, Integer> diffrences, HashMap<SynchronizedTable, Integer> targetMap) {

        List<String> insertList = new LinkedList<>();
        Set<String> deleteList = new HashSet<>();
        HashMap<String, Integer> deletingDoublesMap = new HashMap<String, Integer>();
        for (SynchronizedTable st : diffrences.keySet()) {
            Integer counter = diffrences.get(st);
            if (counter > 0) {
                for (int i = 0; i < counter; i++) {
                    insertList.add(st.getText());
                }
            } else if (targetMap.get(st) == 1 && counter != 0) {
                deleteList.add(st.getText());
            } else if (counter != 0) {
                deletingDoublesMap.put(st.getText(), Math.abs(counter));
            }
        }
        if (deleteList.size() > 0) {
            deleteRecordsFromTarget(deleteList);
        }
        if (insertList.size() > 0) {
            insertRecordsToTarget(insertList);
        }
        if (deletingDoublesMap.keySet().size() > 0) {
            deleteRecordsFromTarget(deletingDoublesMap);
        }
    }

    public void synchronizeTables(List<SynchronizedTable> source, List<SynchronizedTable> target, HashMap<SynchronizedTable, Integer> diffrences) {
        List<String> texts = source.stream().map(SynchronizedTable::getText).collect(Collectors.toList());

        boolean runFlag = false;
        for (SynchronizedTable st : diffrences.keySet()) {
            if (diffrences.get(st) != 0) {
                runFlag = true;
                break;
            }
        }

        if (runFlag) {
            if (target.size() > 0) {
                deleteAllRecordsFromTarget();
            }
            if (source.size() > 0) {
                insertRecordsToTarget(texts);
            }
        }
    }

    public void insertRecordsToSource(List<String> texts) {
        final Session session = getSourceDatabase();
        if (session != null) {
            LOGGER.info("Inserting to source database " + texts.size() + " records");
            try {
                Transaction transaction = session.beginTransaction();
                final Query query = session.createSQLQuery("insert into ds_synchronized_table(dst_text) values" +
                        getQueryValues(texts));
                query.executeUpdate();
                transaction.commit();
            } finally {
                sourceDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Source database locked");
        }

    }

    public void deleteRecordsFromSource(Integer count) {
        final Session session = getSourceDatabase();
        if (session != null) {
            LOGGER.info("Deleting from source database " + count + " records");
            try {
                Transaction transaction = session.beginTransaction();
                final Query query = session.createSQLQuery("delete from ds_synchronized_table " +
                        "where rowid in (select rowid from ds_synchronized_table LIMIT " + count + ")");
                query.executeUpdate();
                transaction.commit();
            } finally {
                sourceDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Source database locked");
        }
    }

    public List<SynchronizedTable> getAllSynchronizedTableFromSource() {
        final Session session = getSourceDatabase();
        List<SynchronizedTable> toReturn = new ArrayList<>();
        if (session != null) {
            try {
                final Query query = session.createNamedQuery("SynchronizedTable.findAll");
                for (Object o : query.list()) {
                    toReturn.add((SynchronizedTable) o);
                }
            } finally {
                sourceDatabaseUsing.set(false);
                session.close();
            }
        } else {
            toReturn = null;
            LOGGER.error("Source database locked");
        }
        return toReturn;
    }

    public List<SynchronizedTable> getAllSynchronizedTableFromTarget() {
        final Session session = getTargetDatabase();
        List<SynchronizedTable> toReturn = new ArrayList<>();
        if (session != null) {
            try {
                final Query query = session.createNamedQuery("SynchronizedTable.findAll");
                for (Object o : query.list()) {
                    toReturn.add((SynchronizedTable) o);
                }
            } finally {
                targetDatabaseUsing.set(false);
                session.close();

            }
        } else {
            toReturn = null;
            LOGGER.error("Target database locked");
        }
        return toReturn;
    }

    public void deleteRecordsFromTarget(HashMap<String, Integer> deletingDoublesMap) {
        final Session session = getTargetDatabase();
        if (session != null) {
            try {
                Transaction transaction = session.beginTransaction();
                for (String s : deletingDoublesMap.keySet()) {
                    LOGGER.info("Deleting from target database " + s +" "+ deletingDoublesMap.get(s)+" records ");
                    final Query query = session.createSQLQuery("delete from ds_synchronized_table " +
                            "where rowid in (select rowid from ds_synchronized_table where dst_text = '" + s + "' LIMIT " + deletingDoublesMap.get(s) + ")");
                    query.executeUpdate();
                }
                transaction.commit();
            } finally {
                targetDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Target database locked");
        }
    }

    public void deleteRecordsFromTarget(Set<String> texts) {
        final Session session = getTargetDatabase();
        if (session != null) {
            LOGGER.info("Deleting from target database " + texts.size() + " records");
            try {
                Transaction transaction = session.beginTransaction();
                final Query query = session.createSQLQuery("delete from ds_synchronized_table " +
                        "where dst_text in (" + getListValues(texts) + ")");
                query.executeUpdate();
                transaction.commit();
            } finally {
                targetDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Target database locked");
        }
    }

    public void deleteAllRecordsFromSource() {
        final Session session = getSourceDatabase();
        if (session != null) {
            LOGGER.info("Deleting all from source database");
            try {
                Transaction transaction = session.beginTransaction();
                final Query query = session.createSQLQuery("delete from ds_synchronized_table");
                query.executeUpdate();
                transaction.commit();
            } finally {
                sourceDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Source database locked");
        }
    }

    public void deleteAllRecordsFromTarget() {
        final Session session = getTargetDatabase();
        if (session != null) {
            LOGGER.info("Deleting all from target database");
            try {
                Transaction transaction = session.beginTransaction();
                final Query query = session.createSQLQuery("delete from ds_synchronized_table");
                query.executeUpdate();
                transaction.commit();
            } finally {
                targetDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Target database locked");
        }
    }

    public void insertRecordsToTarget(List<String> texts) {
        final Session session = getTargetDatabase();
        if (session != null) {
            LOGGER.info("Inserting to target database " + texts.size() + " records");
            try {
                Transaction transaction = session.beginTransaction();
                final Query query = session.createSQLQuery("insert into ds_synchronized_table(dst_text) values" +
                        getQueryValues(texts));
                query.executeUpdate();
                transaction.commit();
            } finally {
                targetDatabaseUsing.set(false);
                session.close();
            }
        } else {
            LOGGER.error("Target database locked");
        }
    }

    private String getQueryValues(List<String> values) {
        String toReturn = "";
        for (int i = 0; i < values.size(); i++) {
            toReturn += "('" + values.get(i) + "'),";
        }
        return toReturn.substring(0, toReturn.length() - 1);
    }

    private String getListValues(Set<String> values) {
        String toReturn = "";
        for (String value : values) {
            toReturn += "'" + value + "',";
        }
        return toReturn.substring(0, toReturn.length() - 1);
    }

}