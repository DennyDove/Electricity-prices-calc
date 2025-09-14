package com.denidove.Energy.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "trans_tariffs")
public class PowerTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer year;
    private Double firstHalf_Klin;
    private Double secondHalf_Klin;
    private Double firstHalf_Bor;
    private Double secondHalf_Bor;

    private Double firstHalf_Klin_1st;
    private Double secondHalf_Klin_1st;
    private Double firstHalf_Bor_1st;
    private Double secondHalf_Bor_1st;

    public PowerTariff(Integer year, Double firstHalf_Klin, Double secondHalf_Klin, Double firstHalf_Bor, Double secondHalf_Bor,
                       Double firstHalf_Klin_1st, Double secondHalf_Klin_1st, Double firstHalf_Bor_1st, Double secondHalf_Bor_1st) {
        this.year = year;
        this.firstHalf_Klin = firstHalf_Klin;
        this.secondHalf_Klin = secondHalf_Klin;
        this.firstHalf_Bor = firstHalf_Bor;
        this.secondHalf_Bor = secondHalf_Bor;
        this.firstHalf_Klin_1st = firstHalf_Klin_1st;
        this.secondHalf_Klin_1st = secondHalf_Klin_1st;
        this.firstHalf_Bor_1st = firstHalf_Bor_1st;
        this.secondHalf_Bor_1st = secondHalf_Bor_1st;
    }
}
