import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.IQueue;

import datamodel.NodeDetails;
import executionservices.SystemMonitorThread;
import executionservices.SystemThreadPoolExecutor;
import executionservices.RejectedExecutionHandlerImpl;
import executionservices.RunnableWorkerThread;
import utils.HazelcastManager;

public class WorkerPoolMain {

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
			HazelcastManager.printLog("Not all parameters informed. Using default values"); 
			HazelcastManager.printLog(""); 
			HazelcastManager.printLog("Usage: java WorkerPool <pool core size> <pool max size> <queue capacity> <timeout (secs)> <task process (ms)> <retry sleep (ms)> <retry max attempts> <initial sleep (secs)> <monitor sleep (secs)>"); 
			HazelcastManager.printLog("  Example: java WorkerPool 10 15 20 50 5000 5000 5 5 3"); 
			HazelcastManager.printLog(""); 
			HazelcastManager.printLog("System will continue processing until a task with " + HazelcastManager.getStopProcessingSignal() + " content is received");
			HazelcastManager.printLog(""); 
		} 

		HazelcastManager.printLog("Waiting " + initialSleep + " secs to start...",true); 
		Thread.sleep(initialSleep*1000); 

		printParameters ("Started");

		// RejectedExecutionHandler implementation 
		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl(); 
		// Get the ThreadFactory implementation to use 
		ThreadFactory threadFactory = Executors.defaultThreadFactory(); 
		// Creating the ThreadPoolExecutor 
		SystemThreadPoolExecutor executorPool = new SystemThreadPoolExecutor(poolCoreSize, poolMaxSize, timeoutSecs, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueCapacity), threadFactory, rejectionHandler); 
		
		//Initialize hazelcast and get inetAddressAndPort

		long startTime = System.currentTimeMillis();
		
		NodeDetails currentNodeDetails = new NodeDetails(
				HazelcastManager.getNodeId(), 
				HazelcastManager.getInetAddress(),
				HazelcastManager.getInetPort(),
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
		SystemMonitorThread monitor = new SystemMonitorThread(executorPool, monitorSleep, HazelcastManager.getInetAddress()+":"+HazelcastManager.getInetPort()); 
		Thread monitorThread = new Thread(monitor); 
		monitorThread.start(); 

		//Initialize and define the hazelcast cache maps and queues
		HazelcastManager.putIntoMap(HazelcastManager.getMonitorMapName(), HazelcastManager.getNodeId(), currentNodeDetails);
		
		// Listen to tasks (taskQueue) and submit work to the thread pool 
		IQueue<String> hazelcastTaskQueue = HazelcastManager.getInstance().getQueue( HazelcastManager.getTaskQueueName() );
		while ( true ) {
			String item = hazelcastTaskQueue.take();
			HazelcastManager.printLog("Consumed: " + item + " from Hazelcast Task Queue",true);
			if ( (HazelcastManager.getStopProcessingSignal()).equals(item) ) {
				HazelcastManager.printLog("Detected " + HazelcastManager.getStopProcessingSignal(), true);
				hazelcastTaskQueue.put( HazelcastManager.getStopProcessingSignal() );
				break;
			}
			executorPool.execute(new RunnableWorkerThread(processTime,item,retrySleepTime,retryMaxAttempts, HazelcastManager.getNodeId()));
			taskNumber++;
		}
		HazelcastManager.printLog("Hazelcast consumer Finished",true);

		// Shut down the pool 
		HazelcastManager.printLog("Shutting down executor pool...",true); 
		executorPool.shutdown(); 
		HazelcastManager.printLog(executorPool.getTaskCount() + " tasks. No additional tasks will be accepted",true); 

		// Shut down the monitor thread 
		while (!executorPool.isTerminated()) { 
			HazelcastManager.printLog("Waiting for all the Executor to terminate",true); 
			Thread.sleep(monitorSleep*1000); 
		} 

		HazelcastManager.printLog("Executor terminated",true); 
		HazelcastManager.printLog("Shutting down monitor thread...",true); 
		monitor.shutdown(); 
		HazelcastManager.printLog("Shutting down monitor thread... done",true); 
		long stopTime = System.currentTimeMillis();

		currentNodeDetails = (NodeDetails)HazelcastManager.getFromMap(HazelcastManager.getMonitorMapName(),HazelcastManager.getNodeId());
		currentNodeDetails.setStopTime(stopTime);
		currentNodeDetails.setActiveStatus(false);
		
		List<Long> elapsedArrayList = currentNodeDetails.getElapsedArray();
		long totalProcessed = 0L;
		long avgElapsedTime = 0L;

		if (elapsedArrayList.size() > 0) {
			totalProcessed = totalProcessed + elapsedArrayList.size();
			for (int i=0; i < elapsedArrayList.size(); i++) {
				avgElapsedTime += elapsedArrayList.get(i);
			}
			avgElapsedTime = avgElapsedTime / elapsedArrayList.size();
		}
		currentNodeDetails.setAvgElapsedTime(avgElapsedTime);		
		
		HazelcastManager.putIntoMap(HazelcastManager.getMonitorMapName(), HazelcastManager.getNodeId(), currentNodeDetails);
		
		HazelcastManager.printLog("Shutting down hazelcast client...",true);
		HazelcastManager.getInstance().getLifecycleService().shutdown();
		
		printParameters ("Finished");
		HazelcastManager.printLog("Results:"); 
		HazelcastManager.printLog("**************************************************"); 
		HazelcastManager.printLog("  - Start time  : " + new Timestamp(startTime)); 
		HazelcastManager.printLog("  - Stop time   : " + new Timestamp(stopTime)); 

		long millis = stopTime - startTime;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days); 
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes); 
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		HazelcastManager.printLog("  - Elapsed time: " + (stopTime - startTime) + " ms - (" + hours + " hrs " + minutes + " min " + seconds + " secs)"); 
		HazelcastManager.printLog("**************************************************"); 
		HazelcastManager.printLog("  - Min elapsed execution time: " + executorPool.getMinExecutionTime() + " ms"); 
		HazelcastManager.printLog("  - Max elapsed execution time: " + executorPool.getMaxExecutionTime() + " ms"); 
		HazelcastManager.printLog("  - Avg elapsed execution time: " + currentNodeDetails.getAvgElapsedTime() + " ms");
		HazelcastManager.printLog("**************************************************"); 
		System.exit(0);	
	}
	
	private static void printParameters (final String title) {
		HazelcastManager.printLog("");
		HazelcastManager.printLog("**************************************************"); 
		HazelcastManager.printLog(title + " WorkerPool with the following parameters:"); 
		HazelcastManager.printLog("**************************************************"); 
		HazelcastManager.printLog("  - pool core size       : " + poolCoreSize); 
		HazelcastManager.printLog("  - pool max size        : " + poolMaxSize); 
		HazelcastManager.printLog("  - queue capacity       : " + queueCapacity); 
		HazelcastManager.printLog("  - timeout (secs)       : " + timeoutSecs); 
		HazelcastManager.printLog("  - number of tasks      : " + taskNumber); 
		HazelcastManager.printLog("  - task process (ms)    : " + processTime); 
		HazelcastManager.printLog("  - retry sleep (ms)     : " + retrySleepTime); 
		HazelcastManager.printLog("  - retry max attempts   : " + retryMaxAttempts);
		HazelcastManager.printLog("  - initial sleep (secs) : " + retryMaxAttempts); 
		HazelcastManager.printLog("  - monitor sleep (secs) : " + monitorSleep); 
		HazelcastManager.printLog("**************************************************");
	}
} 