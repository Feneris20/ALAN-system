package com.feneris.databasesynchronizer.controller;

import com.feneris.databasesynchronizer.databases.model.SynchronizedTable;

import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HibernateControllerTest {

    private static HibernateController hibernateController;
    private static File sourceFile;
    private static File targetFile;
    private static File sourceFileBackup;
    private static File targetFileBackup;
    private static File sourceFileEmpty;
    private static File targetFileEmpty;


    private static List<String> generatedListX;
    private static List<String> generatedListY;

    private static final int X = 1000;
    private static final int Y = 100;

    @BeforeClass
    public static void doBackup() throws Exception {

        sourceFile = new File("database-source.db");
        targetFile = new File("database-target.db");

        sourceFileEmpty = new File(sourceFile.getAbsolutePath().replace("database-source.db", "empty_databases" + File.separator + "database-source.db"));
        targetFileEmpty = new File(sourceFile.getAbsolutePath().replace("database-target.db", "empty_databases" + File.separator + "database-target.db"));

        sourceFileBackup = new File("backup-database-source.db");
        targetFileBackup = new File("backup-database-target.db");

        copyFile(sourceFile, sourceFileBackup, StandardCopyOption.REPLACE_EXISTING);
        copyFile(targetFile, targetFileBackup, StandardCopyOption.REPLACE_EXISTING);

        copyFile(sourceFileEmpty, sourceFile, StandardCopyOption.REPLACE_EXISTING);
        copyFile(targetFileEmpty, targetFile, StandardCopyOption.REPLACE_EXISTING);

        FairyController fairyController = new FairyController("yyyy-MM-dd");
        fairyController.setSeperator(" ");
        fairyController.setRecordPattern("person.firstName person.lastName");
        fairyController.setCount(X);
        generatedListX = fairyController.getResult();
        fairyController.setCount(Y);
        generatedListY = fairyController.getResult();
        hibernateController = new HibernateController();
    }

    @AfterClass
    public static void revertBackup() throws IOException {
        hibernateController.closeConnections();
        copyFile(sourceFileBackup, sourceFile, StandardCopyOption.REPLACE_EXISTING);
        copyFile(targetFileBackup, targetFile, StandardCopyOption.REPLACE_EXISTING);
        sourceFileBackup.delete();
        targetFileBackup.delete();
    }

    private static void copyFile(File source, File destination, StandardCopyOption option) throws IOException {
        Path originalFilePath = Paths.get(source.getPath());
        Path movedFilePath = Paths.get(destination.getPath());
        Files.copy(originalFilePath, movedFilePath, option);
    }

    @Test
    public void synchronizationDeltaTestXTo0() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        hibernateController.insertRecordsToSource(generatedListX);


        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(diffrences, synchronizeController.convertToMap(target));

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationDeltaTestXToX() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        hibernateController.insertRecordsToSource(generatedListX);
        hibernateController.insertRecordsToTarget(generatedListX);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(diffrences, synchronizeController.convertToMap(target));

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationDeltaTestXToY() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        hibernateController.insertRecordsToSource(generatedListX);
        hibernateController.insertRecordsToTarget(generatedListY);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(diffrences, synchronizeController.convertToMap(target));

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationDeltaTestXXTo0() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        ArrayList<String> list2X = new ArrayList<>();
        list2X.addAll(generatedListX);
        list2X.addAll(generatedListX);
        hibernateController.insertRecordsToSource(list2X);


        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(diffrences, synchronizeController.convertToMap(target));

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationDeltaTestXXToY() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        ArrayList<String> list2X = new ArrayList<>();
        list2X.addAll(generatedListX);
        list2X.addAll(generatedListX);
        hibernateController.insertRecordsToSource(list2X);
        hibernateController.insertRecordsToTarget(generatedListY);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(diffrences, synchronizeController.convertToMap(target));

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationDeltaTestXXToX() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        ArrayList<String> list2X = new ArrayList<>();
        list2X.addAll(generatedListX);
        list2X.addAll(generatedListX);
        hibernateController.insertRecordsToSource(list2X);
        hibernateController.insertRecordsToTarget(generatedListX);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(diffrences, synchronizeController.convertToMap(target));

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }


    @Test
    public void synchronizationTruncateTestXTo0() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        hibernateController.insertRecordsToSource(generatedListX);


        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(source, target, diffrences);

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationTruncateTestXToX() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        hibernateController.insertRecordsToSource(generatedListX);
        hibernateController.insertRecordsToTarget(generatedListX);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(source, target, diffrences);

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationTruncateTestXToY() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        hibernateController.insertRecordsToSource(generatedListX);
        hibernateController.insertRecordsToTarget(generatedListY);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(source, target, diffrences);

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationTruncateTestXXTo0() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        ArrayList<String> list2X = new ArrayList<>();
        list2X.addAll(generatedListX);
        list2X.addAll(generatedListX);
        hibernateController.insertRecordsToSource(list2X);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(source, target, diffrences);

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationTruncateTestXXToY() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        ArrayList<String> list2X = new ArrayList<>();
        list2X.addAll(generatedListX);
        list2X.addAll(generatedListX);
        hibernateController.insertRecordsToSource(list2X);
        hibernateController.insertRecordsToTarget(generatedListY);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(source, target, diffrences);

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void synchronizationTruncateTestXXToX() {
        hibernateController.deleteAllRecordsFromTarget();
        hibernateController.deleteAllRecordsFromSource();
        ArrayList<String> list2X = new ArrayList<>();
        list2X.addAll(generatedListX);
        list2X.addAll(generatedListX);
        hibernateController.insertRecordsToSource(list2X);
        hibernateController.insertRecordsToTarget(generatedListX);

        List source = hibernateController.getAllSynchronizedTableFromSource();
        List target = hibernateController.getAllSynchronizedTableFromTarget();

        Date start = new Date();

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(source), synchronizeController.convertToMap(target));
        hibernateController.synchronizeTables(source, target, diffrences);

        Date end = new Date();

        source = hibernateController.getAllSynchronizedTableFromSource();
        target = hibernateController.getAllSynchronizedTableFromTarget();

        assertTrue(synchronizeController.listEquals(source, target));
        assertTrue("Time (" + (end.getTime() - start.getTime()) + ") should be less as 3000 ms ", (end.getTime() - start.getTime()) < 3000);
    }

    @Test
    public void getAllRecordsFromTarget() {
        assertNotNull(hibernateController.getAllSynchronizedTableFromTarget());
    }

    @Test
    public void getAllRecordsFromSource() {
        assertNotNull(hibernateController.getAllSynchronizedTableFromSource());
    }

    @Test
    public void insertRecordsToSource() {
        List before = hibernateController.getAllSynchronizedTableFromSource();
        hibernateController.insertRecordsToSource(generatedListX);
        List after = hibernateController.getAllSynchronizedTableFromSource();
        assertEquals(generatedListX.size(), after.size() - before.size());
    }

    @Test
    public void insertRecordsToTarget() {
        List before = hibernateController.getAllSynchronizedTableFromTarget();
        hibernateController.insertRecordsToTarget(generatedListX);
        List after = hibernateController.getAllSynchronizedTableFromTarget();
        assertEquals(generatedListX.size(), after.size() - before.size());
    }


    @Test
    public void deleteRecordsFromTargetBySet() {
        List<SynchronizedTable> before = hibernateController.getAllSynchronizedTableFromTarget();
        Set<String> toDelete = new HashSet<>();
        if (before.size() == 0) {
            hibernateController.insertRecordsToTarget(generatedListX);
            before = hibernateController.getAllSynchronizedTableFromTarget();
        }
        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> beforeMap = synchronizeController.convertToMap(before);

        for (SynchronizedTable st : beforeMap.keySet()) {
            if (beforeMap.get(st) == 1) {
                toDelete.add(st.getText());
            }
        }
        if (toDelete.size() > 0) {
            hibernateController.deleteRecordsFromTarget(toDelete);
        }

        List after = hibernateController.getAllSynchronizedTableFromTarget();
        assertEquals(after.size(), before.size() - toDelete.size());
    }

    @Test
    public void deleteRecordsFromTargetByMap() {
        List<SynchronizedTable> before = hibernateController.getAllSynchronizedTableFromTarget();
        HashMap<String, Integer> toDelete = new HashMap<String, Integer>();

        if (before.size() == 0) {
            hibernateController.insertRecordsToTarget(generatedListX);
            before = hibernateController.getAllSynchronizedTableFromTarget();
        }

        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> beforeMap = synchronizeController.convertToMap(before);

        int toDeleteCounter = 0;
        for (SynchronizedTable st : beforeMap.keySet()) {
            toDelete.put(st.getText(), 1);
            toDeleteCounter++;
        }
        if (toDelete.size() > 0) {
            hibernateController.deleteRecordsFromTarget(toDelete);
        }

        List after = hibernateController.getAllSynchronizedTableFromTarget();
        assertEquals(after.size(), before.size() - toDeleteCounter);
    }

    @Test
    public void deleteAllRecordsFromTarget() {
        hibernateController.deleteAllRecordsFromTarget();
        List after = hibernateController.getAllSynchronizedTableFromTarget();
        assertEquals(0, after.size());
    }

    @Test
    public void deleteAllRecordsFromSource() {
        hibernateController.deleteAllRecordsFromSource();
        List after = hibernateController.getAllSynchronizedTableFromSource();
        assertEquals(0, after.size());
    }

}