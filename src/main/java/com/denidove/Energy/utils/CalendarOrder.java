package com.denidove.Energy.utils;

import com.denidove.Energy.entities.TransPeaks;

import java.util.Comparator;

public class CalendarOrder implements Comparator {
    public int compare(Object obj1, Object obj2) {
        TransPeaks transPeaks1 = (TransPeaks) obj1;
        TransPeaks transPeaks2 = (TransPeaks) obj2;
        Long s1 = transPeaks1.getId();
        Long s2 = transPeaks2.getId();
        return s1.compareTo(s2);
    }
}
