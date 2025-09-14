package com.denidove.Energy.services;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.entities.TransPeaks;
import com.denidove.Energy.utils.CalendarOrder;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class MatrixService extends ExcelDocument {

    @Autowired
    // Несмотря на то, что данный класс наследуется от ExcelDocument, всё равно требуется подклюяать бин ExcelDocument,
    // чтобы корректно работать с его полями (записывать и получать значения). Иначе получим NPE
    ExcelDocument excelDocument;
    @Autowired
    private DocumentDto documentDto;
    @Autowired
    private TransPeakService transPeakService;

    public void inputConsumptionValues() {
        plant = excelDocument.getPlant();
        xlsx = openXlsx("c:/EnergyReports/templates/"+ plant +" prices_aux.xlsx");

        var hourVolumes = documentDto.getHourVolumes();

        int k = 0;
        for(int j = 0; j < dayCalendar.length; j++) {
            for(int i = 0; i < 24; i++) {
                xlsx.getSheetAt(0).getRow(2 + j).getCell(1 + i).setCellValue(hourVolumes[i + k]);
            }
            k+=24;
        }
    }

    // Заполнение итоговой суммы по электроэнергии (без скидки) за месяц в отчетный файл
    public void inputElecPrice() {
        var elecPriceMonth_3pc = documentDto.getElecPriceMonth_3pc();
        var elecPriceMonth_4pc = documentDto.getElecPriceMonth_4pc();
        xlsx.getSheetAt(0).getRow(6).getCell(31).setCellValue(elecPriceMonth_4pc);
        xlsx.getSheetAt(0).getRow(28).getCell(31).setCellValue(elecPriceMonth_3pc);
    }

    // Заполнение итоговой суммы по электроэнергии (со скидкой) за месяц в отчетный файл
    public void inputElecPrice_discount() {
        var elecPriceMonth_3pc = documentDto.getElecPriceMonth_3pc_discount();
        var elecPriceMonth_4pc = documentDto.getElecPriceMonth_4pc_discount();
        xlsx.getSheetAt(0).getRow(6).getCell(34).setCellValue(elecPriceMonth_4pc);
        xlsx.getSheetAt(0).getRow(28).getCell(34).setCellValue(elecPriceMonth_3pc);
    }

    // Заполнение цены сетевой мощности в отчетный файл
    public void inputServiceRegimeFee() {
        var serviceRegimeFee = documentDto.getServiceRegimeFee();
        xlsx.getSheetAt(0).getRow(7).getCell(38).setCellValue(serviceRegimeFee);
    }

    // Заполнение цены сетевой мощности в отчетный файл
    public void inputPowerOptPrice() {
        var powerOptPrice = documentDto.getPowerOptPrice();
        xlsx.getSheetAt(0).getRow(10).getCell(38).setCellValue(powerOptPrice);
    }

    // Заполнение цены сетевой мощности в отчетный файл
    public void inputPowerTransPrice() {
        var powerTransPrice = documentDto.getPowerTransPrice();
        xlsx.getSheetAt(0).getRow(13).getCell(38).setCellValue(powerTransPrice * 1000);
    }

    //toDo Разобрать алгоритм и провести оптимизацию кода
    // Расчет мощности оптового рынка
    public void inputCapacityOptVol(int period) {
        var hourVolumes = documentDto.getHourVolumes();
        double[] pikHours = new double[31];
        double[] dayPik = new double[31];
        xls = open("c:/EnergyReports/"+ plant +"/downloads/pikHour/20250"+ period +"_"+ plant +"_peaks.xls");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.LL.yyyy");

        // Парсинг данных часов пиковой нагрузки из xls файла
        try {
            int i = 0;
            while (Double.valueOf(xls.getSheetAt(0).getRow(8 + i).getCell(1).getNumericCellValue()) != null) {
                workingDayIndex[i] = LocalDate.parse(xls.getSheetAt(0).getRow(8 + i).getCell(0).getStringCellValue(), formatter).getDayOfMonth();
                pikHours[workingDayIndex[i]-1] = xls.getSheetAt(0).getRow(8 + i).getCell(1).getNumericCellValue();
                i++;
                workingDays = i;
            }
        } catch (NullPointerException npe) {}

        // Внесение часов пиковой нагрузки в отчетный файл
        for(int i = 0; i < dayCalendar.length; i++) {
            xlsx.getSheetAt(0).getRow(2 + i).getCell(29).setCellValue(pikHours[i]);
        }

        // в цикле возвращаются максимальные пики оптовой мощности
        int k = 0; //toDo комментарии для - чего эта переменная
        int j = 0; //toDo комментарии для - чего эта переменная
        for(int i = 0; i < dayCalendar.length; i++) {
            if(i == workingDayIndex[k]-1) {
                //toDo разобрать строчку ниже, написать объяснение:
                dayPik[i] = hourVolumes[j + (int)pikHours[i] -1];
                // Внесение часов пиковой нагрузки в отчетный файл
                xlsx.getSheetAt(0).getRow(2 + i).getCell(28).setCellValue(dayPik[i]);
                k++;
            } else dayPik[i] = 0;
            System.out.println(dayPik[i]);
            j += 24;
        }

        // Внесение часов пиковой нагрузки в отчетный файл
        capacityOptVol = Arrays.stream(dayPik).sum() / workingDays;
        documentDto.setCapacityOptVol(capacityOptVol);
        xlsx.getSheetAt(0).getRow(33).getCell(28).setCellValue(capacityOptVol);
    }

    //toDo Разобрать алгоритм и провести оптимизацию кода
    // Расчет сетевой мощности (передача электроэнергии)
    public void inputCapacityTransVol(int month) {

        var peaks = transPeakService.getPeaks();
        // сортировка нужна, чтобы упорядочить объекты в календарном порядке, иначе может получитья чехорда
        peaks.sort(new CalendarOrder());


        // Интервалы определения максимальной мощности. (- 1) переводной коэффициент для корректной нумерации массива
        //toDo написать пояснения по поводцу индексов inclusive | exclusive
        //toDo также сделать базу данных для занесения тарифов и всех ценовых параметров (таких как интервалы)
        int intervalA_start = peaks.get(month - 1).getFirstHourStart() - (1);
        int intervalA_end = peaks.get(month - 1).getFirstHourEnd();
        int intervalB_start = peaks.get(month - 1).getSecondHourStart() - (1);
        int intervalB_end = peaks.get(month - 1).getSecondHourEnd();
        String test = peaks.get(month - 1).getMonth();

        var hourVolumes = documentDto.getHourVolumes();
        double[] dayPik = new double[31];
        int j = 0; //toDo комментарии для - чего эта переменная
        int k = 0; //toDo комментарии для - чего эта переменная
        // в цикле вычисляются максимальные пики сетевой мощности
        for(int i = 0; i < dayCalendar.length; i++) {
            if(i == workingDayIndex[k]-1) {
                var maxValueA = Arrays.stream(hourVolumes, j + intervalA_start, j + intervalA_end).max().getAsDouble();
                var maxValueB = 0.0;
                if(intervalB_start == -1 && intervalB_end == 0) maxValueB = 0;
                else maxValueB = Arrays.stream(hourVolumes, j + intervalB_start, j + intervalB_end).max().getAsDouble();
                dayPik[i] = Math.max(maxValueA, maxValueB);
                // Внесение часов пиковой нагрузки в отчетный файл
                xlsx.getSheetAt(0).getRow(2 + i).getCell(27).setCellValue(dayPik[i]);
                k++;
            } else dayPik[i] = 0;
            System.out.println(dayPik[i]);
            j += 24;
        }

        capacityTransVol = Arrays.stream(dayPik).sum() / workingDays;
        documentDto.setCapacityTransVol(capacityTransVol);
        xlsx.getSheetAt(0).getRow(33).getCell(27).setCellValue(capacityTransVol);
    }

    //  Выгрузка отчетного файла на жесткий диск
    //toDo
    public void save(int i) {
        // Метод заставляет пересчитаться формулы при следующем открытии файла
        XSSFFormulaEvaluator.evaluateAllFormulaCells(xlsx);
        try (FileOutputStream output = new FileOutputStream("c:/EnergyReports/"+i+"_"+ plant +"_matrixReport.xlsx")) { // try-with-resources
            xlsx.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
