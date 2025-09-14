package com.denidove.Energy.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "tariff_components")
public class TariffComponents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer year;
    private Double firstHalf_Klin_tsprt;
    private Double firstHalf_Klin_sp;
    private Double firstHalf_Klin_infra;
    private Double secondHalf_Klin_tsprt;
    private Double secondHalf_Klin_sp;
    private Double secondHalf_Klin_infra;
    private Double firstHalf_Bor_tsprt;
    private Double firstHalf_Bor_sp;
    private Double firstHalf_Bor_infra;
    private Double secondHalf_Bor_tsprt;
    private Double secondHalf_Bor_sp;
    private Double secondHalf_Bor_infra;

    public TariffComponents(Integer year, Double firstHalf_Klin_tsprt, Double firstHalf_Klin_sp,Double firstHalf_Klin_infra,
                            Double secondHalf_Klin_tsprt, Double secondHalf_Klin_sp, Double secondHalf_Klin_infra,
                            Double firstHalf_Bor_tsprt, Double firstHalf_Bor_sp, Double firstHalf_Bor_infra,
                            Double secondHalf_Bor_tsprt, Double secondHalf_Bor_sp, Double secondHalf_Bor_infra) {
        this.year = year;
        this.firstHalf_Klin_tsprt = firstHalf_Klin_tsprt;
        this.firstHalf_Klin_sp = firstHalf_Klin_sp;
        this.firstHalf_Klin_infra = firstHalf_Klin_infra;
        this.secondHalf_Klin_tsprt = secondHalf_Klin_tsprt;
        this.secondHalf_Klin_sp = secondHalf_Klin_sp;
        this.secondHalf_Klin_infra = secondHalf_Klin_infra;
        this.firstHalf_Bor_tsprt = firstHalf_Bor_tsprt;
        this.firstHalf_Bor_sp = firstHalf_Bor_sp;
        this.firstHalf_Bor_infra = firstHalf_Bor_infra;
        this.secondHalf_Bor_tsprt = secondHalf_Bor_tsprt;
        this.secondHalf_Bor_sp = secondHalf_Bor_sp;
        this.secondHalf_Bor_infra = secondHalf_Bor_infra;
    }
}
