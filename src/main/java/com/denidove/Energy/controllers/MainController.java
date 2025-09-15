package com.denidove.Energy.controllers;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.dto.TransPiksDto;
import com.denidove.Energy.enums.Month;
import com.denidove.Energy.services.ElecService;
import com.denidove.Energy.services.ExcelDocument;
import com.denidove.Energy.services.MatrixService;
import com.denidove.Energy.services.TransPeakService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;


import java.util.List;

@Controller
public class MainController {

    @Autowired
    private TransPiksDto transPiksDto;
    @Autowired
    private DocumentDto documentDto;
    @Autowired
    private ExcelDocument excelDocument;
    @Autowired
    private ElecService elecService;
    @Autowired
    private MatrixService matrixService;
    @Autowired
    private TransPeakService transPeakService;

    private List months = List.of(Month.values());
    private List years = List.of(2023, 2024, 2025, 2026, 2027);

    @GetMapping("/")
    public String index(Model model) {
        documentDto.setDiscount(excelDocument.getDiscount());

        model.addAttribute("documentDto", documentDto);
        model.addAttribute("months", months);
        model.addAttribute("years", years);
        return "index.html";
    }

    @GetMapping("/update-menu")
    public String update_menu(Model model, @ModelAttribute("documentDto") DocumentDto documentDto) {

        model.addAttribute("documentDto", documentDto);
        model.addAttribute("months", months);
        model.addAttribute("years", years);
        return "update_menu.html";
    }

    @GetMapping("/update")
    public String update(Model model, @ModelAttribute("documentDto") DocumentDto documentDto) {
        var plant = documentDto.getPlant();
        var startPeriod = documentDto.getStartPeriod().getValue();
        var endPeriod = documentDto.getEndPeriod().getValue();
        excelDocument.setPlant(plant);
        excelDocument.setYear(documentDto.getYear());
        excelDocument.downloadPikHour(startPeriod, endPeriod);
        excelDocument.downloadElecPrices(startPeriod, endPeriod);

        var message = true;

        model.addAttribute("documentDto", documentDto);
        model.addAttribute("months", months);
        model.addAttribute("years", years);
        model.addAttribute("message", message);
        return "update_confirm.html";
    }

    @GetMapping("/reports")
    // Ниже указываем для @ModelAttribute("documentDto") FormDto, а не DocumentDto, для того, чтобы можно было записать в бин DocumentDto значение переменной plant.
    // Иначе значение plant полученное из html формы записывается во временный объект DocumentDto, а не в основной бин DocumentDto, имеющий видимость @SessionScope
    public String reports(Model model, @ModelAttribute("documentDto") DocumentDto documentDto) {
        var plant = documentDto.getPlant();
        excelDocument.setPlant(plant);
        excelDocument.setYear(documentDto.getYear());
        excelDocument.setDiscount(documentDto.getDiscount());

        for(int month = documentDto.getStartPeriod().getValue(); month < documentDto.getEndPeriod().getValue() + 1; month++) {
            elecService.inputConsumptionValues(month);
            elecService.inputElecPrices(month);
            matrixService.inputConsumptionValues();
            matrixService.inputElecPrice();
            matrixService.inputServiceRegimeFee();
            matrixService.inputPowerOptPrice();
            matrixService.inputPowerTransPrice();
            matrixService.inputCapacityOptVol(month);
            matrixService.inputCapacityTransVol(month);
            matrixService.save(month);
        }

        model.addAttribute("months", months);
        model.addAttribute("years", years);
        model.addAttribute("documentDto", documentDto);
        return "index.html";
    }
}
