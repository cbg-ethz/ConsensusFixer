package ch.ethz.bsse.cf.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SortMapByValue {

    public static boolean ASC = true;
    public static boolean DESC = false;

    public static Map<Integer, Double> sortByComparator(Map<Integer, Double> unsortMap, final boolean order) {

        List<Entry<Integer, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            @Override
            public int compare(Entry<Integer, Double> o1,
                    Entry<Integer, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Integer, Double> sortedMap = new LinkedHashMap<>();
        for (Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}