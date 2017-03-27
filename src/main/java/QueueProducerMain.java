import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datamodel.ClientDetails;
import datamodel.ExecutionTask;
import utils.HazelcastInstanceUtils;
import utils.SystemUtils;

public class QueueProducerMain {
	
	// Logger
	private static Logger logger = LoggerFactory.getLogger(QueueProducerMain.class);

	private static int numberOfTaks = 0;
	private static int sleepTime = 0;
	private static boolean sendStopProcessingSignal = false;
	private static boolean writeResultsToFile = false;
	private static final int monitorDelay = 10;
	
	public static void main( String[] args ) throws Exception {
	  
		// Check arguments
		if (args != null && args.length >= 2) { 
			numberOfTaks = Integer.parseInt(args[0]);
			sleepTime = Integer.parseInt(args[1]);

			if (args.length >= 3 && ("true".equalsIgnoreCase(args[2]))) {
				sendStopProcessingSignal = true;
			}
			if (args.length >= 4 && ("true".equalsIgnoreCase(args[3]))) {
				writeResultsToFile = true;
			}
		} else { 
			logger.info  ("Not all parameters informed"); 
			logger.info  (""); 
			logger.info  ("Usage: java HazelcastQueueProducer <number of tasks> <sleep (ms)> <send stop processing signal> <write results to file>"); 
			logger.info  ("  Example: java HazelcastQueueProducer 1000 5 false true");
			logger.info  (""); 
		} 
		  
		// Populate historical data
		SystemUtils.populateHistoricalData();
		
		// Put execution tasks into the Hazelcast queue
		for ( int k = 1; k <= numberOfTaks; k++ ) {
			ExecutionTask executionTask = new ExecutionTask (("Task-"+k),"Calculation",
			"{"+
			    "\"field1\": "+k+","+
			    "\"field2\": \"value2\","+
			    "\"field3\": 12.50,"+
			    "\"field4\": [\"item4_1\", \"item4_2\"]"+
			"}",System.currentTimeMillis());				

			HazelcastInstanceUtils.putIntoQueue(HazelcastInstanceUtils.getTaskQueueName(), executionTask );
			logger.info  ("Producing: " + k);
			Thread.sleep(sleepTime);
		}
		
		// Put Stop Signal into the Hazelcast queue if required
		if (sendStopProcessingSignal) {
			HazelcastInstanceUtils.putStopSignalIntoQueue(HazelcastInstanceUtils.getTaskQueueName());
		}
		logger.info  ("Producer Finished!");
		logger.info  ("Waiting " + monitorDelay + " secs to start monitoring");
		Thread.sleep(monitorDelay*1000);
		logger.info  ("Checking " + HazelcastInstanceUtils.getMonitorMapName() + " every "+monitorDelay+" secs");
		Thread.sleep(monitorDelay*1000);

		boolean stopMonitoring;

		while ( true ) {
			stopMonitoring = true;

			Iterator<Entry<String, ClientDetails>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();

			while (iter.hasNext()) {
	            Entry<String, ClientDetails> entry = iter.next();
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

		// Shutdown Hazelcast cluster node instance
		logger.info ("Shutting down hazelcast instace...");
		HazelcastInstanceUtils.shutdown();
		
		// Write cluster nodes execution summary into a file if required
/*
		if (writeResultsToFile) {
			writeLogFile (result);
		}
*/		
		// Exit application
		//System.exit(0);
	}

	private static void writeLogFile (final String result) {
		
		Path path = Paths.get(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss"))+".csv");
		logger.info ("Writing result file " + path);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		    writer.write(result);
		} catch (Exception ex) {
			logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
		}
	}
}