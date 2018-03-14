package utils;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import datamodel.FxRate;

public class SystemUtils {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(SystemUtils.class);
	
	// Resources names
	private static final String mainResourcePath = "";
	private static final String historicalDataPath = "/historical_data/";
	private static final String templatesPath = "/templates/";
	private static final String publicPath = "/public/";

	// File names
	private static final String resultTemplateFileName = "result.ftl";
	private static final String historicalDataFileName = "fxhistoricaldata.csv";
/*
	// Historical data headers
	private static final String[] historicalListHeader = {"Date","USD","JPY","BGN","CYP",
														  "CZK","DKK","EEK","GBP","HUF",
														  "LTL","LVL","MTL","PLN","ROL",
														  "RON","SEK","SIT","SKK","CHF",
														  "ISK","NOK","HRK","RUB","TRL",
														  "TRY","AUD","BRL","CAD","CNY",
														  "HKD","IDR","INR","KRW","MXN",
														  "MYR","NZD","PHP","SGD","THB",
														  "ZAR","ILS"};
	private static final String[] inscopeCurrencyList = {"USD","JPY","GBP"};
*/

	public static String getHistoricalDataFileName () {
		return historicalDataFileName;
	}

	public static String getMainResourcePath () {
		return mainResourcePath;
	}

	public static String getHistoricalDataPath () {
		return historicalDataPath;
	}

	public static String getTemplatesPath () {
		return templatesPath;
	}

	public static String getPublicPath () {
		return publicPath;
	}

	public static String getResultTemplateFileName () {
		return resultTemplateFileName;
	}

    public static String getHostName () {
    	String result = "unknown";
        try {
            result = InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {}
        return result;
    }
    
	public static int getIntParameterOrDefault (String args[], int argPosition, int defaultValue) {
		return ((args != null) && (args.length >= argPosition+1))?Integer.parseInt(args[argPosition]):defaultValue;
	}

	public final Date getDateFromString(String date, String format) {
		Date result = null;
		try {
			// format example: "dd-mm-yyyy"
			result = new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
