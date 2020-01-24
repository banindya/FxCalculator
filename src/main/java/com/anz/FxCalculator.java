package com.anz;

import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import org.json.JSONException;

public class FxCalculator {
	
	private final static Logger logger = Logger.getLogger("FxCalculatorLog");
	
	public static void main(String[] args) {
		Properties properties = new Properties();
		int permits;
		RateComputationEngine rateComputationEngine;
		try {
			setUpLogging();
			properties.load(FxCalculator.class.getClassLoader().getResourceAsStream("app.properties"));
			permits = Integer.parseInt(properties.getProperty("converterPermits"));
			rateComputationEngine = new RateComputationEngine(permits);
		} catch (IOException|NumberFormatException ex) {
			System.err.println("Error loading configuration files.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			return;
		} catch (JSONException|IllegalArgumentException ex) {
			System.err.println("Error parsing direct feed rates from file.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			return;
		} catch (Exception ex) {
			System.err.println("Error initializing RateComputationEngine.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			return;
		}

		System.out.println("Please enter input in the format as \"<convert from> <number of units> in <convert to>\" or \"quit\" to exit.");
		try (Scanner scanner = new Scanner(System.in)) {
			String input;
			ExecutorService executor = Executors.newCachedThreadPool();
			while ((input = scanner.nextLine()) != null && !input.equals("quit")) {
				// asynchronous computation so as not to block the UI thread.
				final String finalInput = input;
				executor.submit(() -> rateComputationEngine.convertCurrencyRate(finalInput));
			}
			executor.shutdownNow();
		}
	}
	
	private static void setUpLogging() {
		LogManager.getLogManager().reset();
		logger.setLevel(Level.SEVERE);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.SEVERE);
		logger.addHandler(ch);
	}
}
