package com.anz;

import static org.junit.Assert.*;

import java.io.*;

import org.json.JSONException;
import org.junit.*;


public class FxCalculatorTests {
	private final ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
	private final ByteArrayOutputStream sysErr = new ByteArrayOutputStream();
	private final PrintStream originalOut = System.out;
	private final PrintStream originalErr = System.err;
	
	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(sysOut));
		System.setErr(new PrintStream(sysErr));
	}
	
	@After
	public void restoreStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}
	
	@Test
	public void testFxCalculation() {
		RateComputationEngine rateComputationEngine;
		try {
			rateComputationEngine = new RateComputationEngine(1);
		} catch (FileNotFoundException e) {
			fail();
			return;
		} catch (JSONException e) {
			fail();
			return;
		}
		rateComputationEngine.convertCurrencyRate("NOK 100.34 in JPY");
		// %n is needed since actual output has a carriage return at the end:
		assertEquals(String.format("NOK 100.34 = JPY 1711%n"), sysOut.toString());
	}
	
	@Test
	public void testInvalidInput() {
		RateComputationEngine converter;
		try {
			converter = new RateComputationEngine(1);
		} catch (FileNotFoundException e) {
			fail();
			return;
		} catch (JSONException e) {
			fail();
			return;
		}
		converter.convertCurrencyRate("random invalid input");
		assertEquals(String.format("Incorrect input. Type in a line the form of \"<convert from> <number of units> in <convert to>\". Example: AUD 100 in USD.%n"), sysErr.toString());
	}
	
	@Test
	public void testUnknownRate() {
		RateComputationEngine converter;
		try {
			converter = new RateComputationEngine(1);
		} catch (FileNotFoundException e) {
			fail();
			return;
		} catch (JSONException e) {
			fail();
			return;
		}
		converter.convertCurrencyRate("ABC 100.20 in XYZ");
		String[] messageLines = sysErr.toString().split("\\R", 2);
		assertEquals("Unable to find rate for ABC/XYZ", messageLines[0]);
	}
}
