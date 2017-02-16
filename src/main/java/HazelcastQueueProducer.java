import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

public class HazelcastQueueProducer {
	
	private static int numberOfTaks = 0;
	private static int sleepTime = 0;
	private static boolean sendStopProcessingSignal = false;
	private static final String stopProcessingSignal = "STOP_PROCESSING_SIGNAL";
	private static final String taskQueueName = "taskQueue";
	private static final String monitorMapName = "monitorMap";
	private static final int monitorDelay = 10;
	
	public static void main( String[] args ) throws Exception {
	  
		if (args != null && args.length >= 2) { 
			numberOfTaks = Integer.parseInt(args[0]);
			sleepTime = Integer.parseInt(args[1]);
			if (args.length == 3 && ("true".equalsIgnoreCase(args[2]))) {
				sendStopProcessingSignal = true;
			}
		} else { 
			printLog ("Not all parameters informed",true); 
			printLog (""); 
			printLog ("Usage: java HazelcastQueueProducer <number of tasks> <sleep (ms)> <send stop processing signal"); 
			printLog ("  Example: java HazelcastQueueProducer 1000 5 false");
			printLog (""); 
		} 
		  
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		IQueue<String> queue = hz.getQueue( taskQueueName );
		for ( int k = 1; k <= numberOfTaks; k++ ) {
		  queue.put( "Task-"+k );
		  System.out.println( "Producing: " + k );
		  Thread.sleep(sleepTime);
		}
		
		if (sendStopProcessingSignal) {
			printLog ("Sending " + stopProcessingSignal,true);
			queue.put( stopProcessingSignal );
		}
		printLog ("Producer Finished!",true);

		List<Long> elapsedArrayList;
		long avgElapsedTime;
		int totalProcessed = 0;
		int totalProcessedPrev = 0;
		String result;
		
		while ( true ) {
			result = "";
			IMap<String,NodeDetails> monitorMap = hz.getMap(monitorMapName);
			if (monitorMap != null && monitorMap.size() > 0) {
				printLog("******************************************");
				printLog("Node | Start Time | Stop Time | # Tasks processed | Avg time");
				result = result + "Node;Start Time;Stop Time;# Tasks processed;Avg time\n";
				for (Map.Entry<String,NodeDetails> nodeEntry : monitorMap.entrySet()) {

					elapsedArrayList = nodeEntry.getValue().getElapsedArray();
					
					avgElapsedTime = 0L;
					if (elapsedArrayList.size() > 0) {
						totalProcessed = totalProcessed + elapsedArrayList.size();
						for (int i=0; i < elapsedArrayList.size(); i++) {
							avgElapsedTime += elapsedArrayList.get(i);
						}
						avgElapsedTime = avgElapsedTime / elapsedArrayList.size();
					}
					
					printLog(nodeEntry.getValue().getInetAddres() + ":" +  nodeEntry.getValue().getInetPort() + " | " +
							new Timestamp(nodeEntry.getValue().getStartTime()) + " | " +
							((nodeEntry.getValue().getStopTime()>0L)?(new Timestamp(nodeEntry.getValue().getStopTime())):" - ") + " | " +
							elapsedArrayList.size() + " | " + 
							avgElapsedTime);
					
					result = result + nodeEntry.getValue().getInetAddres() + ":" +  nodeEntry.getValue().getInetPort() + ";" +
							new Timestamp(nodeEntry.getValue().getStartTime()) + ";" +
							((nodeEntry.getValue().getStopTime()>0L)?(new Timestamp(nodeEntry.getValue().getStopTime())):" - ") + ";" +
							elapsedArrayList.size() + ";" + 
							avgElapsedTime+"\n";
				}
			} else {
				printLog("No " + monitorMapName + " found",true);
			}
			printLog("Waiting " + monitorDelay + " secs to check " + monitorMapName,true);
			Thread.sleep(monitorDelay*1000);
			
			if (totalProcessed > 0 && totalProcessedPrev == totalProcessed) {
				printLog("No activity detected in the last " +  monitorDelay + " secs ["+totalProcessedPrev+" vs "+totalProcessed+"]. Shutting down",true);
				break;
			}

			totalProcessedPrev = totalProcessed;
			totalProcessed = 0;
		}
		printLog("Shutting down hazelcast client...",true);
		hz.getLifecycleService().shutdown();
		
		writeLogFile (result);
		
		System.exit(0);
	}

	private static void printLog (final String textToPrint) {
		printLog (textToPrint, false);
	}

	private static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
	
	private static void writeLogFile (final String result) {
		
		Path path = Paths.get("results/"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HHmmss"))+".csv");
		printLog("Writing result file " + path);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
		    writer.write(result);
		} catch (Exception ex) {
			printLog("Exception: " + ex.getClass() + " - " + ex.getMessage());
		}
	}
}