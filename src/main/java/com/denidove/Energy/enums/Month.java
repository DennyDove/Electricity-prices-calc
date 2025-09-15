package com.denidove.Energy.enums;

import lombok.Getter;

@Getter

public enum Month {
    January("Январь", 1),
    February("Февраль", 2),
    March("Март", 3),
    April("Апрель", 4),
    May("Май", 5),
    June("Июнь", 6),
    July("Июль", 7),
    August("Август", 8),
    September("Сентябрь", 9),
    October("Октябрь", 10),
    November("Ноябрь", 11),
    December("Декабрь", 12);

    final String name;
    final int value;

    Month(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
