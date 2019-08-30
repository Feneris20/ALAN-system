package com.feneris.databasesynchronizer.services;

import com.feneris.databasesynchronizer.databases.Databases;
import com.feneris.databasesynchronizer.controller.FairyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EventService implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private FairyController fairyController = new FairyController("yyyy-MM-dd");

    private Thread worker;
    private AtomicBoolean running = new AtomicBoolean(false);
    private int interval;

    @Value(value = "${event.service.maxRecordsInsert}")
    int maxRecords;
    @Value(value = "${event.service.maxRecordsDelete}")
    int minRecords;

    @Value(value = "${event.service.maxInterval}")
    int maxInterval;
    @Value(value = "${event.service.minInterval}")
    int minInteval;

    public EventService() throws IOException {
        fairyController.setSeperator(" ");
        fairyController.setRecordPattern("person.firstName person.lastName");
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


            interval = (new Random().nextInt((maxInterval - minInteval) + 1) + minInteval) * 1000;
            LOGGER.warn("Next event for " + interval + " [ms]");
            try {
                int amount = new Random().nextInt((maxRecords - minRecords) + 1) + minRecords;
                if (amount >= 0) {
                    fairyController.setCount(amount);
                    Databases.hibernateController.insertRecordsToSource(fairyController.getResult());
                } else {
                    Databases.hibernateController.deleteRecordsFromSource(Math.abs(amount));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
