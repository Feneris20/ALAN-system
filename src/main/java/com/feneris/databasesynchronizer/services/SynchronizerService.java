package com.feneris.databasesynchronizer.services;

import com.feneris.databasesynchronizer.databases.Databases;
import com.feneris.databasesynchronizer.controller.SynchronizeController;
import com.feneris.databasesynchronizer.databases.model.SynchronizedTable;

import org.hibernate.TransactionException;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SynchronizerService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizerService.class);

    private Thread worker;
    private AtomicBoolean running = new AtomicBoolean(false);
    private int interval;

    public long runSynchronize(List<SynchronizedTable> source, List<SynchronizedTable> target) throws LockAcquisitionException, TransactionException {
        Date start = new Date();
        SynchronizeController synchronizeController = new SynchronizeController();
        HashMap<SynchronizedTable, Integer> sourceMap = synchronizeController.convertToMap(source);
        HashMap<SynchronizedTable, Integer> targetMap = synchronizeController.convertToMap(target);
        Databases.hibernateController.synchronizeTables(
                synchronizeController.getDifferencesMap(
                        sourceMap,
                        targetMap
                ),
                targetMap
        );
        Date end = new Date();
        return (end.getTime() - start.getTime());
    }


    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    public void interrupt() {
        running.set(false);
        worker.interrupt();
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isStopped() {
        return !running.get();
    }


    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                LOGGER.info("Run synchronize");
                List source = Databases.hibernateController.getAllSynchronizedTableFromSource();
                List target = Databases.hibernateController.getAllSynchronizedTableFromTarget();
                if (source != null && target != null) {
                    long time = runSynchronize(source, target);
                    LOGGER.info("Time of synchronization " + time + " [ms]");
                } else {
                    LOGGER.warn("Synchronization skipped");
                }

            } finally {
                running.set(false);
                LOGGER.info("End synchronize");
            }

        }
    }
}
