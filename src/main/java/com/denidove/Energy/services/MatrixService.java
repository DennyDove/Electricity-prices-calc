package com.denidove.Energy.services;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.utils.CalendarOrder;
import com.denidove.Energy.utils.NIO;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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

    // Заполнение часовых объемов в матричную форму
    public void inputConsumptionValues() {
        plant = excelDocument.getPlant();
        var path = String.format("/templates/%s prices_aux.xlsx", plant);
        xlsx = openXlsxAsStream(path);

        var hourVolumes = documentDto.getHourVolumes();
        int k = 0;
        for(int j = 0; j < dayCalendar.length; j++) {
            for(int i = 0; i < 24; i++) {
                xlsx.getSheetAt(0).getRow(2 + j).getCell(1 + i).setCellValue(hourVolumes[i + k]);
            }
            k+=24;
        }
    }

    // Заполнение итоговой суммы по электроэнергии за месяц в отчетный файл
    public void inputElecPrice() {
        var elecPriceMonth_3pc = documentDto.getElecPriceMonth_3pc();
        var elecPriceMonth_4pc = documentDto.getElecPriceMonth_4pc();
        xlsx.getSheetAt(0).getRow(6).getCell(31).setCellValue(elecPriceMonth_4pc);
        xlsx.getSheetAt(0).getRow(28).getCell(31).setCellValue(elecPriceMonth_3pc);
    // Заполнение итоговой суммы по электроэнергии (со скидкой) за месяц в отчетный файл:
        var elecPriceMonth_3pc_discount = documentDto.getElecPriceMonth_3pc_discount();
        var elecPriceMonth_4pc_discount = documentDto.getElecPriceMonth_4pc_discount();
        xlsx.getSheetAt(0).getRow(6).getCell(34).setCellValue(elecPriceMonth_4pc_discount);
        xlsx.getSheetAt(0).getRow(28).getCell(34).setCellValue(elecPriceMonth_3pc_discount);
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

    // Расчет мощности оптового рынка
    public void inputCapacityOptVol(int period) {
        // Обнуление переменных, иначе некорректно считается за период нескольких месяцев
        Arrays.fill(workingDayIndex, 0);
        workingDays = 0;

        var hourVolumes = documentDto.getHourVolumes();
        double[] pikHours = new double[31];
        double[] dayPik = new double[31];
        var path = String.format("/%s/downloads/pikHour/20250%s_%s_peaks.xls", plant, period, plant);
        xls = open(path);
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
        int k = 0; // Переменная k производит увеличение индекса массива workingDayIndex, для
        int j = 0; // j это коэффициент, который увеличивает индексы массива hourVolumes на 24,
                   // то есть происходит переход на "новую строку" в массиве.
        for(int i = 0; i < dayCalendar.length; i++) {
            // В каждой итерации i сверяется с индксом рабочего дня (то есть с календарными числами рабочих дней) за вычетом единицы (для приведения соответствия к нумерации массива)
            if(i == workingDayIndex[k]-1) {
                // Переменная j производит переход на "новую строку" в массиве hourVolumes.
                dayPik[i] = hourVolumes[j + (int)pikHours[i] - 1];
                // Внесение часов пиковой нагрузки в отчетный файл
                xlsx.getSheetAt(0).getRow(2 + i).getCell(28).setCellValue(dayPik[i]);
                k++;
            } else dayPik[i] = 0;
            System.out.println(dayPik[i]);
            j += 24;
        }

        // Внесение часов пиковой нагрузки в отчетный файл
        capacityOptVol = Arrays.stream(dayPik)
                .filter(value -> value != 0)  // Нужно удалить нулевае значения из стрима, иначе некорректно считаются общие средние значения
                .sum() / workingDays;
        documentDto.setCapacityOptVol(capacityOptVol);
        xlsx.getSheetAt(0).getRow(33).getCell(28).setCellValue(capacityOptVol);
    }

    // toDo Расчет сетевой мощности (передача электроэнергии)
    //  Оптимизированный вариант кода, который составил с помощью AI:
    public void inputCapacityTransVol(int month) {
        var peaks = transPeakService.getPeaks();
        // сортировка нужна, чтобы упорядочить объекты в календарном порядке, иначе может получитья чехорда
        peaks.sort(new CalendarOrder());

        // Интервалы определения максимальной мощности. (- 1) переводной коэффициент для корректной нумерации массива
        //toDo написать пояснения по поводцу индексов inclusive | exclusive
        int intervalA_start = peaks.get(month - 1).getFirstHourStart() - (1);
        int intervalA_end = peaks.get(month - 1).getFirstHourEnd();
        int intervalB_start = peaks.get(month - 1).getSecondHourStart() - (1);
        int intervalB_end = peaks.get(month - 1).getSecondHourEnd();

        var hourVolumes = documentDto.getHourVolumes();
        double[] dayPik = new double[31];
        int days = dayCalendar.length; // предполагаем, что dayCalendar — массив длиной 31
        int hoursPerDay = 24;

        // В цикле вычисляются максимальные пики сетевой мощности
        for (int i = 0; i < days; i++) {
            int baseIndex = i * hoursPerDay; // переход на новый диапазон (календарный день) в массие

            // проверяем, относится ли день к рабочим
            int currentIndex = i; // ← локальная переменная, effectively final
            boolean isWorkingDay = Arrays.stream(workingDayIndex).anyMatch(day -> day - 1 == currentIndex);

            if (isWorkingDay) {
                double maxA = maxInRange(hourVolumes, baseIndex + intervalA_start, baseIndex + intervalA_end);
                double maxB = (intervalB_start == -1 && intervalB_end == 0)
                        ? 0
                        : maxInRange(hourVolumes, baseIndex + intervalB_start, baseIndex + intervalB_end);

                dayPik[i] = Math.max(maxA, maxB);

                xlsx.getSheetAt(0).getRow(2 + i).getCell(27).setCellValue(dayPik[i]);
            } else {
                dayPik[i] = 0;
            }

            System.out.println(dayPik[i]);
        }

        capacityTransVol = Arrays.stream(dayPik)
                .filter(value -> value != 0)  // Нужно удалить нулевае значения из стрима, иначе некорректно считаются общие средние значения
                .sum() / workingDays;
        documentDto.setCapacityTransVol(capacityTransVol);
        xlsx.getSheetAt(0).getRow(33).getCell(27).setCellValue(capacityTransVol);
    }

    // ---- Вспомогательная функция для метода выше - inputCapacityTransVol(int month)----
    private static double maxInRange(double[] arr, int start, int end) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = start; i < end && i < arr.length; i++) {
            if (arr[i] > max) max = arr[i];
        }
        return max;
    }


    // toDo Расчет сетевой мощности (передача электроэнергии)
    //  Старый вариант кода, который написал сам:
    /*
    public void inputCapacityTransVol(int month) {
        var peaks = transPeakService.getPeaks();
        // сортировка нужна, чтобы упорядочить объекты в календарном порядке, иначе может получитья чехорда
        peaks.sort(new CalendarOrder());

        // Интервалы определения максимальной мощности. (- 1) переводной коэффициент для корректной нумерации массива
        //toDo написать пояснения по поводцу индексов inclusive | exclusive
        int intervalA_start = peaks.get(month - 1).getFirstHourStart() - (1);
        int intervalA_end = peaks.get(month - 1).getFirstHourEnd();
        int intervalB_start = peaks.get(month - 1).getSecondHourStart() - (1);
        int intervalB_end = peaks.get(month - 1).getSecondHourEnd();

        var hourVolumes = documentDto.getHourVolumes();
        double[] dayPik = new double[31];

        int k = 0; // Переменная k производит увеличение индекса массива workingDayIndex, для
        int j = 0; // j это коэффициент, который увеличивает индексы массива hourVolumes на 24,
                   // то есть происходит переход на "новую строку" в массиве.
        // В цикле вычисляются максимальные пики сетевой мощности
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
    }*/

    //  Выгрузка отчетного файла на жесткий диск
    public void save(int period) {
        // Определяем папку, в которой лежит JAR-файл
        File jarDir = NIO.getAppDirectory();
        var fileName = String.format("/%s_%s_matrixReport.xlsx", period, plant);
        File path = new File(jarDir, fileName);

        // Метод заставляет пересчитаться формулы при следующем открытии файла
        XSSFFormulaEvaluator.evaluateAllFormulaCells(xlsx);

        try (FileOutputStream output = new FileOutputStream(path)) { // try-with-resources
            xlsx.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
