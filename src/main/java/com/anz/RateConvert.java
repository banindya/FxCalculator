package com.anz;

public class RateConvert {
	private CurrencyRepo base;
	private CurrencyRepo terms;
	private float rate;
	
	public RateConvert(CurrencyRepo base, CurrencyRepo terms, float rate) {
		this.base = base;
		this.terms = terms;
		this.rate = rate;
	}
	
	public CurrencyRepo getBase() { return base; }
	public CurrencyRepo getTerms() { return terms; }
	public float getRate() { return rate; }
}
