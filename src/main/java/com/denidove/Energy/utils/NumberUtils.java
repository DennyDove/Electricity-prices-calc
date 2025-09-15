package com.denidove.Energy.utils;

public class NumberUtils {

    // Метод для перевода числа 1, 2, 3... в формат 01, 02, 03...
    public static String format(int number) {
        if(number < 10) return "0" + number;
        else return String.valueOf(number);
    }
}
