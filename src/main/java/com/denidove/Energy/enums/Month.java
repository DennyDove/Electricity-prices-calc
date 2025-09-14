package com.denidove.Energy.enums;

import lombok.Getter;

@Getter

public enum Month {
    January("Январь", 1),
    February("Февраль", 2),
    March("Март", 3),
    April("Апрель", 4),
    May("Май", 5);

    final String name;
    final int value;

    Month(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
