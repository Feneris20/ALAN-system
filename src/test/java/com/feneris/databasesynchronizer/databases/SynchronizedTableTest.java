package com.feneris.databasesynchronizer.databases;

import com.feneris.databasesynchronizer.databases.model.SynchronizedTable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SynchronizedTableTest {

    private SynchronizedTable synchronizedTable;

    @Before
    public void init() {
        synchronizedTable = new SynchronizedTable();
    }

    @Test
    public void createSynchronizedTable() {
        assertNotNull(synchronizedTable);
    }

    @Test
    public void setTextSynchronizedTable() {
        synchronizedTable.setText("TEXT");
        assertEquals("TEXT", synchronizedTable.getText());
    }

    @Test
    public void equalsSynchronizedTables() {
        synchronizedTable.setText("TEXT");
        SynchronizedTable synchronizedTable2 = new SynchronizedTable();
        synchronizedTable2.setText("TEXT");
        assertTrue(synchronizedTable.equals(synchronizedTable2));
    }

    @Test(expected = NullPointerException.class)
    public void setNullInSynchronizedTable() {
        synchronizedTable.setText(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOver2000CharsToSynchronizedTable() {
        StringBuilder testBuilder = new StringBuilder();
        for (int i = 0; i < 2001; i++) {
            testBuilder.append("x");
        }
        synchronizedTable.setText(testBuilder.toString());

    }
}
