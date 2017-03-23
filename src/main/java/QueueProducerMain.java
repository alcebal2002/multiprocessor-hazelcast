import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import datamodel.ExecutionTask;
import utils.HazelcastInstanceUtils;
import utils.SystemUtils;

public class QueueProducerMain {
	
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
			SystemUtils.printLog ("Not all parameters informed",true); 
			SystemUtils.printLog (""); 
			SystemUtils.printLog ("Usage: java HazelcastQueueProducer <number of tasks> <sleep (ms)> <send stop processing signal> <write results to file>"); 
			SystemUtils.printLog ("  Example: java HazelcastQueueProducer 1000 5 false true");
			SystemUtils.printLog (""); 
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
			SystemUtils.printLog ("Producing: " + k);
			Thread.sleep(sleepTime);
		}
		
		// Put Stop Signal into the Hazelcast queue if required
		if (sendStopProcessingSignal) {
			HazelcastInstanceUtils.putStopSignalIntoQueue(HazelcastInstanceUtils.getTaskQueueName());
		}
		SystemUtils.printLog ("Producer Finished!",true);
		Thread.sleep(monitorDelay*1000);
		SystemUtils.printLog ("Checking " + HazelcastInstanceUtils.getMonitorMapName() + " every "+monitorDelay+" secs",true);
		Thread.sleep(monitorDelay*1000);

		while ( HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).size() > 0 ) {
			Thread.sleep(monitorDelay*1000);
		}
		Thread.sleep(monitorDelay*1000);
		
		SystemUtils.printLog(HazelcastInstanceUtils.getMonitorMapName() + " empty",true);

		// Shutdown Hazelcast cluster node instance
		SystemUtils.printLog("Shutting down hazelcast client...",true);
		HazelcastInstanceUtils.shutdown();
		
		// Write cluster nodes execution summary into a file if required
/*
		if (writeResultsToFile) {
			writeLogFile (result);
		}
*/		
		// Exit application
		System.exit(0);
	}

	private static void writeLogFile (final String result) {
		
		Path path = Paths.get(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss"))+".csv");
		SystemUtils.printLog("Writing result file " + path);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		    writer.write(result);
		} catch (Exception ex) {
			SystemUtils.printLog("Exception: " + ex.getClass() + " - " + ex.getMessage());
		}
	}
}