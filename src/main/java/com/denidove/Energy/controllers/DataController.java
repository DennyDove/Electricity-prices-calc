package com.denidove.Energy.controllers;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.dto.TransPiksDto;
import com.denidove.Energy.entities.PowerTariff;
import com.denidove.Energy.entities.TariffComponents;
import com.denidove.Energy.entities.TransPeaks;
import com.denidove.Energy.enums.Month;
import com.denidove.Energy.services.ElecService;
import com.denidove.Energy.services.ExcelDocument;
import com.denidove.Energy.services.MatrixService;
import com.denidove.Energy.services.TransPeakService;
import com.denidove.Energy.utils.CalendarOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.transform.Source;
import java.util.List;

@Controller
public class DataController {

    @Autowired
    private TransPiksDto transPiksDto;
    @Autowired
    private DocumentDto documentDto;
    @Autowired
    private ExcelDocument excelDocument;
    @Autowired
    private TransPeakService transPeakService;

    String[] month = new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябр", "Декабрь"};


    @GetMapping("/data")
    public String piksPageView(Model model) {

        var tariffs = transPeakService.getTariffs();
        if(transPeakService.getTariffs().isEmpty()) {
            tariffs.add(new PowerTariff(2025, 0.0, 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0));
        }

        var piksList = transPeakService.getPeaks();
        if(piksList.isEmpty()) {
            for(int i = 0; i < 12; i++) {
                var p = new TransPeaks(month[i], 0,0,0,0);
                piksList.add(p);
            }
        }

        //toDo - done: Нужна обработка try-catch, т.к. при первом запуске программы всегда выскакивает NPE
        try {
            piksList.sort(new CalendarOrder());
        } catch (NullPointerException e) {
            System.out.println("Словили NPE :)");
        }


        var components = transPeakService.getComponents();
        if(components.isEmpty())
            components.add((new TariffComponents(2025, 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 0.0,
                    0.0,0.0, 0.0, 0.0)));

        model.addAttribute("tariffs", tariffs.getLast());
        model.addAttribute("components", components.getLast());
        model.addAttribute("piksList", piksList);
        return "data.html";
    }

    @PostMapping("/put-piks")
    public String putPiks(Model model, @RequestParam(value = "firstHourStart", required = true) int[] firstHourStart,
                  @RequestParam(value = "firstHourEnd", required = true) int[] firstHourEnd,
                  @RequestParam(value = "secondHourStart", required = true) int[] secondHourStart,
                  @RequestParam(value = "secondHourEnd", required = true) int[] secondHourEnd,

                  // Двуставочные тарифы на передачу электроэнергии
                  @RequestParam(value = "firstHalf_Klin", required = true) double firstHalf_Klin,
                  @RequestParam(value = "secondHalf_Klin", required = true) double secondHalf_Klin,
                  @RequestParam(value = "firstHalf_Bor", required = true) double firstHalf_Bor,
                  @RequestParam(value = "secondHalf_Bor", required = true) double secondHalf_Bor,

                  // Одноставочные тарифы на передачу электроэнергии
                  @RequestParam(value = "firstHalf_Klin_1st", required = true) double firstHalf_Klin_1st,
                  @RequestParam(value = "secondHalf_Klin_1st", required = true) double secondHalf_Klin_1st,
                  @RequestParam(value = "firstHalf_Bor_1st", required = true) double firstHalf_Bor_1st,
                  @RequestParam(value = "secondHalf_Bor_1st", required = true) double secondHalf_Bor_1st,

                  // Прочие компоненты цены: сет.потери + сбыт. надбавка + инфраструктура
                  @RequestParam(value = "firstHalf_Klin_tsprt", required = true) double firstHalf_Klin_tsprt,
                  @RequestParam(value = "firstHalf_Klin_sp", required = true) double firstHalf_Klin_sp,
                  @RequestParam(value = "firstHalf_Klin_infra", required = true) double firstHalf_Klin_infra,
                  @RequestParam(value = "secondHalf_Klin_tsprt", required = true) double secondHalf_Klin_tsprt,
                  @RequestParam(value = "secondHalf_Klin_sp", required = true) double secondHalf_Klin_sp,
                  @RequestParam(value = "secondHalf_Klin_infra", required = true) double secondHalf_Klin_infra,
                  @RequestParam(value = "firstHalf_Bor_tsprt", required = true) double firstHalf_Bor_tsprt,
                  @RequestParam(value = "firstHalf_Bor_sp", required = true) double firstHalf_Bor_sp,
                  @RequestParam(value = "firstHalf_Bor_infra", required = true) double firstHalf_Bor_infra,
                  @RequestParam(value = "secondHalf_Bor_tsprt", required = true) double secondHalf_Bor_tsprt,
                  @RequestParam(value = "secondHalf_Bor_sp", required = true) double secondHalf_Bor_sp,
                  @RequestParam(value = "secondHalf_Bor_infra", required = true) double secondHalf_Bor_infra) {

        int[] piks1HourStart = firstHourStart;
        int[] piks1HourEnd = firstHourEnd;
        int[] piks2HourStart = secondHourStart;
        int[] piks2HourEnd = secondHourEnd;

        //toDo if (tariffs.isEmpty) ...
        var tariffs = transPeakService.getTariffs();
        var piksList = transPeakService.getPeaks();
        var components = transPeakService.getComponents();

        if(tariffs.isEmpty())
            transPeakService.loadPowerTariff(2025, firstHalf_Klin, secondHalf_Klin, firstHalf_Bor, secondHalf_Bor,
                    firstHalf_Klin_1st, secondHalf_Klin_1st, firstHalf_Bor_1st, secondHalf_Bor_1st);
        else
            transPeakService.updatePowerTariff(firstHalf_Klin, secondHalf_Klin, firstHalf_Bor, secondHalf_Bor,
                    firstHalf_Klin_1st, secondHalf_Klin_1st, firstHalf_Bor_1st, secondHalf_Bor_1st);

        if(piksList.isEmpty())
            transPeakService.loadPeaks(month, piks1HourStart, piks1HourEnd, piks2HourStart, piks2HourEnd);
        else
            transPeakService.updatePeaks(piks1HourStart, piks1HourEnd, piks2HourStart, piks2HourEnd);

        if(components.isEmpty())
            transPeakService.loadTariffComponents(2025, firstHalf_Klin_tsprt, firstHalf_Klin_sp, firstHalf_Klin_infra,
                    secondHalf_Klin_tsprt, secondHalf_Klin_sp, secondHalf_Klin_infra, firstHalf_Bor_tsprt, firstHalf_Bor_sp,
                    firstHalf_Bor_infra, secondHalf_Bor_tsprt, secondHalf_Bor_sp, secondHalf_Bor_infra);
        else
            transPeakService.updateTariffComponents(firstHalf_Klin_tsprt, firstHalf_Klin_sp, firstHalf_Klin_infra,
                    secondHalf_Klin_tsprt, secondHalf_Klin_sp, secondHalf_Klin_infra, firstHalf_Bor_tsprt, firstHalf_Bor_sp,
                    firstHalf_Bor_infra, secondHalf_Bor_tsprt, secondHalf_Bor_sp, secondHalf_Bor_infra);

        List<Month> months = List.of(Month.values());

        model.addAttribute("documentDto", documentDto);
        model.addAttribute("months", months);
        return "index.html";
    }
}
