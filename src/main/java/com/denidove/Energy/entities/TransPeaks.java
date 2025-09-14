package com.denidove.Energy.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "trans_peaks")
public class TransPeaks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer firstHourStart;
    private Integer firstHourEnd;
    private Integer secondHourStart;
    private Integer secondHourEnd;

    private String month;

    public TransPeaks(String month, Integer firstHourStart, Integer firstHourEnd, Integer secondHourStart, Integer secondHourEnd) {
        this.firstHourStart = firstHourStart;
        this.firstHourEnd = firstHourEnd;
        this.secondHourStart = secondHourStart;
        this.secondHourEnd = secondHourEnd;
        this.month = month;
    }
}
