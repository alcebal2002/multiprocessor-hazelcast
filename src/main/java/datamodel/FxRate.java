package datamodel;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FxRate implements Serializable {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(FxRate.class);
	
	private static final long serialVersionUID = 1L;
	private String baseCurrency;
	private String quoteCurrency;
	private String conversionDate;
	private float value;
	
	/**
	 * @param baseCurrency
	 * @param quoteCurrency
	 * @param conversionDate
	 * @param value
	 */
	public FxRate(String baseCurrency, String quoteCurrency, String conversionDate, String conversionValue) 
		throws NumberFormatException {
		this.baseCurrency = baseCurrency;
		this.quoteCurrency = quoteCurrency;
		this.conversionDate = conversionDate;
		this.value = Float.parseFloat(conversionValue);
	}
	
	public FxRate(String[] line)
		throws NumberFormatException {
		this.baseCurrency = line[0];
		this.quoteCurrency = line[1];
		this.conversionDate = line[2];
		this.value = Float.parseFloat(line[3]);
	}

	public final String getBaseCurrency() {
		return baseCurrency;
	}
	public final String getQuoteCurrency() {
		return quoteCurrency;
	}
	public final String getConversionDate() {
		return conversionDate;
	}
	public final float getValue() {
		return value;
	}
	
	public final String toCsvFormat () {
		return  this.getConversionDate() + ";" +
				this.getBaseCurrency() + ";" +
				this.getQuoteCurrency() + ";" +
				this.getValue() + ";"; 
	}
} 