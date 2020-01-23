package com.anz;

import java.util.HashMap;
import java.util.Map;

public enum CurrencyRepo  {
	AUD("Australian dollar", false),
	CAD("Canadian dollar", false),
	CNY("Chinese yuan", false),
	CZK("Czech koruna", true),
	DKK("Danish krone", true),	
	EUR("Euro", false),
	GBP("Pound sterling", false),
	JPY("Japanese yen", false),
	NOK("Norwegian krone", true),
	NZD("New Zealand dollar", false),
	USD("United States dollar", true);



    private static final Map<String, CurrencyRepo> BY_LABEL = new HashMap<>();
     
    static {
        for (CurrencyRepo c : values()) {
            BY_LABEL.put(c.label(), c);
        }
    }
 
    private final String label;
    private final boolean isEurCross;
 
    private CurrencyRepo(String label, boolean isEurCross) {
        this.label = label;
        this.isEurCross = isEurCross;
    }
    
    public static CurrencyRepo valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }
    
    public String label() {
        return label;
    }
    
    public boolean isEurCross() {
        return isEurCross;
    }
}
