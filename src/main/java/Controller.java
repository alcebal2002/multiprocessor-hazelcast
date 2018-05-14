import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.IQueue;
import com.opencsv.CSVReader;

import datamodel.ExecutionTask;
import datamodel.FxRate;
import datamodel.WorkerDetail;
import executionservices.RunnableWorkerThread;
import utils.ApplicationProperties;
import utils.Constants;
import utils.HazelcastInstanceUtils;
import utils.SystemUtils;

public class Controller {
	
	// Logger
	private static Logger logger = LoggerFactory.getLogger(Controller.class);

	private static int numberOfTasks;
	private static int sleepTime;
	private static boolean sendStopProcessingSignal;
	private static boolean loadHistoricalData;
	private static boolean writeResultsToFile;
	private static int monitorDelay;
	
	public static void main( String[] args ) throws Exception {
	  
		logger.info("Application started");
		logger.info("Loading application properties from " + Constants.APPLICATION_PROPERTIES);

		// Load application properties
		numberOfTasks = ApplicationProperties.getIntProperty(Constants.CONTROLLER_QUEUEPRODUCER_NUMBER_OF_TASKS);
		sleepTime = ApplicationProperties.getIntProperty(Constants.CONTROLLER_QUEUEPRODUCER_SLEEPTIME);
		sendStopProcessingSignal = ApplicationProperties.getBooleanProperty(Constants.CONTROLLER_QUEUEPRODUCER_SENDSTOPPROCESSINGSIGNAL);
		loadHistoricalData = ApplicationProperties.getBooleanProperty(Constants.CONTROLLER_LOAD_HISTORICAL_DATA);
		writeResultsToFile = ApplicationProperties.getBooleanProperty(Constants.CONTROLLER_WRITE_RESULTS_TO_FILE);
		monitorDelay = ApplicationProperties.getIntProperty(Constants.CONTROLLER_MONITOR_DELAY);
		
		// Print parameters used
		printParameters ("Start");

		// Initialize Hazelcast instance
		HazelcastInstanceUtils.getInstance();
		
		// Populate historical data from file and put into Hazelcast
		HazelcastInstanceUtils.setStatus(Constants.HZ_STATUS_LOADING_HISTORICAL_DATA);
		if (loadHistoricalData) {
			logger.info("[FX loaded / #Files] : [" + populateHistoricalFxData() + " / " + HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getHistoricalMapName()).size() + "]");
		}
	
		// Put execution tasks into the Hazelcast queue
		HazelcastInstanceUtils.setStatus(Constants.HZ_STATUS_PUBLISHING_TASKS);
		logger.info ("Producer Started...");
		
		createExecutionTasks();
		
		// Put Stop Signal into the Hazelcast queue if required
		if (sendStopProcessingSignal) {
			logger.info ("Sending " + HazelcastInstanceUtils.getStopProcessingSignal() + " to " + HazelcastInstanceUtils.getTaskQueueName());
			HazelcastInstanceUtils.putStopSignalIntoQueue(HazelcastInstanceUtils.getTaskQueueName());
		}
		logger.info ("Producer Finished.");
		HazelcastInstanceUtils.setStatus(Constants.HZ_STATUS_WAITING_TO_START_MONITORING);
		logger.info ("Waiting " + monitorDelay + " secs to start monitoring");
		Thread.sleep(monitorDelay*1000);
		HazelcastInstanceUtils.setStatus(Constants.HZ_STATUS_PROCESSING_TASKS);
		logger.info ("Checking " + HazelcastInstanceUtils.getMonitorMapName() + " every "+monitorDelay+" secs");
		Thread.sleep(monitorDelay*1000);

		boolean stopMonitoring;

		while ( true ) {
			stopMonitoring = true;

			Iterator<Entry<String, Object>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();

			while (iter.hasNext()) {
	            Entry<String, Object> entry = iter.next();
	            if (((WorkerDetail) entry.getValue()).getActiveStatus()) stopMonitoring = false;
	        }
			
			if (stopMonitoring) {
				logger.info ("All clients are inactive. Stopping monitoring...");
				// Puts Stop signal into the results queue
				logger.info ("Sending " + HazelcastInstanceUtils.getStopProcessingSignal () + " to " + HazelcastInstanceUtils.getResultsQueueName());
				HazelcastInstanceUtils.putStopSignalIntoQueue(HazelcastInstanceUtils.getResultsQueueName());
				break;
			} else {
				logger.info ("Keeping the monitoring running every " + monitorDelay + " secs until all the clients are inactive...");
				Thread.sleep(monitorDelay*1000);
			}
		}
		
		// Get all the results from the threads and combine them into a final Results Map
		// Listen to Hazelcast tasks queue and submit work to the thread pool for each task 
		Map<String,Integer> completeResultsMap = new HashMap<String,Integer>();
		IQueue<Object> hazelcastResultsQueue = HazelcastInstanceUtils.getResultsQueue();
		logger.info ("Reading from Hazelcast: " + HazelcastInstanceUtils.getResultsQueueName() + "...");
		while ( true ) {
			Object resultItem = hazelcastResultsQueue.take();
			logger.debug ("Consumed Results from : " + HazelcastInstanceUtils.getResultsQueueName());
			
			if (resultItem instanceof String) {
				if ( (HazelcastInstanceUtils.getStopProcessingSignal()).equals(resultItem) ) {
					logger.info ("Detected " + HazelcastInstanceUtils.getStopProcessingSignal());
					break;
				}
			} else if (resultItem instanceof HashMap) {
				Iterator<Entry<String, Integer>> iter = ((HashMap<String,Integer>)resultItem).entrySet().iterator();
				
				while (iter.hasNext()) {
		            Entry<String, Integer> entry = iter.next();
					if (completeResultsMap.containsKey(entry.getKey())) {
						completeResultsMap.put(entry.getKey(),completeResultsMap.get(entry.getKey())+1);
					} else {
						completeResultsMap.put(entry.getKey(),entry.getValue());
					}
		        }

			}
		}
		
		logger.info("Complete Results: " + completeResultsMap.toString());
		
		if (writeResultsToFile) {
			writeWorkersLog ();
		}
		HazelcastInstanceUtils.setStatus(Constants.HZ_STATUS_PROCESS_COMPLETED);
		// Shutdown Hazelcast cluster node instance
		logger.info ("Shutting down hazelcast instace...");
		HazelcastInstanceUtils.shutdown();
		
		printParameters ("Finished");
		// Write cluster nodes execution summary into a file if required
		// Exit application
		//System.exit(0);
	}

	// Populates historical data and puts the objects into Hazelcast Map
    // FX Historical Data format: conversionDate,conversionTime,open,high,low,close
    public static int populateHistoricalFxData () 
    	throws Exception {
    	
    	int totalCounter=0;
    	
    	logger.info("Getting all the FX Data files (" + ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_FILE_EXTENSION) + ") from "+ ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_PATH));
    	
    	List<String> dataFiles = SystemUtils.getFilesFromPath(ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_PATH),ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_FILE_EXTENSION));
    	List<FxRate> fxList;
    	
    	for(String dataFile : dataFiles){
    		int fileCounter=0;
    		
    		fxList = new ArrayList<FxRate>();
        	logger.info ("Populating historical FX data from " + dataFile + "...",true);
        	
        	try {
        		CSVReader reader = new CSVReader(new FileReader(ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_PATH) + dataFile));
        		// CSVReader reader = new CSVReader(new InputStreamReader(Controller.class.getClass().getResourceAsStream(ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_PATH) + ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_FILE_NAME) + ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_FILE_EXTENSION))),ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_SEPARATOR).charAt(0));
    	        String [] nextLine;
    	        while ((nextLine = reader.readNext()) != null) {
    	        	
    	        	FxRate fxRate = new FxRate (dataFile.substring(0,dataFile.indexOf(".")),nextLine,fileCounter);
    	        	fxList.add(fxRate);
    				if (fileCounter%10000 == 0) {
    		        	logger.info ("Loaded " + fileCounter + " FX rates so far");
    				}
    				fileCounter++;
    				totalCounter++;
    	        }
    	        logger.info (dataFile.substring(0,dataFile.indexOf(".")) + " total FX rates loaded: " + fileCounter);
    	        reader.close();
    	        
    	        HazelcastInstanceUtils.putIntoMap(HazelcastInstanceUtils.getHistoricalMapName(),dataFile.substring(0,dataFile.indexOf(".")),fxList);
    	    	logger.info ("Populating historical data finished",true);
    	    	
        	} catch (Exception ex) {
        		logger.error ("Exception in file " + dataFile + " - line " + fileCounter + " - " + ex.getClass() + " - " + ex.getMessage());
        		throw ex;
        	}
    	}
    	return totalCounter;
    }

	// Creates the execution tasks and puts them into Hazelcast Execution Queue
    @SuppressWarnings("unchecked")
	public static void createExecutionTasks () 
    	throws Exception {
    	
    	logger.info("Putting execution tasks into Hazelcast for processing");
    	Iterator<Entry<String, Object>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getHistoricalMapName()).entrySet().iterator();

		while (iter.hasNext()) {
            Entry<String, Object> entry = iter.next();
            
            List<FxRate> fxList = (List<FxRate>) entry.getValue();
            int counter = 0;
            
            for (FxRate fxRate : fxList) {
            	counter++;
                //ExecutionTask executionTask = new ExecutionTask ((entry.getKey()+"-"+counter),"FXRATE",(""+fxRate.getId()),entry.getKey(),System.currentTimeMillis());				
            	ExecutionTask executionTask = new ExecutionTask ((entry.getKey()+"-"+counter),"FXRATE",fxRate.getPositionId(),ApplicationProperties.getFloatProperty(Constants.CONTROLLER_EXECUTION_INCREASE_PERCENTAGE),ApplicationProperties.getFloatProperty(Constants.CONTROLLER_EXECUTION_DECREASE_PERCENTAGE),fxRate,System.currentTimeMillis());

        		HazelcastInstanceUtils.putIntoQueue(HazelcastInstanceUtils.getTaskQueueName(), executionTask );
        		logger.info ("Producing: " + counter);
        		Thread.sleep(sleepTime);            
            }
            logger.info ("Produced " + counter + " tasks for " + entry.getKey());
        }
		
		/*
		// (simulating incoming tasks)
		for ( int k = 1; k <= numberOfTasks; k++ ) {
			ExecutionTask executionTask = new ExecutionTask (("Task-"+k),"Calculation",
					Constants.CONTROLLER_QUEUEPRODUCER_TASK_CONTENT.replaceAll("<counter>", ""+k),System.currentTimeMillis());				

			HazelcastInstanceUtils.putIntoQueue(HazelcastInstanceUtils.getTaskQueueName(), executionTask );
			logger.info ("Producing: " + k);
			Thread.sleep(sleepTime);
		}
		*/
    }    

	// Print execution parameters 
	private static void printParameters (final String title) {
		logger.info ("");
		logger.info ("****************************************************"); 
		logger.info (title + " QueueProducer with the following parameters:"); 
		logger.info ("****************************************************"); 
		logger.info ("  - number of tasks      : " + numberOfTasks); 
		logger.info ("  - sleep time           : " + sleepTime); 
		logger.info ("  - send stop signal     : " + sendStopProcessingSignal); 
		logger.info ("  - load historical data : " + loadHistoricalData); 
		logger.info ("  - write results to log : " + writeResultsToFile); 
		logger.info ("  - monitor delay (secs) : " + monitorDelay); 
		logger.info ("****************************************************");
	}
	
	// Write All the workers final log (summary) into a file
    private static void writeWorkersLog () throws Exception {
		BufferedWriter bWriter = null;
		Path path = Paths.get(LocalDateTime.now().format(DateTimeFormatter.ofPattern(ApplicationProperties.getStringProperty(Constants.CONTROLLER_WORKER_LOG_FILE_PATTERN)))+ApplicationProperties.getStringProperty(Constants.CONTROLLER_WORKER_LOG_FILE_EXTENSION));
							
		logger.info ("Writing worker result into file " + path);
		try {
			bWriter = Files.newBufferedWriter(path);
			
			if (HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()) != null &&
				HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).size() > 0) {
				Iterator<Entry<String, Object>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();
	
				while (iter.hasNext()) {
		            Entry<String, Object> entry = iter.next();
		            bWriter.write(((WorkerDetail) entry.getValue()).toCsvFormat());
		        }
			} else {
				 bWriter.write("No workers found");
			}
		} catch (Exception ex) {
			logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
		} finally {
			try {
				if (bWriter != null)
					bWriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}