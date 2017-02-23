import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;

import com.hazelcast.core.IMap;

import datamodel.NodeDetails;
import utils.HazelcastManager;

public class QueueProducerMain {
	
	private static int numberOfTaks = 0;
	private static int sleepTime = 0;
	private static boolean sendStopProcessingSignal = false;
//	private static final String hitoricalTicksMapName = "historicalTicksMap";
	private static final int monitorDelay = 10;
	
	public static void main( String[] args ) throws Exception {
	  
		if (args != null && args.length >= 2) { 
			numberOfTaks = Integer.parseInt(args[0]);
			sleepTime = Integer.parseInt(args[1]);
			if (args.length == 3 && ("true".equalsIgnoreCase(args[2]))) {
				sendStopProcessingSignal = true;
			}
		} else { 
			HazelcastManager.printLog ("Not all parameters informed",true); 
			HazelcastManager.printLog (""); 
			HazelcastManager.printLog ("Usage: java HazelcastQueueProducer <number of tasks> <sleep (ms)> <send stop processing signal"); 
			HazelcastManager.printLog ("  Example: java HazelcastQueueProducer 1000 5 false");
			HazelcastManager.printLog (""); 
		} 
		  
		// Populate historical data
		HazelcastManager.populateHistoricalData();
		
		for ( int k = 1; k <= numberOfTaks; k++ ) {
			HazelcastManager.putIntoQueue(HazelcastManager.getTaskQueueName(), ("Task-"+k) );
			HazelcastManager.printLog ("Producing: " + k);
			Thread.sleep(sleepTime);
		}
		
		if (sendStopProcessingSignal) {
			HazelcastManager.putStopSignalIntoQueue(HazelcastManager.getTaskQueueName());
		}
		HazelcastManager.printLog ("Producer Finished!",true);

		int totalProcessed = 0;
		int totalProcessedPrev = 0;
		String result;
		
		while ( true ) {
			result = "";
			IMap<String,NodeDetails> monitorMap = HazelcastManager.getInstance().getMap(HazelcastManager.getMonitorMapName());
			if (monitorMap != null && monitorMap.size() > 0) {
				HazelcastManager.printLog("******************************************");
				HazelcastManager.printLog("Node | Start Time | Stop Time | # Tasks processed | Avg time");
				result = result + "Node;Start Time;Stop Time;# Tasks processed;Avg time\n";
				for (Map.Entry<String,NodeDetails> nodeEntry : monitorMap.entrySet()) {
					HazelcastManager.printLog(nodeEntry.getValue().getInetAddres() + ":" +  nodeEntry.getValue().getInetPort() + " | " +
							new Timestamp(nodeEntry.getValue().getStartTime()) + " | " +
							((nodeEntry.getValue().getStopTime()>0L)?(new Timestamp(nodeEntry.getValue().getStopTime())):" - ") + " | " +
							nodeEntry.getValue().getElapsedArray().size() + " | " + 
							nodeEntry.getValue().getAvgElapsedTime());
					
					result = result + nodeEntry.getValue().getInetAddres() + ":" +  nodeEntry.getValue().getInetPort() + ";" +
							new Timestamp(nodeEntry.getValue().getStartTime()) + ";" +
							((nodeEntry.getValue().getStopTime()>0L)?(new Timestamp(nodeEntry.getValue().getStopTime())):" - ") + ";" +
							nodeEntry.getValue().getElapsedArray().size() + ";" + 
							nodeEntry.getValue().getAvgElapsedTime()+"\n";
					
					totalProcessed = totalProcessed + nodeEntry.getValue().getElapsedArray().size();
				}
			} else {
				HazelcastManager.printLog("No " + HazelcastManager.getMonitorMapName() + " found",true);
			}
			HazelcastManager.printLog("Waiting " + monitorDelay + " secs to check " + HazelcastManager.getMonitorMapName(),true);
			Thread.sleep(monitorDelay*1000);
			
			if (totalProcessed > 0 && totalProcessedPrev == totalProcessed) {
				HazelcastManager.printLog("No activity detected in the last " +  monitorDelay + " secs ["+totalProcessedPrev+" vs "+totalProcessed+"]. Shutting down",true);
				break;
			}

			totalProcessedPrev = totalProcessed;
			totalProcessed = 0;
		}
		
		Iterator<Object> iterator = HazelcastManager.getInstance().getList(HazelcastManager.getHistoricalListName()).iterator();
		int numHistoricalRecords=0;
		while ( iterator.hasNext() ) {
			numHistoricalRecords++;
			HazelcastManager.printLog ("Historical Value["+numHistoricalRecords+"]: " + iterator.next());
		}
		
		HazelcastManager.printLog("Shutting down hazelcast client...",true);
		HazelcastManager.getInstance().getLifecycleService().shutdown();
		
		writeLogFile (result);
		
		System.exit(0);
	}

	private static void writeLogFile (final String result) {
		
		Path path = Paths.get("target/"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss"))+".csv");
		HazelcastManager.printLog("Writing result file " + path);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		    writer.write(result);
		} catch (Exception ex) {
			HazelcastManager.printLog("Exception: " + ex.getClass() + " - " + ex.getMessage());
		}
	}
}