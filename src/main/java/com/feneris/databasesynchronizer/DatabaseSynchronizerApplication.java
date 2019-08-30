package com.feneris.databasesynchronizer;

import com.feneris.databasesynchronizer.services.EventService;
import com.feneris.databasesynchronizer.services.SynchronizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class DatabaseSynchronizerApplication implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSynchronizerApplication.class);

    @Autowired
    private SynchronizerService synchronizerServis;

    @Autowired
    private EventService eventService;

    @Value(value = "${database.checkInterval}")
    int databaseCheckInterval;

    public static void main(String[] args) {
        LOGGER.info("Databases synchronizer start");
        SpringApplication.run(DatabaseSynchronizerApplication.class, args);
        LOGGER.info("Databases synchronizer stop");
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {

        boolean eventServiceRun = args.containsOption("runEventService");
        File runFile = createRunFile();

        if (eventServiceRun) {
            eventService.start();
        }
        while (runFile.exists()) {
            Thread.sleep(2000);
            if (synchronizerServis.isStopped()) {
                synchronizerServis.start();
            }
        }
        if (eventServiceRun) {
            eventService.interrupt();
        }

    }

    private File createRunFile() throws IOException {
        File runFile = new File("run");
        if (!runFile.exists()) {
            runFile.createNewFile();
        }
        return runFile;
    }

}
