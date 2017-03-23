package utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

import datamodel.ExecutionTask;
import executionservices.SystemThreadPoolExecutor;
import datamodel.ClientDetails;

public class HazelcastInstanceUtils {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(HazelcastInstanceUtils.class);

	private static HazelcastInstance hazelcastInstance;

	// Queue instance names
	private static final String taskQueueName = "taskQueue";
	
	// List instance names
	private static final String historicalDataListName = "historicalList";
	
	// Map instance names
	private static final String monitorMapName = "monitorMap";
	
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

	public static void putStopSignalIntoQueue (final String queueName) {
		logger.info ("Sending " + getStopProcessingSignal() + " to " + queueName);
		putIntoQueue(queueName,(new ExecutionTask(getStopProcessingSignal())));
	}

	public static void putIntoQueue (final String queueName, final Object value) {
		try {
			getInstance().getQueue(queueName).put(value);
		} catch (InterruptedException e) {
			logger.error ("Exception: " + e.getClass() + " - " + e.getMessage());
		}
	}

	public static IQueue<ExecutionTask> getQueue  (final String queueName) {
		return getInstance().getQueue(queueName);
	}

	public static IMap<String,ClientDetails> getMap  (final String mapName) {
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
    
    public static void shutdown() {
    	getInstance().getLifecycleService().shutdown();
    }
}
