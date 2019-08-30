package com.feneris.databasesynchronizer.databases;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class DatabasesTest {

    @Test
    public void checkNotNullDatabases() {
        assertNotNull(Databases.getInstance());
    }

    @Test
    public void checkNotNullHibernateController() {
        assertNotNull(Databases.hibernateController);
    }
}
