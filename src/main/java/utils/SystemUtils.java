package utils;
import java.net.InetAddress;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final String historicalDataFileName = "eurofxref-hist.csv";
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

    public static int populateHistoricalData () {
    	
    	int counter=0;
    	/*
    	printLog ("Populating historical data from " + getHistoricalDataPath() + getHistoricalDataFileName() + "...",true);
    	try {
    		CSVReader reader = new CSVReader(new InputStreamReader(HazelcastManager.class.getClass().getResourceAsStream(getHistoricalDataPath() + getHistoricalDataFileName())));
	        String [] nextLine;
	        while ((nextLine = reader.readNext()) != null) {
	        	counter++;
	        	putIntoList (getHistoricalListName(), Arrays.toString(nextLine));
	        	//logger.info (nextLine[1] + nextLine[2] + nextLine[8]);
	        }
	        reader.close();
	    	printLog ("Populating historical data done",true);
	    	
    	} catch (Exception ex) {
    		logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
    	}
    	*/
    	return counter;
    }
    
	public static int getIntParameterOrDefault (String args[], int argPosition, int defaultValue) {
		return args[argPosition] != null?Integer.parseInt(args[argPosition]):defaultValue;
	}

}
