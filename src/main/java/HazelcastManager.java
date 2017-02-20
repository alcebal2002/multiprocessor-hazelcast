import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Arrays;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.opencsv.CSVReader;

public class HazelcastManager {

	private static HazelcastInstance hazelcastInstance;
	private static final String taskQueueName = "taskQueue";
	private static final String monitorMapName = "monitorMap";
	private static final String historicalListName = "historicalList";
/*
	private static final String[] historicalListHeader = {"Date","USD","JPY","BGN","CYP",
														  "CZK","DKK","EEK","GBP","HUF",
														  "LTL","LVL","MTL","PLN","ROL",
														  "RON","SEK","SIT","SKK","CHF",
														  "ISK","NOK","HRK","RUB","TRL",
														  "TRY","AUD","BRL","CAD","CNY",
														  "HKD","IDR","INR","KRW","MXN",
														  "MYR","NZD","PHP","SGD","THB",
														  "ZAR","ILS"};
*/
	private static final String[] inscopeCurrencyList = {"GBP", "USD", "JPY"};

	private static final String stopProcessingSignal = "STOP_PROCESSING_SIGNAL";

	protected HazelcastManager() {}
	
	public static synchronized HazelcastInstance getInstance () {
		if (hazelcastInstance == null) {
			hazelcastInstance = Hazelcast.newHazelcastInstance(); 
		}
		
		return hazelcastInstance;
	}
	
	public static String getTaskQueueName () {
		return taskQueueName;
	}

	public static String getMonitorMapName () {
		return monitorMapName;
	}

	public static String getStopProcessingSignal () {
		return stopProcessingSignal;
	}

	public static String getHistoricalListName () {
		return historicalListName;
	}

	public static void putStopSignalIntoQueue (final String queueName) {
		printLog ("Sending " + getStopProcessingSignal(),true);
		putIntoQueue(queueName,getStopProcessingSignal());
	}

	public static void putIntoQueue (final String queueName, final String value) {
		try {
			getInstance().getQueue(queueName).put(value);
		} catch (InterruptedException e) {
			printLog ("Exception: " + e.getClass() + " - " + e.getMessage());
		}
	}

	public static Object getFromMap  (final String mapName, final String key) {
		return getInstance().getMap(mapName).get(key);
	}
	
	public static void putIntoMap (final String mapName, final String key, final Object value) {
		getInstance().getMap(mapName).put(key,value);
	}
	
	public static void putIntoList (final String listName, final Object value) {
		getInstance().getList(listName).add(value);
	}

	public static String getNodeId () {
    	String result = "unknown";
        try {
            result = getInstance().getCluster().getLocalMember().getUuid();
        } catch (Exception ex) {}
        return result;
    }

    public static String getInetAddress () {
    	String result = "unknown";
        try {
            result = getInstance().getCluster().getLocalMember().getSocketAddress().getHostName();
        } catch (Exception ex) {}
        return result;
    }

    public static int getInetPort () {
    	int result = 0;
        try {
            result = getInstance().getCluster().getLocalMember().getSocketAddress().getPort();
        } catch (Exception ex) {}
        return result;
    }

    public static int populateHistoricalData () {
    	int counter=0;
    	printLog ("Populating historical data...",true);
    	try {
	    	CSVReader reader = new CSVReader(new FileReader("src/main/resources/eurofxref-hist.csv"));
	        String [] nextLine;
	        while ((nextLine = reader.readNext()) != null) {
	        	counter++;
	        	putIntoList (getHistoricalListName(), Arrays.toString(nextLine));
	        	//printLog (nextLine[0] + nextLine[1] + "etc...");
	        }
	        reader.close();
	    	printLog ("Populating historical data done",true);
	    	
    	} catch (Exception ex) {
    		printLog ("Exception: " + ex.getClass() + " - " + ex.getMessage());
    	}
    	return counter;
    }
    
    
	public static void printLog (final String textToPrint) {
		printLog (textToPrint, false);
	}

	public static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
}
