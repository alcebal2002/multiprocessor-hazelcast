import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

public class WorkerPool {

	// Default parameter values 
	private static int poolCoreSize = 5;
	private static int poolMaxSize = 10; 
	private static int queueCapacity = 50; 
	private static int timeoutSecs = 50; 
	private static int processTime = 5000; 
	private static int retrySleepTime = 5000; 
	private static int retryMaxAttempts = 5; 
	private static int initialSleep = 5; 
	private static int monitorSleep = 3; 

	private static int taskNumber = 0; 
	
	private static HazelcastInstance hz;
	
	private static final String stopProcessingSignal = "STOP_PROCESSING_SIGNAL";
	private static final String taskQueueName = "taskQueue";
	private static final String monitorMapName = "monitorMap";
	
	public static void main(String args[]) throws InterruptedException { 

		if (args != null && args.length == 9) { 
			poolCoreSize = Integer.parseInt(args[0]); 
			poolMaxSize = Integer.parseInt(args[1]); 
			queueCapacity = Integer.parseInt(args[2]); 
			timeoutSecs = Integer.parseInt(args[3]); 
			processTime = Integer.parseInt(args[4]); 
			retrySleepTime = Integer.parseInt(args[5]); 
			retryMaxAttempts = Integer.parseInt(args[6]); 
			initialSleep = Integer.parseInt(args[7]); 
			monitorSleep = Integer.parseInt(args[8]); 
		} else { 
			printLog("Not all parameters informed. Using default values"); 
			printLog(""); 
			printLog("Usage: java WorkerPool <pool core size> <pool max size> <queue capacity> <timeout (secs)> <task process (ms)> <retry sleep (ms)> <retry max attempts> <initial sleep (secs)> <monitor sleep (secs)>"); 
			printLog("  Example: java WorkerPool 10 15 20 50 5000 5000 5 5 3"); 
			printLog(""); 
			printLog("System will continue processing until a task with " + stopProcessingSignal + " content is received");
			printLog(""); 
		} 

		printLog("Waiting " + initialSleep + " secs to start...",true); 
		Thread.sleep(initialSleep*1000); 

		printParameters ("Started");

		// RejectedExecutionHandler implementation 
		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl(); 
		// Get the ThreadFactory implementation to use 
		ThreadFactory threadFactory = Executors.defaultThreadFactory(); 
		// Creating the ThreadPoolExecutor 
		MyThreadPoolExecutor executorPool = new MyThreadPoolExecutor(poolCoreSize, poolMaxSize, timeoutSecs, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueCapacity), threadFactory, rejectionHandler); 
		
		//Initialize hazelcast and get inetAddressAndPort
		hz = Hazelcast.newHazelcastInstance();

		long startTime = System.currentTimeMillis();
		
		NodeDetails currentNodeDetails = new NodeDetails(
				getNodeId(), 
				getInetAddress(),
				getInetPort(),
				poolCoreSize,
				poolMaxSize,
				queueCapacity,
				timeoutSecs,
				processTime,
				retrySleepTime,
				retryMaxAttempts,
				initialSleep,
				monitorSleep,
				taskNumber,
				startTime);
		
		// Start the monitoring thread 
		MyMonitorThread monitor = new MyMonitorThread(executorPool, monitorSleep, getInetAddress()+":"+getInetPort()); 
		Thread monitorThread = new Thread(monitor); 
		monitorThread.start(); 

		//Initialize and define the hazelcast cache maps and queues
		IMap<String, NodeDetails> monitorMap = hz.getMap(monitorMapName);
		monitorMap.put(getNodeId(),currentNodeDetails);
		
		// Listen to tasks (taskQueue) and submit work to the thread pool 
		IQueue<String> hazelcastTaskQueue = hz.getQueue( taskQueueName );
		while ( true ) {
			String item = hazelcastTaskQueue.take();
			printLog("Consumed: " + item + " from Hazelcast Task Queue",true);
			if ( stopProcessingSignal.equals(item) ) {
				printLog("Detected " + stopProcessingSignal, true);
				hazelcastTaskQueue.put( stopProcessingSignal );
				break;
			}
			executorPool.execute(new WorkerThread(processTime,item,retrySleepTime,retryMaxAttempts, hz, getNodeId()));
			taskNumber++;
		}
		printLog("Hazelcast consumer Finished",true);

		// Shut down the pool 
		printLog("Shutting down executor pool...",true); 
		executorPool.shutdown(); 
		printLog(executorPool.getTaskCount() + " tasks. No additional tasks will be accepted",true); 

		// Shut down the monitor thread 
		while (!executorPool.isTerminated()) { 
			printLog("Waiting for all the Executor to terminate",true); 
			Thread.sleep(monitorSleep*1000); 
		} 

		printLog("Executor terminated",true); 
		printLog("Shutting down monitor thread...",true); 
		monitor.shutdown(); 
		printLog("Shutting down monitor thread... done",true); 
		long stopTime = System.currentTimeMillis();

		currentNodeDetails = monitorMap.get(getNodeId());
		currentNodeDetails.setStopTime(stopTime);
		monitorMap.put(getNodeId(),currentNodeDetails);
		
		printLog("Shutting down hazelcast client...",true);
		hz.getLifecycleService().shutdown();
		
		printParameters ("Finished");
		printLog("Results:"); 
		printLog("**************************************************"); 
		printLog("  - Start time  : " + new Timestamp(startTime)); 
		printLog("  - Stop time   : " + new Timestamp(stopTime)); 

		long millis = stopTime - startTime;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days); 
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes); 
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		printLog("  - Elapsed time: " + (stopTime - startTime) + " ms - (" + hours + " hrs " + minutes + " min " + seconds + " secs)"); 
		printLog("**************************************************"); 
		printLog("  - Min elapsed execution time: " + executorPool.getMinExecutionTime() + " ms"); 
		printLog("  - Max elapsed execution time: " + executorPool.getMaxExecutionTime() + " ms"); 
		printLog("  - Avg elapsed execution time: " + (executorPool.getMaxExecutionTime() + executorPool.getMinExecutionTime())/2 + " ms");
		printLog("**************************************************"); 
		System.exit(0);	
	}
	
	private static void printParameters (final String title) {
		printLog("");
		printLog("**************************************************"); 
		printLog(title + " WorkerPool with the following parameters:"); 
		printLog("**************************************************"); 
		printLog("  - pool core size       : " + poolCoreSize); 
		printLog("  - pool max size        : " + poolMaxSize); 
		printLog("  - queue capacity       : " + queueCapacity); 
		printLog("  - timeout (secs)       : " + timeoutSecs); 
		printLog("  - number of tasks      : " + taskNumber); 
		printLog("  - task process (ms)    : " + processTime); 
		printLog("  - retry sleep (ms)     : " + retrySleepTime); 
		printLog("  - retry max attempts   : " + retryMaxAttempts);
		printLog("  - initial sleep (secs) : " + retryMaxAttempts); 
		printLog("  - monitor sleep (secs) : " + monitorSleep); 
		printLog("**************************************************");
	}
	
	private static void printLog (final String textToPrint) {
		printLog (textToPrint, false);
	}

	private static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
	
    private static String getNodeId () {
    	String result = "unknown";
        try {
            result = hz.getCluster().getLocalMember().getUuid();
        } catch (Exception ex) {}
        return result;
    }

    private static String getInetAddress () {
    	String result = "unknown";
        try {
            result = hz.getCluster().getLocalMember().getSocketAddress().getHostName();
        } catch (Exception ex) {}
        return result;
    }

    private static int getInetPort () {
    	int result = 0;
        try {
            result = hz.getCluster().getLocalMember().getSocketAddress().getPort();
        } catch (Exception ex) {}
        return result;
    }
} 