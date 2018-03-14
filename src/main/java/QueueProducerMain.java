import java.io.BufferedWriter;
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
		
		// Initialize Hazelcast instance
		HazelcastInstanceUtils.getInstance();
		
		// Populate historical data from file and put into Hazelcast
		logger.info("Number of FX Rates loaded from file: " + populateHistoricalFxData());

		// Check number of objects loaded into Hazelcast List
		logger.info("Number of FX Rates in Hazelcast: " + HazelcastInstanceUtils.getList(HazelcastInstanceUtils.getHistoricalListName()).size());
	
		// Put execution tasks into the Hazelcast queue
		logger.info ("Producer Started...");
		for ( int k = 1; k <= numberOfTaks; k++ ) {
			ExecutionTask executionTask = new ExecutionTask (("Task-"+k),"Calculation",
			"{"+
			    "\"field1\": "+k+","+
			    "\"field2\": \"value2\","+
			    "\"field3\": 12.50,"+
			    "\"field4\": [\"item4_1\", \"item4_2\"]"+
			"}",System.currentTimeMillis());				

			HazelcastInstanceUtils.putIntoQueue(HazelcastInstanceUtils.getTaskQueueName(), executionTask );
			logger.info ("Producing: " + k);
			Thread.sleep(sleepTime);
		}
		
		// Put Stop Signal into the Hazelcast queue if required
		if (sendStopProcessingSignal) {
			HazelcastInstanceUtils.putStopSignalIntoQueue(HazelcastInstanceUtils.getTaskQueueName());
		}
		logger.info ("Producer Finished!");
		logger.info ("Waiting " + monitorDelay + " secs to start monitoring");
		Thread.sleep(monitorDelay*1000);
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

	// Populates historical FX data and puts the objects into Hazelcast List
    // FX Historical Data format: basecurrency;quotecurrency;date;value;
    public static int populateHistoricalFxData () 
    	throws Exception {
    	
    	int counter=0;
    	logger.info ("Populating historical FX data from " + SystemUtils.getHistoricalDataPath() + SystemUtils.getHistoricalDataFileName() + "...",true);
    	try {
    		CSVReader reader = new CSVReader(new InputStreamReader(QueueProducerMain.class.getClass().getResourceAsStream(SystemUtils.getHistoricalDataPath() + SystemUtils.getHistoricalDataFileName())),';');
	        String [] nextLine;
	        while ((nextLine = reader.readNext()) != null) {
	        	counter++;
	        	FxRate fxRate = new FxRate (nextLine);
	        	logger.info ("Line " + counter + " : " + fxRate.toCsvFormat());
	    		HazelcastInstanceUtils.putIntoList(HazelcastInstanceUtils.getHistoricalListName(), fxRate );
	        }
	        reader.close();
	    	logger.info ("Populating historical FX data done",true);
	    	
    	} catch (Exception ex) {
    		logger.error ("Exception in line " + counter + " - " + ex.getClass() + " - " + ex.getMessage());
    		throw ex;
    	}
    	return counter;
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