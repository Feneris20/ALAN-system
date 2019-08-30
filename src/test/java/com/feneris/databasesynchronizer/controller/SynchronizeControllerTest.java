package com.feneris.databasesynchronizer.controller;

import com.feneris.databasesynchronizer.databases.model.SynchronizedTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SynchronizeControllerTest {

    private SynchronizeController synchronizeController;
    private LinkedList<SynchronizedTable> sourceList;
    private LinkedList<SynchronizedTable> targetList;

    @Before
    public void init() {
        synchronizeController = new SynchronizeController();
        sourceList = new LinkedList<>();
        targetList = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            sourceList.add(new SynchronizedTable());
            targetList.add(new SynchronizedTable());
        }

        for (int j = 0; j < sourceList.size(); j++) {
            sourceList.get(j).setText("TEXT" + j);
            targetList.get(j).setText("TEXT" + (2 * j));
        }
    }

    @Test
    public void checkConvertToMap() {
        HashMap<SynchronizedTable, Integer> sourceMap = synchronizeController.convertToMap(sourceList);
        int sourceCounter = 0;
        for (SynchronizedTable st : sourceMap.keySet()) {
            sourceCounter += sourceMap.get(st);
        }

        HashMap<SynchronizedTable, Integer> targetMap = synchronizeController.convertToMap(targetList);
        int targetCounter = 0;
        for (SynchronizedTable st : targetMap.keySet()) {
            targetCounter += targetMap.get(st);
        }

        assertEquals(sourceCounter, sourceList.size());
        assertEquals(targetCounter, targetList.size());
    }

    @Test
    public void checkDifrences() {
        HashMap<SynchronizedTable, Integer> diffrences = synchronizeController.getDifferencesMap(synchronizeController.convertToMap(sourceList), synchronizeController.convertToMap(targetList));

        for (SynchronizedTable st : diffrences.keySet()) {
            int countSource = 0;
            for (SynchronizedTable ssT : sourceList) {
                if (st.equals(ssT)) {
                    countSource++;
                }
            }
            int countTarget = 0;
            for (SynchronizedTable tsT : targetList) {
                if (st.equals(tsT)) {
                    countTarget++;
                }
            }
            assertEquals(countSource - countTarget, diffrences.get(st).intValue());
        }

        for (SynchronizedTable st1 : sourceList) {
            if (!diffrences.containsKey(st1)) {
                Assert.fail();
            }
        }

        for (SynchronizedTable st2 : targetList) {
            if (!diffrences.containsKey(st2)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void checkSynchronizedList() {
        List<SynchronizedTable> synchronizedList = synchronizeController.getSynchronizedTableList(sourceList, targetList);
        assertTrue(synchronizeController.listEquals(sourceList, synchronizedList));
    }

    @Test
    public void checkSynchronizedListWithInsertingDoubles() {
        List<SynchronizedTable> doubles = new ArrayList<>();
        doubles.addAll(sourceList);
        doubles.addAll(sourceList);
        List<SynchronizedTable> synchronizedList = synchronizeController.getSynchronizedTableList(doubles, targetList);
        assertTrue(synchronizeController.listEquals(doubles, synchronizedList));
    }

    @Test
    public void checkSynchronizedListWithRemovingOneFromDoubles() {
        List<SynchronizedTable> doubles = new ArrayList<>();
        doubles.addAll(targetList);
        doubles.addAll(targetList);
        List<SynchronizedTable> synchronizedList = synchronizeController.getSynchronizedTableList(doubles, targetList);
        assertTrue(synchronizeController.listEquals(doubles, synchronizedList));
    }

    @Test
    public void checkListEqualsTrue() {
        assertTrue(synchronizeController.listEquals(sourceList, sourceList));
    }

    @Test
    public void checkListEqualsFalse() {
        assertFalse(synchronizeController.listEquals(sourceList, targetList));
    }

}
