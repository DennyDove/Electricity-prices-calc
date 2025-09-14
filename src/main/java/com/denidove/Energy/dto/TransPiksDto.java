package com.denidove.Energy.dto;

import com.denidove.Energy.entities.TransPeaks;
import com.denidove.Energy.enums.Month;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;

@Getter
@Setter

@Service
@SessionScope
public class TransPiksDto {
    private int firstHourStart;
    private int firstHourEnd;
    private int secondHourStart;
    private int secondHourEnd;

    public TransPiksDto() {
    }

    public TransPiksDto(int firstHourStart, int firstHourEnd, int secondHourStart, int secondHourEnd) {
        this.firstHourStart = firstHourStart;
        this.firstHourEnd = firstHourEnd;
        this.secondHourStart = secondHourStart;
        this.secondHourEnd = secondHourEnd;
    }
}
