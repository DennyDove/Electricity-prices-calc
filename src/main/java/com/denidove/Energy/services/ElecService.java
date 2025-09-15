package com.denidove.Energy.services;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.utils.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Getter
@Setter

@Service
public class ElecService extends ExcelDocument {

    @Autowired
    // Несмотря на то, что данный класс наследуется от ExcelDocument, всё равно требуется подклюяать бин ExcelDocument,
    // чтобы корректно работать с его полями (записывать и получать значения). Иначе получим NPE
    ExcelDocument excelDocument;
    @Autowired
    private DocumentDto documentDto;
    @Autowired
    private TransPeakService transPeakService;

    public double inputConsumptionValues(int period) {
        plant = excelDocument.getPlant();
        double monthSum = 0;
        xls = open("c:/EnergyReports/"+plant+"/downloads/Часовки ВН_0" + period + ".xls");
        if (period <= 6)
            xlsx = openXlsx("c:/EnergyReports/templates/1h_"+plant+"_elec.xlsx");
        else
            xlsx = openXlsx("c:/EnergyReports/templates/2h_"+plant+"_elec.xlsx");

        // Загружаем часовые объемы
        if(plant.equals("Klin")) {
            for (int i = 0; i < hourVolumes.length; i++) {
                hourVolumes[i] = xls.getSheetAt(0).getRow(0 + i).getCell(2).getNumericCellValue() / 1000;
                monthSum += hourVolumes[i];
                xlsx.getSheetAt(0).getRow(13 + i).getCell(9).setCellValue(hourVolumes[i]);
            }
        }

        int k = 0;
        if(plant.equals("Bor")) {
            for (int j = 0; j < dayCalendar.length; j++) {
                for (int i = 0; i < 24; i++) {
                    hourVolumes[k] = xls.getSheetAt(0).getRow( 6 + j).getCell(1 + i).getNumericCellValue();
                    monthSum += hourVolumes[k];
                    xlsx.getSheetAt(0).getRow(13 + k).getCell(9).setCellValue(hourVolumes[k]);
                    k++;
                }
            }
        }

        documentDto.setHourVolumes(hourVolumes);
        documentDto.setElecVolumeMonth(monthSum);
        return monthSum;
    }

    public void inputElecPrices(int period) {
        double discount = (100 - excelDocument.getDiscount())/100; // Коэффициент для расчета скидки
        double monthSum_3pc = 0;
        double monthSum_4pc = 0;
        double monthSum_3pc_discount = 0;
        double monthSum_4pc_discount = 0;
        double[] hourElecSums_3pc = new double[744];
        double[] hourElecSums_4pc = new double[744];
        double[] hourElecSums_3pc_discount = new double[744];
        double[] hourElecSums_4pc_discount = new double[744];
        year = excelDocument.getYear();

        var path = String.format("c:/EnergyReports/%s/downloads/priceHour/%s%s_%s_priceHour.xls", plant, year, NumberUtils.format(period), plant);
        xls = open(path);

        //powerOptPrice = xls.getSheetAt(0).getRow(31).getCell(1).getNumericCellValue();
        // получение средневзвешенной цены мощности на оптовом рынке          // используем метод getStringCellValue() т.к. значение в ячейке определяется как String
        powerOptPrice = Double.parseDouble(xls.getSheetAt(0).getRow(31).getCell(1).getStringCellValue().replace(",", "."));

        //serviceRegimeFee = xls.getSheetAt(0).getRow(34).getCell(1).getNumericCellValue();
        // получение цены за услугу по управлению изменением режима потребления электроэнерии
        serviceRegimeFee = Double.parseDouble(xls.getSheetAt(0).getRow(34).getCell(1).getStringCellValue().replace(",", "."));

        //В данном блоке получаем различные компоненты тарифа из базы данных
        var powerTariffs = transPeakService.getTariffs();
        var tariffComponents = transPeakService.getComponents();
        double transportFee = 0;
        double transportFee_1st = 0;
        double supplyFee = 0;
        double infraFee = 0;
        if(period < 7) {
            if (plant.equals("Klin")) {
                transportFee = tariffComponents.getLast().getFirstHalf_Klin_tsprt();
                transportFee_1st = powerTariffs.getLast().getFirstHalf_Klin_1st();
                supplyFee = tariffComponents.getLast().getFirstHalf_Klin_sp();
                infraFee = tariffComponents.getLast().getFirstHalf_Klin_infra();
                powerTransPrice = powerTariffs.getLast().getFirstHalf_Klin();
            } else if (plant.equals("Bor")) {
                transportFee = tariffComponents.getLast().getFirstHalf_Bor_tsprt();
                transportFee_1st = powerTariffs.getLast().getFirstHalf_Bor_1st();
                supplyFee = tariffComponents.getLast().getFirstHalf_Bor_sp();
                infraFee = tariffComponents.getLast().getFirstHalf_Bor_infra();
                powerTransPrice = powerTariffs.getLast().getFirstHalf_Bor();
            }
        } else {
            if (plant.equals("Klin")) {
                transportFee = tariffComponents.getLast().getSecondHalf_Klin_tsprt();
                transportFee_1st = powerTariffs.getLast().getSecondHalf_Klin_1st();
                supplyFee = tariffComponents.getLast().getSecondHalf_Klin_sp();
                infraFee = tariffComponents.getLast().getSecondHalf_Klin_infra();
                powerTransPrice = powerTariffs.getLast().getSecondHalf_Klin();
            } else if (plant.equals("Bor")) {
                transportFee = tariffComponents.getLast().getSecondHalf_Bor_tsprt();
                transportFee_1st = powerTariffs.getLast().getSecondHalf_Bor_1st();
                supplyFee = tariffComponents.getLast().getSecondHalf_Bor_sp();
                infraFee = tariffComponents.getLast().getSecondHalf_Bor_infra();
                powerTransPrice = powerTariffs.getLast().getSecondHalf_Bor();
            }
        }

        // заполнение часовых цен в отчетный документ
        try {
            int i = 0;
            while (Double.valueOf(xls.getSheetAt(0).getRow(57 + i).getCell(5).getNumericCellValue()) != null) {
                optPrices[i] = xls.getSheetAt(0).getRow(57 + i).getCell(5).getNumericCellValue();
                // Цена электроэнергии без скидки по 3-й и 4-ой ценовой категории
                hourElecSums_3pc[i] = hourVolumes[i] * (optPrices[i] + transportFee_1st + supplyFee + infraFee);
                hourElecSums_4pc[i] = hourVolumes[i] * (optPrices[i] + transportFee + supplyFee + infraFee);
                // Цена электроэнергии со скидкой по 3-й и 4-ой ценовой категории
                hourElecSums_3pc_discount[i] = hourVolumes[i] * (optPrices[i] + transportFee_1st + supplyFee*discount + infraFee);
                hourElecSums_4pc_discount[i] = hourVolumes[i] * (optPrices[i] + transportFee + supplyFee*discount + infraFee);

                // Стоимость электроэнергии без скидки по 3-й и 4-ой ценовой категории
                monthSum_3pc += hourElecSums_3pc[i];
                monthSum_4pc += hourElecSums_4pc[i];
                // Стоимость электроэнергии со скидкой по 3-й и 4-ой ценовой категории
                monthSum_3pc_discount += hourElecSums_3pc_discount[i];
                monthSum_4pc_discount += hourElecSums_4pc_discount[i];
                xlsx.getSheetAt(0).getRow(13 + i).getCell(2).setCellValue(optPrices[i]);
                i++;
            }
        } catch (NullPointerException npe) {}

        documentDto.setOptPrices(optPrices);
        documentDto.setServiceRegimeFee(serviceRegimeFee);
        documentDto.setPowerOptPrice(powerOptPrice);
        documentDto.setPowerTransPrice(powerTransPrice);
        documentDto.setElecPriceMonth_3pc(monthSum_3pc);
        documentDto.setElecPriceMonth_4pc(monthSum_4pc);
        documentDto.setElecPriceMonth_3pc_discount(monthSum_3pc_discount);
        documentDto.setElecPriceMonth_4pc_discount(monthSum_4pc_discount);
        // Сохраняем данные в excel-файл
        File filePath = new File("c:/EnergyReports/"+plant+"/ElecPrices");
        filePath.mkdirs();
        save(filePath.getPath() +"/"+ period+"_"+plant+"_elec.xlsx");
    }

    public void createReport(double[] monthVolumes, double[] monthSum) {
        var report = new XSSFWorkbook();
        report.createSheet().createRow(1);
        report.getSheetAt(0).createRow(2);

        for(int i = 1; i < monthVolumes.length; i++) {
            report.getSheetAt(0).getRow(1).createCell(1+i).setCellValue(monthVolumes[i]);
            report.getSheetAt(0).getRow(2).createCell(1+i).setCellValue(monthSum[i]);
        }

        try (FileOutputStream output = new FileOutputStream("c:/EnergyReports/elecReport.xlsx")) { // try-with-resources
            report.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
