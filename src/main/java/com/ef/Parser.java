package com.ef;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

@SpringBootApplication
public class Parser {

	public static void main(String[] args) {
		if (validCommandLineArgs(new SimpleCommandLinePropertySource(args))) {
			SpringApplication.run(Parser.class, args);
		}
	}

	@SuppressWarnings("rawtypes")
	private static boolean validCommandLineArgs(PropertySource propertySource) {

		Optional.ofNullable(isValidDuration(propertySource.getProperty("duration")))
				.orElseThrow(() -> new RuntimeException("duration cant be null or empty"));
		Optional.ofNullable(
				isThresHold(propertySource.getProperty("threshold"), propertySource.getProperty("duration")))
				.orElseThrow(() -> new RuntimeException("threshold cant be null or empty"));
		Optional.ofNullable(isValidStartDate(propertySource.getProperty("startDate")))
				.orElseThrow(() -> new RuntimeException("starDateTime cant be null or empty"));
		Optional.ofNullable(isFileExist(propertySource.getProperty("accesslog")))
				.orElseThrow(() -> new RuntimeException("accesslog cant be null or empty"));

		return true;

	}

	private static Boolean isFileExist(Object filePath) {
		Path path = Paths.get(String.valueOf(filePath));
		if (!Files.exists(path)) {
			throw new RuntimeException("fiel dont exist please check the file is in the root of ");
		}
		return true;
	}

	private static Boolean isThresHold(Object threshold, Object durationType) {
		try {
			Duration duration = Duration.valueOf(String.valueOf(durationType).toUpperCase());
			return duration.validateLimite(Integer.parseInt(String.valueOf(threshold)));
		} catch (NumberFormatException exception) {
			throw new RuntimeException("bad formated ThresholdValue");
		}
	}

	private static Boolean isValidStartDate(Object startDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
		try {
			sdf.parse(String.valueOf(startDate));
		} catch (ParseException e) {
			throw new RuntimeException("bad formated StartDate");
		}
		return true;
	}

	private static Boolean isValidDuration(Object duration) {
		try {
			Duration.valueOf(String.valueOf(duration).toUpperCase());
		} catch (Exception e) {
			throw new RuntimeException("bad Duration");
		}
		return true;
	}

}
