import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.IQueue;

import datamodel.ExecutionTask;
import datamodel.NodeDetails;
import executionservices.RejectedExecutionHandlerImpl;
import executionservices.RunnableWorkerThread;
import executionservices.SystemLinkedBlockingQueue;
import executionservices.SystemMonitorThread;
import executionservices.SystemThreadPoolExecutor;
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
	private static boolean printDetails = true; 
	
	public static void main(String args[]) throws InterruptedException { 

		if (args != null && args.length >= 9) { 
			poolCoreSize = Integer.parseInt(args[0]); 
			poolMaxSize = Integer.parseInt(args[1]); 
			queueCapacity = Integer.parseInt(args[2]); 
			timeoutSecs = Integer.parseInt(args[3]); 
			processTime = Integer.parseInt(args[4]); 
			retrySleepTime = Integer.parseInt(args[5]); 
			retryMaxAttempts = Integer.parseInt(args[6]); 
			initialSleep = Integer.parseInt(args[7]); 
			monitorSleep = Integer.parseInt(args[8]);
			if (args.length == 10 && "false".equalsIgnoreCase((String)args[9])) {
				printDetails = false;
			}
		} else { 
			HazelcastManager.printLog("Not all parameters informed. Using default values"); 
			HazelcastManager.printLog(""); 
			HazelcastManager.printLog("Usage: java WorkerPool <pool core size> <pool max size> <queue capacity> <timeout (secs)> <task process (ms)> <retry sleep (ms)> <retry max attempts> <initial sleep (secs)> <monitor sleep (secs)> [<print details>]"); 
			HazelcastManager.printLog("  Example: java WorkerPool 10 15 20 50 5000 5000 5 5 3 [false]"); 
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
		
		/* Define the BlockingQueue. 
		 * ArrayBlockingQueue to set a fixed capacity queue
		 * LinkedBlockingQueue to set an unbound capacity queue
		*/
		// BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(queueCapacity);
		SystemLinkedBlockingQueue<Runnable> blockingQueue = new SystemLinkedBlockingQueue<Runnable>();		
		
		// Creating the ThreadPoolExecutor 
		SystemThreadPoolExecutor executorPool = new SystemThreadPoolExecutor(poolCoreSize, poolMaxSize, timeoutSecs, TimeUnit.SECONDS, blockingQueue, threadFactory, rejectionHandler); 
		
		// Create cluster node object
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
		
		// Listen to Hazelcast tasks queue and submit work to the thread pool for each task 
		IQueue<ExecutionTask> hazelcastTaskQueue = HazelcastManager.getInstance().getQueue( HazelcastManager.getTaskQueueName() );
		while ( true ) {
			/*
			 * Option to avoid getting additional tasks from Hazelcast distributed queue if there is no processing capacity available in the ThreadPool 
			 */
			if ((executorPool.getActiveCount() < executorPool.getMaximumPoolSize()) ||
//				(blockingQueue.remainingCapacity() > 0)) { // For ArrayBlockingQueue
				(blockingQueue.size() < queueCapacity)) { // For LinkedBlockingQueue 
				ExecutionTask executionTaskItem = hazelcastTaskQueue.take();
				if (printDetails) HazelcastManager.printLog("Consumed: " + executionTaskItem.getTaskId() + " from Hazelcast Task Queue",true);
				if ( (HazelcastManager.getStopProcessingSignal()).equals(executionTaskItem.getTaskType()) ) {
					HazelcastManager.printLog("Detected " + HazelcastManager.getStopProcessingSignal(), true);
					HazelcastManager.putStopSignalIntoQueue(HazelcastManager.getTaskQueueName());
					break;
				}
				executorPool.execute(new RunnableWorkerThread(processTime,executionTaskItem,retrySleepTime,retryMaxAttempts, HazelcastManager.getNodeId(), printDetails));
				taskNumber++;
			}
		}
		HazelcastManager.printLog("Hazelcast consumer Finished",true);

		// Shut down the pool 
		HazelcastManager.printLog("Shutting down executor pool...",true); 
		executorPool.shutdown(); 
		HazelcastManager.printLog(taskNumber/*executorPool.getTaskCount()*/ + " tasks. No additional tasks will be accepted",true); 

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

		// Update currrent cluster node status and statistics
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
		
		// Shutdown Hazelcast cluster node instance		
		HazelcastManager.printLog("Shutting down hazelcast client...",true);
		HazelcastManager.getInstance().getLifecycleService().shutdown();
		
		// Print statistics
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
		
		// Exit application
		System.exit(0);	
	}
	
	// Print worker pool execution parameters 
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
		HazelcastManager.printLog("  - initial sleep (secs) : " + initialSleep); 
		HazelcastManager.printLog("  - monitor sleep (secs) : " + monitorSleep); 
		HazelcastManager.printLog("**************************************************");
	}
} 