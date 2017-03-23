import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

import datamodel.ClientDetails;
import datamodel.ExecutionTask;
import executionservices.RejectedExecutionHandlerImpl;
import executionservices.RunnableWorkerThread;
import executionservices.SystemLinkedBlockingQueue;
import executionservices.SystemMonitorThread;
import executionservices.SystemThreadPoolExecutor;
import utils.HazelcastInstanceUtils;
import utils.SystemUtils;

public class WorkerPoolMain {

	// Default parameter values 
	private static int poolCoreSize = 5;
	private static int poolMaxSize = 10; 
	private static int queueCapacity = 500; 
	private static int timeoutSecs = 50; 
	private static int processTime = 10; 
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
			SystemUtils.printLog("Not all parameters informed. Using default values"); 
			SystemUtils.printLog(""); 
			SystemUtils.printLog("Usage: java WorkerPool <pool core size> <pool max size> <queue capacity> <timeout (secs)> <task process (ms)> <retry sleep (ms)> <retry max attempts> <initial sleep (secs)> <monitor sleep (secs)> [<print details>]"); 
			SystemUtils.printLog("  Example: java WorkerPool 10 15 20 50 5000 5000 5 5 3 [false]"); 
			SystemUtils.printLog(""); 
			SystemUtils.printLog("System will continue processing until a task with " + HazelcastInstanceUtils.getStopProcessingSignal() + " content is received");
			SystemUtils.printLog(""); 
		} 

		SystemUtils.printLog("Waiting " + initialSleep + " secs to start...",true); 
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
		SystemLinkedBlockingQueue<Runnable> blockingQueue = new SystemLinkedBlockingQueue<Runnable>();		
		
		// Creating the ThreadPoolExecutor 
		SystemThreadPoolExecutor executorPool = new SystemThreadPoolExecutor(poolCoreSize, poolMaxSize, timeoutSecs, TimeUnit.SECONDS, blockingQueue, threadFactory, rejectionHandler); 
		
		// Create cluster node object
		long startTime = System.currentTimeMillis();

		HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
		
		ClientDetails clientDetails = new ClientDetails(
				""+System.currentTimeMillis(),
				SystemUtils.getHostName(),
				"PORT",
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
		SystemMonitorThread monitor = new SystemMonitorThread(executorPool, monitorSleep, "LOCAL"/*HazelcastManager.getInetAddress()+":"+HazelcastManager.getInetPort()*/); 
		Thread monitorThread = new Thread(monitor); 
		monitorThread.start(); 

		hzClient.getMap(HazelcastInstanceUtils.getMonitorMapName()).put(clientDetails.getUuid(),clientDetails);
		
		// Listen to Hazelcast tasks queue and submit work to the thread pool for each task 
		IQueue<ExecutionTask> hazelcastTaskQueue = hzClient.getQueue( HazelcastInstanceUtils.getTaskQueueName() );
		while ( true ) {
			/*
			 * Option to avoid getting additional tasks from Hazelcast distributed queue if there is no processing capacity available in the ThreadPool 
			 */
			if ((executorPool.getActiveCount() < executorPool.getMaximumPoolSize()) ||
//				(blockingQueue.remainingCapacity() > 0)) { // For ArrayBlockingQueue
				(blockingQueue.size() < queueCapacity)) { // For LinkedBlockingQueue 
				ExecutionTask executionTaskItem = hazelcastTaskQueue.take();
				if (printDetails) SystemUtils.printLog("Consumed: " + executionTaskItem.getTaskId() + " from Hazelcast Task Queue",true);
				if ( (HazelcastInstanceUtils.getStopProcessingSignal()).equals(executionTaskItem.getTaskType()) ) {
					SystemUtils.printLog("Detected " + HazelcastInstanceUtils.getStopProcessingSignal(), true);
					hzClient.getQueue(HazelcastInstanceUtils.getTaskQueueName()).put(new ExecutionTask(HazelcastInstanceUtils.getStopProcessingSignal()));
					break;
				}
				executorPool.execute(new RunnableWorkerThread(processTime,executionTaskItem,retrySleepTime,retryMaxAttempts, "LOCAL"/*HazelcastManager.getNodeId()*/, printDetails));
				taskNumber++;
			}
		}
		SystemUtils.printLog("Hazelcast consumer Finished",true);

		// Shut down the pool 
		SystemUtils.printLog("Shutting down executor pool...",true); 
		executorPool.shutdown(); 
		SystemUtils.printLog(taskNumber/*executorPool.getTaskCount()*/ + " tasks. No additional tasks will be accepted",true); 

		// Shut down the monitor thread 
		while (!executorPool.isTerminated()) { 
			SystemUtils.printLog("Waiting for all the Executor to terminate",true); 
			Thread.sleep(monitorSleep*1000); 
		} 

		SystemUtils.printLog("Executor terminated",true); 
		SystemUtils.printLog("Shutting down monitor thread...",true); 
		monitor.shutdown(); 
		SystemUtils.printLog("Shutting down monitor thread... done",true); 
		long stopTime = System.currentTimeMillis();

		//Remove NodeDetails from the monitorMap
		hzClient.getMap(HazelcastInstanceUtils.getMonitorMapName()).remove(clientDetails.getUuid());
		
		// Shutdown Hazelcast cluster node instance		
		SystemUtils.printLog("Shutting down hazelcast client...",true);
		hzClient.getLifecycleService().shutdown();
		
		// Print statistics
		printParameters ("Finished");
		SystemUtils.printLog("Results:"); 
		SystemUtils.printLog("**************************************************"); 
		SystemUtils.printLog("  - Start time  : " + new Timestamp(startTime)); 
		SystemUtils.printLog("  - Stop time   : " + new Timestamp(stopTime)); 

		long millis = stopTime - startTime;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days); 
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes); 
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		SystemUtils.printLog("  - Elapsed time: " + (stopTime - startTime) + " ms - (" + hours + " hrs " + minutes + " min " + seconds + " secs)"); 
		SystemUtils.printLog("**************************************************"); 
		SystemUtils.printLog("  - Min elapsed execution time: " + executorPool.getMinExecutionTime() + " ms"); 
		SystemUtils.printLog("  - Max elapsed execution time: " + executorPool.getMaxExecutionTime() + " ms"); 
		SystemUtils.printLog("  - Avg elapsed execution time: " + executorPool.getAvgExecutionTime() + " ms");
		SystemUtils.printLog("**************************************************"); 
		
		// Exit application
		System.exit(0);	
	}
	
	// Print worker pool execution parameters 
	private static void printParameters (final String title) {
		SystemUtils.printLog("");
		SystemUtils.printLog("**************************************************"); 
		SystemUtils.printLog(title + " WorkerPool with the following parameters:"); 
		SystemUtils.printLog("**************************************************"); 
		SystemUtils.printLog("  - pool core size       : " + poolCoreSize); 
		SystemUtils.printLog("  - pool max size        : " + poolMaxSize); 
		SystemUtils.printLog("  - queue capacity       : " + queueCapacity); 
		SystemUtils.printLog("  - timeout (secs)       : " + timeoutSecs); 
		SystemUtils.printLog("  - number of tasks      : " + taskNumber); 
		SystemUtils.printLog("  - task process (ms)    : " + processTime); 
		SystemUtils.printLog("  - retry sleep (ms)     : " + retrySleepTime); 
		SystemUtils.printLog("  - retry max attempts   : " + retryMaxAttempts);
		SystemUtils.printLog("  - initial sleep (secs) : " + initialSleep); 
		SystemUtils.printLog("  - monitor sleep (secs) : " + monitorSleep); 
		SystemUtils.printLog("**************************************************");
	}
} 