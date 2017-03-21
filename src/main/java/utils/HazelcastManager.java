package utils;
import java.sql.Timestamp;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

import datamodel.ExecutionTask;
import datamodel.NodeDetails;

public class HazelcastManager {

	private static HazelcastInstance hazelcastInstance;

	// Queue instance names
	private static final String taskQueueName = "taskQueue";
	
	// List instance names
	private static final String historicalDataListName = "historicalList";
	
	// Map instance names
	private static final String monitorMapName = "monitorMap";
	
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

	// Stop process signal
	private static final String stopProcessingSignal = "STOP_PROCESSING_SIGNAL";

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
		return historicalDataListName;
	}

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

	public static void putStopSignalIntoQueue (final String queueName) {
		printLog ("Sending " + getStopProcessingSignal() + " to " + queueName,true);
		putIntoQueue(queueName,(new ExecutionTask(getStopProcessingSignal())));
	}

	public static void putIntoQueue (final String queueName, final Object value) {
		try {
			getInstance().getQueue(queueName).put(value);
		} catch (InterruptedException e) {
			printLog ("Exception: " + e.getClass() + " - " + e.getMessage());
		}
	}

	public static IQueue<ExecutionTask> getQueue  (final String queueName) {
		return getInstance().getQueue(queueName);
	}

	public static IMap<String,NodeDetails> getMap  (final String mapName) {
		return getInstance().getMap(mapName);
	}

	public static Object getFromMap  (final String mapName, final String key) {
		return getInstance().getMap(mapName).get(key);
	}
	
	public static void putIntoMap (final String mapName, final String key, final Object value) {
		getInstance().getMap(mapName).put(key,value);
	}
	
	public static void removeFromMap (final String mapName, final String key) {
		getInstance().getMap(mapName).remove(key);
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
    	/*
    	printLog ("Populating historical data from " + getHistoricalDataPath() + getHistoricalDataFileName() + "...",true);
    	try {
    		CSVReader reader = new CSVReader(new InputStreamReader(HazelcastManager.class.getClass().getResourceAsStream(getHistoricalDataPath() + getHistoricalDataFileName())));
	        String [] nextLine;
	        while ((nextLine = reader.readNext()) != null) {
	        	counter++;
	        	putIntoList (getHistoricalListName(), Arrays.toString(nextLine));
	        	//printLog (nextLine[1] + nextLine[2] + nextLine[8]);
	        }
	        reader.close();
	    	printLog ("Populating historical data done",true);
	    	
    	} catch (Exception ex) {
    		printLog ("Exception: " + ex.getClass() + " - " + ex.getMessage());
    	}
    	*/
    	return counter;
    }
    
	public static void printLog (final String textToPrint) {
		printLog (textToPrint, false);
	}

	public static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
}
