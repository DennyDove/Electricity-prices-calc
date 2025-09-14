package com.denidove.Energy.dto;

import com.denidove.Energy.enums.Month;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;

@Setter
@Getter

@Service
@SessionScope
public class DocumentDto {

    private String plant;

    private Month startPeriod;
    private Month endPeriod;

    private List<TransPiksDto> transPiksDtoList;

    private double discount;
    private double serviceRegimeFee;
    private double elecPriceMonth_3pc;
    private double elecPriceMonth_4pc;
    private double elecPriceMonth_3pc_discount;
    private double elecPriceMonth_4pc_discount;
    private double elecVolumeMonth;
    private double powerOptPrice;
    private double powerTransPrice;
    private double capacityOptVol;
    private double capacityTransVol;
    private double[] optPrices = new double[744];
    private double[] hourElecSums = new double[744];
    private double[] hourVolumes = new double[744];

    private HSSFWorkbook xls;
    private XSSFWorkbook elecPricesDocument;
    private XSSFWorkbook matrixDocument;
}
