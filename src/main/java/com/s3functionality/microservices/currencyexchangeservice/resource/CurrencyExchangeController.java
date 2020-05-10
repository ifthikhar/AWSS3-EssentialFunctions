package com.s3functionality.microservices.currencyexchangeservice.resource;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.s3functionality.microservices.currencyexchangeservice.service.AWSS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.s3functionality.microservices.currencyexchangeservice.util.environment.InstanceInformationService;

@RestController
public class CurrencyExchangeController {
	@Value("${amazonProperties.bucketName}")
	private String bucketName;

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyExchangeController.class);

	@Autowired
	private ExchangeValueRepository repository;

	@Autowired
	private InstanceInformationService instanceInformationService;

	@Autowired
	AWSS3Service awss3Service;

	@GetMapping("/hello")
	public String getMessage()
	{
		return "Hello --> Green Deployment";
	}

	@DeleteMapping("/deleteFile")
	public String deleteFile(@RequestPart(value = "url") String fileUrl) {
		return deleteExchangeFile(fileUrl,bucketName);
	}



	@GetMapping("/currency-exchange/from/{from}/to/{to}")
	public ExchangeValue retrieveExchangeValue(@PathVariable String from, @PathVariable String to,
			@RequestHeader Map<String, String> headers) {

		printAllHeaders(headers);

		ExchangeValue exchangeValue = repository.findByFromAndTo(from, to);

		LOGGER.info("{} {} {}", from, to, exchangeValue);

		if (exchangeValue == null) {
			throw new RuntimeException("Unable to find data to convert " + from + " to " + to);
		}

		exchangeValue.setExchangeEnvironmentInfo(instanceInformationService.retrieveInstanceInfo());
		try
		{

			System.out.println(saveExchangeRequest(exchangeValue));
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
		}

		return exchangeValue;
	}

	private String  saveExchangeRequest(ExchangeValue exchangeValue) throws IOException {
		//create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();
		//configure objectMapper for pretty input
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//write customerObj object to customer2.json file
		Date date = new Date();
		File file= new File(date+"Request.json");

		objectMapper.writeValue(file, exchangeValue);
		return awss3Service.uploadFileTos3bucket(file.getName(),file,bucketName);


	}

	private String deleteExchangeFile(String fileUrl, String bucketName)
	{
		return awss3Service.deleteFileFromS3Bucket(fileUrl, bucketName);
	}

	private void printAllHeaders(Map<String, String> headers) {
		headers.forEach((key, value) -> {
			LOGGER.info(String.format("Header '%s' = %s", key, value));
		});
	}
}
