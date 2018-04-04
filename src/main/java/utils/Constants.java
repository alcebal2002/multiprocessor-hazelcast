package utils;

public class Constants {
	// Application properties file
	public static final String APPLICATION_PROPERTIES = "application.properties";
	
	// Controller Properties
	public static final String CONTROLLER_WRITE_RESULTS_TO_FILE = "controller.writeResultsToFile";
	public static final String CONTROLLER_MONITOR_DELAY = "controller.monitorDelay";
	public static final String CONTROLLER_LOAD_HISTORICAL_DATA = "controller.loadHistoricalData";
	public static final String CONTROLLER_HISTORICAL_DATA_PATH = "controller.historicalDataPath";
	public static final String CONTROLLER_HISTORICAL_DATA_FILE_NAME = "controller.historicalDataFileName";
	public static final String CONTROLLER_QUEUEPRODUCER_NUMBER_OF_TASKS = "controller.queueproducer.numberOfTasks";
	public static final String CONTROLLER_QUEUEPRODUCER_SLEEPTIME = "controller.queueproducer.sleepTime";
	public static final String CONTROLLER_QUEUEPRODUCER_SENDSTOPPROCESSINGSIGNAL = "controller.queueproducer.sendStopProcessingSignal";
	public static final String CONTROLLER_QUEUEPRODUCER_TASK_CONTENT = "{"+
		    "\"field1\": <counter>,"+
		    "\"field2\": \"value2\","+
		    "\"field3\": 12.50,"+
		    "\"field4\": [\"item4_1\", \"item4_2\"]"+
		"}";
	public static final String CONTROLLER_WORKER_LOG_FILE_PATTERN = "controller.worker.logFilePattern";
	public static final String CONTROLLER_WORKER_LOG_FILE_EXTENSION = "controller.worker.logFileExtension";
	
	// Worker Pool Properties
	public static final String WORKER_POOL_CORESIZE = "workerpool.coreSize";
	public static final String WORKER_POOL_MAXSIZE = "workerpool.maxSize"; 
	public static final String WORKER_POOL_QUEUE_CAPACITY = "workerpool.queueCapacity"; 
	public static final String WORKER_POOL_TIMEOUT_SECS = "workerpool.timeoutSecs"; 
	public static final String WORKER_POOL_PROCESS_TIME = "workerpool.processTime"; 
	public static final String WORKER_POOL_RETRY_SLEEP_TIME = "workerpool.retrySleepTime"; 
	public static final String WORKER_POOL_RETRY_MAX_ATTEMPTS = "workerpool.retryMaxAttempts"; 
	public static final String WORKER_POOL_INITIAL_SLEEP = "workerpool.initialSleep"; 
	public static final String WORKER_POOL_MONITOR_SLEEP = "workerpool.monitorSleep";
	public static final String WORKER_POOL_REFRESH_AFTER = "workerpool.refreshAfter";
	
	// Spark Properties
	public static final String SPARK_TEMPLATE_PATH = "spark.templatePath";
	public static final String SPARK_PUBLIC_PATH = "spark.publicPath";
	public static final String SPARK_TEMPLATE_FILE_NAME = "spark.templateFileName";
	public static final String SPARK_WELCOME_MESSAGE = "Welcome to Spark !";
	public static final String SPARK_BYE_MESSAGE = "Go away!";
	
	// Chart.js
	public static final String[] CHART_DATA_COLORS = {"red","orange","yellow","green","blue","purple","grey"};
	
	// Hazelcast Properties
	// Queue instance names
	public static final String HZ_TASK_QUEUE_NAME = "hz.taskQueueName";
	
	// List instance names
	public static final String HZ_HISTORICAL_DATA_LIST_NAME = "hz.historicalDataListName";
	
	// Map instance names
	public static final String HZ_MONITOR_MAP_NAME = "hz.monitorMapName";
	public static final String HZ_STATUS_MAP_NAME = "hz.statusMapName";
	public static final String HZ_STATUS_ENTRY_KEY = "status";
	
	public static final String HZ_STATUS_LOADING_HISTORICAL_DATA = "Loading Historical Data";
	public static final String HZ_STATUS_PUBLISHING_TASKS = "Publishing Tasks";
	public static final String HZ_STATUS_WAITING_TO_START_MONITORING = "Waiting to start Monitoring";
	public static final String HZ_STATUS_PROCESSING_TASKS = "Processing Tasks";
	public static final String HZ_STATUS_SHUTTING_DOWN = "Shutting down";
	public static final String HZ_STATUS_PROCESS_COMPLETED = "Process Completed";
	
	// Stop process signal
	public static final String HZ_STOP_PROCESSING_SIGNAL = "hz.stopProcessingSignal";

	
/*
	// Historical data headers
	private static final String[] historicalListHeader = {"Date","USD","JPY","BGN","CYP",
														  "CZK","DKK","EEK","GBP","HUF",
														  "LTL","LVL","MTL","PLN","ROL",
														  "RON","SEK","SIT","SKK","CHF",
														  "ISK","NOK","HRK","RUB","TRL",
														  "TRY","AUD","BRL","CAD","CNY",
														  "HKD","IDR","INR","KRW","MXN",
														  "MYR","NZD","PHP","SGD","THB",
														  "ZAR","ILS"};
	private static final String[] inscopeCurrencyList = {"USD","JPY","GBP"};
*/
}
