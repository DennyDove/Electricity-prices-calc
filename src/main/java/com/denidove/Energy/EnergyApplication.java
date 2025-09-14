package com.denidove.Energy;

import com.denidove.Energy.dto.DocumentDto;
import com.denidove.Energy.services.ElecService;
import com.denidove.Energy.services.ExcelDocument;
import com.denidove.Energy.services.MatrixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EnergyApplication {

	@Autowired
	private DocumentDto documentDto;
	@Autowired
	private ExcelDocument excelDocument;
	@Autowired
	private ElecService elecService;
	@Autowired
	private MatrixService matrixService;

	private static final Logger log = LoggerFactory.getLogger(EnergyApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(EnergyApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			log.info("Command line started");
			log.info("");

			//elecService.downloadPikHour(1, 7);
			//elecService.downloadElecPrices(1, 7);

			int month = 4;

			/*
			for(int month = 1; month < 8; month++) {
				elecService.inputConsumptionValues(month);
				elecService.inputPricesMES(month);
				elecService.save(month);
			}*/

		};
	}

}
