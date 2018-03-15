package utils;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
	
	private static Properties applicationProperties;
	
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
	
	// Hazelcast Properties
	// Queue instance names
	public static final String HZ_TASK_QUEUE_NAME = "hz.taskQueueName";
	
	// List instance names
	public static final String HZ_HISTORICAL_DATA_LIST_NAME = "hz.historicalDataListName";
	
	// Map instance names
	public static final String HZ_MONITOR_MAP_NAME = "hz.monitorMapName";
	
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
	
	public static String getStringProperty (final String properyName) 
		throws Exception {
		
		return getProperty(properyName);
	}
	
	public static int getIntProperty (final String properyName) 
		throws Exception {
		
		return Integer.parseInt(getProperty(properyName));
	}
	
	public static boolean getBooleanProperty (final String properyName) 
		throws Exception {
			
		return Boolean.parseBoolean(getProperty(properyName));
	}
	
	public static String getProperty (final String properyName) 
		throws Exception {
		
		if (applicationProperties == null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
	
			try(InputStream resourceStream = loader.getResourceAsStream(APPLICATION_PROPERTIES)){
				applicationProperties = new Properties();
				applicationProperties.load(resourceStream);
			} catch (Exception ex) {
				logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
				throw ex;
			}
		}
		return applicationProperties.getProperty(properyName);
	}
}
