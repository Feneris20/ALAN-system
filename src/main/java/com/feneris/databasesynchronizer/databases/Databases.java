package com.feneris.databasesynchronizer.databases;

import com.feneris.databasesynchronizer.controller.HibernateController;

public class Databases {
    private static Databases ourInstance = new Databases();

    public static Databases getInstance() {
        return ourInstance;
    }

    public static HibernateController hibernateController = new HibernateController();

    private Databases() {
    }
}
