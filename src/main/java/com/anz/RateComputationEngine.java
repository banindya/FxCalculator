package com.anz;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

import org.json.*;


public class RateComputationEngine {
	
	Logger logger = Logger.getLogger("FxCalculatorLog");
	
	private ArrayList<RateConvert> rates = new ArrayList<>();
	
	private Semaphore semaphore;
	

	public RateComputationEngine(int permits)
			throws FileNotFoundException, JSONException {
		loadRates();
		semaphore = new Semaphore(permits, true);
	}
	

	private void loadRates() 
			throws FileNotFoundException, JSONException {
		loadDirectFeedRates();
		getInvertedRates();
		getCrossRates();
	}
	

	private void loadDirectFeedRates() 
			throws  JSONException {
		JSONArray directFeeds = new JSONArray(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream("directRateConversions.json")));
		for (int i=0; i < directFeeds.length(); i++) {
			String base = directFeeds.getJSONObject(i).getString("base");
			String terms = directFeeds.getJSONObject(i).getString("terms");
			float rate = (float)directFeeds.getJSONObject(i).getDouble("rate");
			rates.add(new RateConvert(CurrencyRepo.valueOf(base), CurrencyRepo.valueOf(terms), rate));
		}
	}
	

	private void getInvertedRates() {
		RateConvert[] invertedRates = new RateConvert[10];
		for (int i = 0; i < invertedRates.length; i++) {
			invertedRates[i] = new RateConvert(rates.get(i).getTerms(), rates.get(i).getBase(), 1/rates.get(i).getRate());
		}
		rates.addAll(Arrays.asList(invertedRates));
	}
	

	private void getCrossRates() {
		CurrencyRepo[] currencies = CurrencyRepo.values();
		for(int i=0; i<currencies.length; i++) {
	        for(int j=0; j<currencies.length; j++) {
	        	getRate(currencies[i], currencies[j]);
	        }
		}
	}
	

	private void getRate(CurrencyRepo base, CurrencyRepo terms) {

		if (base == terms) {
			rates.add(new RateConvert(base, terms, 1f));
			return;
		}
		if (rates.stream().anyMatch(rate -> rate.getBase() == base && rate.getTerms() == terms))
			return;

		CurrencyRepo crossVia = (base.isEurCross() && terms.isEurCross()) ? CurrencyRepo.EUR : CurrencyRepo.USD;
		getCrossRates(base, terms, crossVia);
	}
	

	private void getCrossRates(CurrencyRepo base, CurrencyRepo terms, CurrencyRepo crossVia) {
		RateConvert baseCrossVia;
		try {
			baseCrossVia = rates.stream().filter((rate) -> rate.getBase() == base && rate.getTerms() == crossVia).findFirst().orElseThrow(IllegalStateException::new);
		} catch (IllegalStateException ex) {
			CurrencyRepo knownRateCurrency = (crossVia == CurrencyRepo.EUR) ? CurrencyRepo.USD : CurrencyRepo.EUR;
			getCrossRates(base, crossVia, knownRateCurrency);
			baseCrossVia = rates.stream().filter((rate) -> rate.getBase() == base && rate.getTerms() == crossVia).findFirst().orElseThrow(IllegalStateException::new);
		}
		RateConvert crossViaTerms;
		try {
			crossViaTerms = rates.stream().filter((rate) -> rate.getBase() == crossVia && rate.getTerms() == terms).findFirst().orElseThrow(IllegalStateException::new);
		} catch (IllegalStateException ex) {
			CurrencyRepo knownRateCurrency = (crossVia == CurrencyRepo.EUR) ? CurrencyRepo.USD : CurrencyRepo.EUR;
			getCrossRates(crossVia, terms, knownRateCurrency);
			crossViaTerms = rates.stream().filter((rate) -> rate.getBase() == crossVia && rate.getTerms() == terms).findFirst().orElseThrow(IllegalStateException::new);
		}
		rates.add(new RateConvert(base, terms, baseCrossVia.getRate()*crossViaTerms.getRate()));
	}
	

	public void convertCurrencyRate(String input) {
		try {
			semaphore.acquire();
			if (!input.matches("^([A-Z]{3} \\d+(\\.\\d{2})? in [A-Z]{3})$"))
				throw new InputMismatchException("Invalid user input: " + input);
			String[] inputValues = input.split(" ");

			RateConvert rateConvert;
			float units;
			try {
				CurrencyRepo base = CurrencyRepo.valueOf(inputValues[0]);
				CurrencyRepo terms = CurrencyRepo.valueOf(inputValues[3]);
				units = Float.parseFloat(inputValues[1]);
				rateConvert = rates.stream().filter((rate) -> rate.getBase() == base && rate.getTerms() == terms).findFirst().orElseThrow(IllegalStateException::new);
			} catch (IllegalArgumentException|IllegalStateException ex) {
				System.err.println("Unable to find rate for " + inputValues[0] + "/" + inputValues[3]);
				logger.log(Level.WARNING, ex.getMessage(), ex);
				return;
			}
			if (rateConvert.getTerms() == CurrencyRepo.JPY) {
				int result = Math.round(units * rateConvert.getRate());
				System.out.println("" + rateConvert.getBase() + " " + inputValues[1] + " = " + rateConvert.getTerms() + " " + result);
			} else {
				float result = Math.round((units * rateConvert.getRate())*100f)/100f;
				System.out.println("" + rateConvert.getBase() + " " + inputValues[1] + " = " + rateConvert.getTerms() + " " + result);
			}

		} catch (InterruptedException ex) {
			System.err.println("Conversion interrupted.");
			logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (InputMismatchException ex) {
			System.err.println("Incorrect input. Type in a line the form of \"<convert from> <number of units> in <convert to>\". Example: AUD 100 in USD.");
			logger.log(Level.WARNING, ex.getMessage(), ex);
		} catch (Exception ex) {
			System.err.println("Unexpected error.");
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
		finally {
			semaphore.release();
		}
	}
}
