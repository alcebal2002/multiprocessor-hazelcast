import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import datamodel.ExecutionTask;
import datamodel.FxRate;
import datamodel.WorkerDetail;
import utils.ApplicationProperties;
import utils.Constants;
import utils.HazelcastInstanceUtils;

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
			logger.info("[loaded from file / loaded into Hazelcast] : [" + populateHistoricalFxData() + " / " + HazelcastInstanceUtils.getList(HazelcastInstanceUtils.getHistoricalListName()).size() + "]");
		}
	
		// Start queueproducer (simulating incoming tasks) and put execution tasks into the Hazelcast queue
		HazelcastInstanceUtils.setStatus(Constants.HZ_STATUS_PUBLISHING_TASKS);
		logger.info ("Producer Started...");
		for ( int k = 1; k <= numberOfTasks; k++ ) {
			ExecutionTask executionTask = new ExecutionTask (("Task-"+k),"Calculation",
					Constants.CONTROLLER_QUEUEPRODUCER_TASK_CONTENT.replaceAll("<counter>", ""+k),System.currentTimeMillis());				

			HazelcastInstanceUtils.putIntoQueue(HazelcastInstanceUtils.getTaskQueueName(), executionTask );
			logger.info ("Producing: " + k);
			Thread.sleep(sleepTime);
		}
		
		// Put Stop Signal into the Hazelcast queue if required
		if (sendStopProcessingSignal) {
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

			Iterator<Entry<String, WorkerDetail>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();

			while (iter.hasNext()) {
	            Entry<String, WorkerDetail> entry = iter.next();
	            if (entry.getValue().getActiveStatus()) stopMonitoring = false;
	        }
			
			if (stopMonitoring) {
				logger.info ("All clients are inactive. Stopping monitoring...");
				break;
			} else {
				logger.info ("Keeping the monitoring running every " + monitorDelay + " secs until all the clients are inactive...");
				Thread.sleep(monitorDelay*1000);
			}
		}

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

	// Populates historical data and puts the objects into Hazelcast List
    // FX Historical Data format: basecurrency;quotecurrency;date;value;
    public static int populateHistoricalFxData () 
    	throws Exception {
    	
    	int counter=0;
    	logger.info ("Populating historical data from " + ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_PATH) + ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_FILE_NAME) + "...",true);
    	try {
    		CSVReader reader = new CSVReader(new InputStreamReader(Controller.class.getClass().getResourceAsStream(ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_PATH) + ApplicationProperties.getStringProperty(Constants.CONTROLLER_HISTORICAL_DATA_FILE_NAME))),';');
	        String [] nextLine;
	        while ((nextLine = reader.readNext()) != null) {
	        	counter++;
	        	FxRate fxRate = new FxRate (nextLine);
	        	logger.info ("Line " + counter + " : " + fxRate.toCsvFormat());
	    		HazelcastInstanceUtils.putIntoList(HazelcastInstanceUtils.getHistoricalListName(), fxRate );
	        }
	        reader.close();
	    	logger.info ("Populating historical data finished",true);
	    	
    	} catch (Exception ex) {
    		logger.error ("Exception in line " + counter + " - " + ex.getClass() + " - " + ex.getMessage());
    		throw ex;
    	}
    	return counter;
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
				Iterator<Entry<String, WorkerDetail>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();
	
	
				while (iter.hasNext()) {
		            Entry<String, WorkerDetail> entry = iter.next();
		            bWriter.write(entry.getValue().toCsvFormat());
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