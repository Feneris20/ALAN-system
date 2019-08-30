package com.feneris.databasesynchronizer.controller;

import com.feneris.databasesynchronizer.databases.model.SynchronizedTable;

import java.util.*;

public class SynchronizeController {

    public List<SynchronizedTable> getSynchronizedTableList(List<SynchronizedTable> source, List<SynchronizedTable> target) {
        HashMap<SynchronizedTable, Integer> diffrences = getDifferencesMap(convertToMap(source), convertToMap(target));
        List<SynchronizedTable> synchronizedTableList = target;

        for (SynchronizedTable stOut : diffrences.keySet()) {
            int counter = diffrences.get(stOut);
            if (counter > 0) {
                for (int i = 0; i < counter; i++) {
                    synchronizedTableList.add(stOut);
                }
            } else if (counter < 0) {
                int actual = 0;
                for (int i = 0; i < synchronizedTableList.size(); i++) {
                    if (stOut.equals(synchronizedTableList.get(i))) {
                        synchronizedTableList.remove(i);
                        actual--;
                    }
                    if (actual == counter) {
                        break;
                    }
                }
            }
        }
        return synchronizedTableList;
    }

    public HashMap<SynchronizedTable, Integer> convertToMap(List<SynchronizedTable> list) {
        HashMap<SynchronizedTable, Integer> toReturn = new HashMap<>();
        for (SynchronizedTable st : list) {
            if (toReturn.containsKey(st)) {
                toReturn.put(st, toReturn.get(st) + 1);
            } else {
                toReturn.put(st, 1);
            }
        }
        return toReturn;
    }

    public HashMap<SynchronizedTable, Integer> getDifferencesMap(HashMap<SynchronizedTable, Integer> sourceMap, HashMap<SynchronizedTable, Integer> targetMap) {
        HashMap<SynchronizedTable, Integer> toReturn = new HashMap<>();
        for (SynchronizedTable st : sourceMap.keySet()) {
            if (targetMap.containsKey(st)) {
                toReturn.put(st, sourceMap.get(st) - targetMap.get(st));
            } else {
                toReturn.put(st, sourceMap.get(st));
            }
        }
        for (SynchronizedTable st : targetMap.keySet()) {
            if (!toReturn.containsKey(st)) {
                toReturn.put(st, -targetMap.get(st));
            }
        }

        return toReturn;
    }

    public boolean listEquals(List<SynchronizedTable> firstList, List<SynchronizedTable> secoundList) {
        Collections.sort(firstList);
        Collections.sort(secoundList);
        return firstList.equals(secoundList);
    }
}
