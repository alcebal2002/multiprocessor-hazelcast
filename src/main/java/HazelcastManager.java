import java.sql.Timestamp;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastManager {

	private static HazelcastInstance hazelcastInstance;
	private static final String taskQueueName = "taskQueue";
	private static final String monitorMapName = "monitorMap";

	private static final String stopProcessingSignal = "STOP_PROCESSING_SIGNAL";

	protected HazelcastManager() {}
	
	public static HazelcastInstance getInstance () {
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

	public static void putStopSignalIntoQueue (final String queueName) {
		printLog ("Sending " + stopProcessingSignal,true);
		putIntoQueue(queueName,stopProcessingSignal);
	}

	public static void putIntoQueue (final String queueName, final String value) {
		try {
			getInstance().getQueue(queueName).put(value);
		} catch (InterruptedException e) {
			printLog ("Exception: " + e.getClass() + " - " + e.getMessage());
		}
	}

	public static Object getFromMap (final String mapName, final String key) {
		return getInstance().getMap(mapName).get(key);
	}
	
	public static void putIntoMap (final String mapName, final String key, final Object value) {
		getInstance().getMap(mapName).put(key,value);
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

    private static void printLog (final String textToPrint) {
		printLog (textToPrint, false);
	}

	private static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
}
