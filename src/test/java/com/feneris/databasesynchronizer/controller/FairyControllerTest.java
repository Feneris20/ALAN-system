package com.feneris.databasesynchronizer.controller;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FairyControllerTest {
    private FairyController fairyController;

    @Before
    public void init() {
        fairyController = new FairyController("yyyy-MM-dd");
    }

    @Test
    public void createFairyController() {
        assertNotNull(fairyController);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createFairyControllerWithIllegalDateFormat() {
        fairyController = new FairyController("dsafds");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDotForSeperator() {
        fairyController.setSeperator(".");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setIllegalValueForRecordPattern() {
        fairyController.setRecordPattern("asdsaads");
    }

    @Test
    public void setCorrectValueForRecordPattern() {
        fairyController.setRecordPattern("person.firstName;person.lastName");
    }

    @Test
    public void setCorrectValueWithParameterForRecordPattern() {
        fairyController.setRecordPattern("date.randomPastDate(#P 2015)");
    }

    @Test
    public void getResult() throws Exception {
        fairyController.setRecordPattern("person.firstName;person.lastName");
        fairyController.setCount(10);
        List<String> list = fairyController.getResult();
        assertNotNull(list);
        assertEquals(10, list.size());
    }

}
