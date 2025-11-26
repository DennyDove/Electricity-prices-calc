package com.denidove.Energy.services;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.utils.NIO;
import com.denidove.Energy.utils.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.SessionScope;

@Setter
@Getter

@Service
@SessionScope
public class ExcelDocument {

    @Autowired
    private DocumentDto documentDto;

    protected int workingDays;
    protected int workingDayIndex[] = new int[31];
    protected int[] dayCalendar = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
    protected double[] optPrices = new double[744];
    protected double[] hourVolumes = new double[744];

    protected double serviceRegimeFee;
    protected double powerOptPrice;
    protected double powerTransPrice;
    protected double capacityOptVol;
    protected double capacityTransVol;

    protected HSSFWorkbook xls;
    protected XSSFWorkbook xlsx;

    protected String plant;
    protected int year;
    protected double discount;

    public HSSFWorkbook open(String xlsDoc)  {

        // Определяем папку, в которой лежит JAR-файл
        File jarDir = NIO.getAppDirectory();
        File file = new File(jarDir, xlsDoc);

        try (FileInputStream is = new FileInputStream(file)) { // try-with-resources
            xls = new HSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xls;
    }

    // Данный вариант метода нужен для корректной загрузки excel-шаблонов из JAR файла
    public HSSFWorkbook openXlsAsStream(String xlsDoc)  {
        try (InputStream inputStream = ExcelDocument.class.getResourceAsStream(xlsDoc)) { // try-with-resources
            xls = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xls;
    }


    public XSSFWorkbook openXlsx(String xlsxDoc)  {
        try (FileInputStream is = new FileInputStream(xlsxDoc)) { // try-with-resources
            xlsx = new XSSFWorkbook(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xlsx;
    }

    // Данный вариант метода нужен для корректной загрузки excel-шаблонов из JAR файла
    public XSSFWorkbook openXlsxAsStream(String xlsxDoc)  {
        try (InputStream inputStream = ExcelDocument.class.getResourceAsStream(xlsxDoc)) { // try-with-resources
            xlsx = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xlsx;
    }

    //  Выгрузка отчетного файла на жесткий диск
    public void save(String path) {
        try (FileOutputStream output = new FileOutputStream(path)) { // try-with-resources
            // Метод заставляет пересчитаться формулы при следующем открытии файла
            XSSFFormulaEvaluator.evaluateAllFormulaCells(xlsx);
            xlsx.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для скачивания пиковых нагрузок и отключенния сертификатов безопасности (с сайта АТС)
    public void downloadPikHour(int startPeriod, int endPerid) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            factory = acceptingAllCertificates();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        RestTemplate restTemplate_SSL_OFF = new RestTemplate(factory);

        // Определяем папку, в которой лежит JAR-файл
        File jarDir = NIO.getAppDirectory();

        String url = "";
        String urlPlant = "";
        File filePath = new File(jarDir, plant +"/downloads/pikHour");
        filePath.mkdirs();

        if(plant.equals("Klin")) urlPlant = "MOSENERG_46_calcfacthour";
        if(plant.equals("Bor")) urlPlant = "NIGNOVEN_22_calcfacthour";

        for(int i = startPeriod; i<endPerid+1; i++) {
            // Метод NumberUtils.format(i) нужен для перевода числа в формат 01, 02, 03 и т.д
            var monthStart = NumberUtils.format(i);

            //    "https://www.atsenergo.ru/dload/calcfacthour_regions/20250"+ i + urlPlant + ".xls"
            url = String.format("https://www.atsenergo.ru/dload/calcfacthour_regions/%s%s_%s.xls", year, monthStart, urlPlant);
            byte[] xlsBytes = restTemplate_SSL_OFF.getForObject(url, byte[].class);

            try {
                var path = String.format(filePath +"/%s%s_%s_peaks.xls", year, monthStart, plant);
                Files.write(Paths.get(path), xlsBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод для скачивания цен ГП и отключенния сертификатов безопасности (с сайта АТС)
    public void downloadElecPrices(int startPeriod, int endPerid) {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            factory = acceptingAllCertificates();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        RestTemplate restTemplate_SSL_OFF = new RestTemplate(factory);

        // Определяем папку, в которой лежит JAR-файл
        File jarDir = NIO.getAppDirectory();

        String url ="";
        String urlPlant = "";
        File filePath = new File(jarDir, plant +"/downloads/priceHour");
        filePath.mkdirs();
        if(plant.equals("Klin")) urlPlant = "MOSENERG_PMOSENER";
        if(plant.equals("Bor")) urlPlant = "NIGNOVEN_PNIGNOVE";
        for(int i = startPeriod; i < endPerid + 1; i++) {
            // Метод NumberUtils.format(i) нужен для перевода числа в формат 01, 02, 03 и т.д
            var monthStart = NumberUtils.format(i);
            var monthEnd = NumberUtils.format(i+1);

                               //https://www.atsenergo.ru/dload/retail/20250801/20250910_MOSENERG_PMOSENER_082025_gtp_1st_stage.xls
            url = String.format("https://www.atsenergo.ru/dload/retail/%s%s01/%s%s10_%s_%s%s_gtp_1st_stage.xls", year, monthStart, year, monthEnd, urlPlant, monthStart, year);
            byte[] xlsBytes = restTemplate_SSL_OFF.getForObject(url, byte[].class);

            try {
                var path = String.format(filePath + "/%s%s_%s_priceHour.xls", year, monthStart, plant);
                Files.write(Paths.get(path), xlsBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //создание настройки ClientHttpRequestFactory для отключения всех сертификатов (в том числе SSL)
    public final HttpComponentsClientHttpRequestFactory acceptingAllCertificates() throws GeneralSecurityException {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create().
                                setTlsSocketStrategy(
                                        new DefaultClientTlsStrategy(
                                                SSLContextBuilder.create()
                                                        .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                                                        .build(),
                                                HostnameVerificationPolicy.BOTH,
                                                NoopHostnameVerifier.INSTANCE
                                        )
                                )
                                .build()
                )
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

}
