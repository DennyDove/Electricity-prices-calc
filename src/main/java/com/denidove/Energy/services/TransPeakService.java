package com.denidove.Energy.services;

import com.denidove.Energy.dto.TransPiksDto;
import com.denidove.Energy.entities.TariffComponents;
import com.denidove.Energy.entities.TransPeaks;
import com.denidove.Energy.entities.PowerTariff;
import com.denidove.Energy.repositories.ComponentsRepo;
import com.denidove.Energy.repositories.TransPeaksRepo;
import com.denidove.Energy.repositories.PowerTariffRepo;
import com.denidove.Energy.utils.CalendarOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@Service
public class TransPeakService {

    private final TransPeaksRepo transPeaksRepo;
    private final TransPiksDto transPiksDto;
    private final PowerTariffRepo powerTariffRepo;
    private final ComponentsRepo componentsRepo;

    public TransPeakService(TransPeaksRepo transPeaksRepo, PowerTariffRepo powerTariffRepo, TransPiksDto transPiksDto,
                            ComponentsRepo componentsRepo) {
        this.transPeaksRepo = transPeaksRepo;
        this.transPiksDto = transPiksDto;
        this.powerTariffRepo = powerTariffRepo;
        this.componentsRepo = componentsRepo;
    }

    public void loadPeaks(String[] month , int[] piks1HourStart, int[] piks1HourEnd,
                            int[] piks2HourStart, int[] piks2HourEnd) {

        List<TransPeaks> transPeaks = new ArrayList<>();

        for(int i = 0; i < 12; i++) {
            var p = new TransPeaks();
            p.setMonth(month[i]);
            p.setFirstHourStart(piks1HourStart[i]);
            p.setFirstHourEnd(piks1HourEnd[i]);
            p.setSecondHourStart(piks2HourStart[i]);
            p.setSecondHourEnd(piks2HourEnd[i]);
            transPeaks.add(p);
        }
        //transPiksDto.setTransPeaks(transPeaks);
        transPeaksRepo.saveAll(transPeaks);
    }

    public void updatePeaks(int[] piks1HourStart, int[] piks1HourEnd,
                          int[] piks2HourStart, int[] piks2HourEnd) {

        List<TransPeaks> transPeaks = transPeaksRepo.findAll();
        // Сортировка нужна, чтобы записи в БД заносились в корректном порядке
        transPeaks.sort(new CalendarOrder());

        int i = 0;
        for(TransPeaks p : transPeaks) {
            p.setFirstHourStart(piks1HourStart[i]);
            p.setFirstHourEnd(piks1HourEnd[i]);
            p.setSecondHourStart(piks2HourStart[i]);
            p.setSecondHourEnd(piks2HourEnd[i]);
            i++;
        }
        transPeaksRepo.saveAll(transPeaks);
    }

    public List<TransPeaks> getPeaks() {
        return transPeaksRepo.findAll();
    }

    public PowerTariff loadPowerTariff(Integer year, Double firstHalf_Klin, Double secondHalf_Klin, Double firstHalf_Bor, Double secondHalf_Bor,
                                       Double firstHalf_Klin_1st, Double secondHalf_Klin_1st, Double firstHalf_Bor_1st, Double secondHalf_Bor_1st) {
        var transTariff = new PowerTariff(year, firstHalf_Klin, secondHalf_Klin, firstHalf_Bor, secondHalf_Bor, firstHalf_Klin_1st, secondHalf_Klin_1st, firstHalf_Bor_1st, secondHalf_Bor_1st);
        return powerTariffRepo.save(transTariff);
    }

    public PowerTariff updatePowerTariff(Double firstHalf_Klin, Double secondHalf_Klin, Double firstHalf_Bor, Double secondHalf_Bor,
                                         Double firstHalf_Klin_1st, Double secondHalf_Klin_1st, Double firstHalf_Bor_1st, Double secondHalf_Bor_1st) {
        var transTariffList = powerTariffRepo.findAll();
        var transTariff = transTariffList.getLast();
        transTariff.setFirstHalf_Klin(firstHalf_Klin);
        transTariff.setSecondHalf_Klin(secondHalf_Klin);
        transTariff.setFirstHalf_Bor(firstHalf_Bor);
        transTariff.setSecondHalf_Bor(secondHalf_Bor);

        transTariff.setFirstHalf_Klin_1st(firstHalf_Klin_1st);
        transTariff.setSecondHalf_Klin_1st(secondHalf_Klin_1st);
        transTariff.setFirstHalf_Bor_1st(firstHalf_Bor_1st);
        transTariff.setSecondHalf_Bor_1st(secondHalf_Bor_1st);
        return powerTariffRepo.save(transTariff);
    }

    public List<PowerTariff> getTariffs() {
        return powerTariffRepo.findAll();
    }

    public TariffComponents loadTariffComponents(Integer year, Double firstHalf_Klin_tsprt, Double firstHalf_Klin_sp, Double firstHalf_Klin_infra,
                                                   Double secondHalf_Klin_tsprt, Double secondHalf_Klin_sp, Double secondHalf_Klin_infra,
                                                   Double firstHalf_Bor_tsprt, Double firstHalf_Bor_sp, Double firstHalf_Bor_infra,
                                                   Double secondHalf_Bor_tsprt, Double secondHalf_Bor_sp, Double secondHalf_Bor_infra) {
        var tariffComponents = new TariffComponents(year, firstHalf_Klin_tsprt, firstHalf_Klin_sp, firstHalf_Klin_infra,
                secondHalf_Klin_tsprt, secondHalf_Klin_sp, secondHalf_Klin_infra, firstHalf_Bor_tsprt, firstHalf_Bor_sp,
                firstHalf_Bor_infra, secondHalf_Bor_tsprt, secondHalf_Bor_sp, secondHalf_Bor_infra);
        return componentsRepo.save(tariffComponents);
    }

    public TariffComponents updateTariffComponents(Double firstHalf_Klin_tsprt, Double firstHalf_Klin_sp, Double firstHalf_Klin_infra,
                                              Double secondHalf_Klin_tsprt, Double secondHalf_Klin_sp, Double secondHalf_Klin_infra,
                                              Double firstHalf_Bor_tsprt, Double firstHalf_Bor_sp, Double firstHalf_Bor_infra,
                                              Double secondHalf_Bor_tsprt, Double secondHalf_Bor_sp, Double secondHalf_Bor_infra) {
        var componentsList = componentsRepo.findAll();
        var components = componentsList.getLast();
        components.setFirstHalf_Klin_tsprt(firstHalf_Klin_tsprt);
        components.setFirstHalf_Klin_sp(firstHalf_Klin_sp);
        components.setFirstHalf_Klin_infra(firstHalf_Klin_infra);
        components.setSecondHalf_Klin_tsprt(secondHalf_Klin_tsprt);
        components.setSecondHalf_Klin_sp(secondHalf_Klin_sp);
        components.setSecondHalf_Klin_infra(secondHalf_Klin_infra);
        components.setFirstHalf_Bor_tsprt(firstHalf_Bor_tsprt);
        components.setFirstHalf_Bor_sp(firstHalf_Bor_sp);
        components.setFirstHalf_Bor_infra(firstHalf_Bor_infra);
        components.setSecondHalf_Bor_tsprt(secondHalf_Bor_tsprt);
        components.setSecondHalf_Bor_sp(secondHalf_Bor_sp);
        components.setSecondHalf_Bor_infra(secondHalf_Bor_infra);
        return componentsRepo.save(components);
    }

    public List<TariffComponents> getComponents() {
        return componentsRepo.findAll();
    }
}
