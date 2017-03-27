import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	// Logger
	private static Logger logger = LoggerFactory.getLogger(WorkerPoolMain.class);
	
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
	private static String nodeId;
	
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
		} else { 
			logger.info ("Not all parameters informed. Using default values"); 
			logger.info (""); 
			logger.info ("Usage: java WorkerPool <pool core size> <pool max size> <queue capacity> <timeout (secs)> <task process (ms)> <retry sleep (ms)> <retry max attempts> <initial sleep (secs)> <monitor sleep (secs)>"); 
			logger.info ("  Example: java WorkerPool 10 15 20 50 5000 5000 5 5 3"); 
			logger.info (""); 
			logger.info ("System will continue processing until a task with " + HazelcastInstanceUtils.getStopProcessingSignal() + " content is received");
			logger.info (""); 
		} 

		logger.info ("Waiting " + initialSleep + " secs to start..."); 
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
		
		nodeId = ""+System.currentTimeMillis();
		
		ClientDetails clientDetails = new ClientDetails(
				nodeId,
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
		SystemMonitorThread monitor = new SystemMonitorThread(executorPool, monitorSleep, nodeId); 
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
				logger.debug ("Consumed: " + executionTaskItem.getTaskId() + " from Hazelcast Task Queue");
				if ( (HazelcastInstanceUtils.getStopProcessingSignal()).equals(executionTaskItem.getTaskType()) ) {
					logger.info ("Detected " + HazelcastInstanceUtils.getStopProcessingSignal());
					hzClient.getQueue(HazelcastInstanceUtils.getTaskQueueName()).put(new ExecutionTask(HazelcastInstanceUtils.getStopProcessingSignal()));
					break;
				}
				executorPool.execute(new RunnableWorkerThread(processTime,executionTaskItem,retrySleepTime,retryMaxAttempts,nodeId));
				taskNumber++;
			}
		}
		logger.info ("Hazelcast consumer Finished");

		// Shut down the pool 
		logger.info ("Shutting down executor pool..."); 
		executorPool.shutdown(); 
		logger.info (taskNumber/*executorPool.getTaskCount()*/ + " tasks. No additional tasks will be accepted"); 

		// Shut down the monitor thread 
		while (!executorPool.isTerminated()) { 
			logger.info ("Waiting for all the Executor to terminate"); 
			Thread.sleep(monitorSleep*1000); 
		} 

		logger.info ("Executor terminated"); 
		logger.info ("Shutting down monitor thread..."); 
		monitor.shutdown(); 
		logger.info ("Shutting down monitor thread... done"); 
		long stopTime = System.currentTimeMillis();

		// Update ClientDetails status to inactive
		clientDetails.setActiveStatus(false);
		hzClient.getMap(HazelcastInstanceUtils.getMonitorMapName()).put(clientDetails.getUuid(),clientDetails);
		
		// Shutdown Hazelcast cluster node instance		
		logger.info ("Shutting down hazelcast client...");
		hzClient.getLifecycleService().shutdown();
		
		// Print statistics
		printParameters ("Finished");
		logger.info ("Results:"); 
		logger.info ("**************************************************"); 
		logger.info ("  - Start time  : " + new Timestamp(startTime)); 
		logger.info ("  - Stop time   : " + new Timestamp(stopTime)); 

		long millis = stopTime - startTime;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days); 
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes); 
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		logger.info ("  - Elapsed time: " + (stopTime - startTime) + " ms - (" + hours + " hrs " + minutes + " min " + seconds + " secs)"); 
		logger.info ("**************************************************"); 
		logger.info ("  - Min elapsed execution time: " + executorPool.getMinExecutionTime() + " ms"); 
		logger.info ("  - Max elapsed execution time: " + executorPool.getMaxExecutionTime() + " ms"); 
		logger.info ("  - Avg elapsed execution time: " + executorPool.getAvgExecutionTime() + " ms");
		logger.info ("**************************************************"); 
		
		// Exit application
		System.exit(0);	
	}
	
	// Print worker pool execution parameters 
	private static void printParameters (final String title) {
		logger.info ("");
		logger.info ("**************************************************"); 
		logger.info (title + " WorkerPool with the following parameters:"); 
		logger.info ("**************************************************"); 
		logger.info ("  - pool core size       : " + poolCoreSize); 
		logger.info ("  - pool max size        : " + poolMaxSize); 
		logger.info ("  - queue capacity       : " + queueCapacity); 
		logger.info ("  - timeout (secs)       : " + timeoutSecs); 
		logger.info ("  - number of tasks      : " + taskNumber); 
		logger.info ("  - task process (ms)    : " + processTime); 
		logger.info ("  - retry sleep (ms)     : " + retrySleepTime); 
		logger.info ("  - retry max attempts   : " + retryMaxAttempts);
		logger.info ("  - initial sleep (secs) : " + initialSleep); 
		logger.info ("  - monitor sleep (secs) : " + monitorSleep); 
		logger.info ("**************************************************");
	}
} 