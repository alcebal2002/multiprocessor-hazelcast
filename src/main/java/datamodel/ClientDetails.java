package datamodel;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientDetails implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String uuid;
	private String inetAddres;
	private String inetPort;
	private boolean activeStatus = true;
	
	// Start up parameters
	private int poolCoreSize;
	private int poolMaxSize; 
	private int queueCapacity; 
	private int timeoutSecs; 
	private int processTime; 
	private int retrySleepTime; 
	private int retryMaxAttempts; 
	private int initialSleep; 
	private int monitorSleep; 

	private int taskNumber = 0; 
	private long startTime = 0L; 
	private long stopTime = 0L; 
	private long avgElapsedTime = 0L;

	private List<Long> elapsedArray;
	private int elapsedArraySize;
	private String csvFormat;

	
	/**
	 * @param uuid
	 * @param inetAddres
	 * @param inetPort
	 * @param poolCoreSize
	 * @param poolMaxSize
	 * @param queueCapacity
	 * @param timeoutSecs
	 * @param processTime
	 * @param retrySleepTime
	 * @param retryMaxAttempts
	 * @param initialSleep
	 * @param monitorSleep
	 * @param taskNumber
	 * @param startTime
	 * @param stopTime
	 * @param elapsedArray
	 */
	public ClientDetails(String uuid, String inetAddres, String inetPort, int poolCoreSize, int poolMaxSize,
			int queueCapacity, int timeoutSecs, int processTime, int retrySleepTime, int retryMaxAttempts,
			int initialSleep, int monitorSleep, int taskNumber, long startTime) {
		this.uuid = uuid;
		this.inetAddres = inetAddres;
		this.inetPort = inetPort;
		this.poolCoreSize = poolCoreSize;
		this.poolMaxSize = poolMaxSize;
		this.queueCapacity = queueCapacity;
		this.timeoutSecs = timeoutSecs;
		this.processTime = processTime;
		this.retrySleepTime = retrySleepTime;
		this.retryMaxAttempts = retryMaxAttempts;
		this.initialSleep = initialSleep;
		this.monitorSleep = monitorSleep;
		this.taskNumber = taskNumber;
		this.startTime = startTime;
		this.elapsedArray = Collections.synchronizedList(new ArrayList<Long>());
	}

	public final String getUuid() {
		return this.uuid;
	}
	public final void setNodeId(String uuid) {
		this.uuid = uuid;
	}
	public final boolean getActiveStatus() {
		return activeStatus;
	}
	public final void setActiveStatus(boolean status) {
		this.activeStatus = status;
	}
	public final String getInetAddres() {
		return inetAddres;
	}
	public final void setInetAddres(String inetAddres) {
		this.inetAddres = inetAddres;
	}
	public final String getInetPort() {
		return inetPort;
	}
	public final void setInetPort(String inetPort) {
		this.inetPort = inetPort;
	}
	public final int getPoolCoreSize() {
		return poolCoreSize;
	}
	public final void setPoolCoreSize(int poolCoreSize) {
		this.poolCoreSize = poolCoreSize;
	}
	public final int getPoolMaxSize() {
		return poolMaxSize;
	}
	public final void setPoolMaxSize(int poolMaxSize) {
		this.poolMaxSize = poolMaxSize;
	}
	public final int getQueueCapacity() {
		return queueCapacity;
	}
	public final void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	public final int getTimeoutSecs() {
		return timeoutSecs;
	}
	public final void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}
	public final int getProcessTime() {
		return processTime;
	}
	public final void setProcessTime(int processTime) {
		this.processTime = processTime;
	}
	public final int getRetrySleepTime() {
		return retrySleepTime;
	}
	public final void setRetrySleepTime(int retrySleepTime) {
		this.retrySleepTime = retrySleepTime;
	}
	public final int getRetryMaxAttempts() {
		return retryMaxAttempts;
	}
	public final void setRetryMaxAttempts(int retryMaxAttempts) {
		this.retryMaxAttempts = retryMaxAttempts;
	}
	public final int getInitialSleep() {
		return initialSleep;
	}
	public final void setInitialSleep(int initialSleep) {
		this.initialSleep = initialSleep;
	}
	public final int getMonitorSleep() {
		return monitorSleep;
	}
	public final void setMonitorSleep(int monitorSleep) {
		this.monitorSleep = monitorSleep;
	}
	public final int getTaskNumber() {
		return taskNumber;
	}
	public final void setTaskNumber(int taskNumber) {
		this.taskNumber = taskNumber;
	}
	public final long getStartTime() {
		return startTime;
	}
	public final String getStartTimeString() {
		return ((this.getStartTime()>0L)?(new Timestamp(this.getStartTime()).toString()):" - ");
	}
	public final void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public final long getStopTime() {
		return stopTime;
	}
	public final String getStopTimeString() {
		return ((this.getStopTime()>0L)?(new Timestamp(this.getStopTime()).toString()):" - ");
	}
	public final void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	public final List<Long> getElapsedArray() {
		return elapsedArray;
	}
	public final int getElapsedArraySize() {
		this.elapsedArraySize = this.getElapsedArray().size();
		return elapsedArraySize;
	}
	public final void addElapsedTime(long elapsedTime) {
		this.elapsedArray.add(elapsedTime);
	}
	
	public final long getAvgElapsedTime() {
		return avgElapsedTime;
	}

	public final void setAvgElapsedTime(long avgElapsedTime) {
		this.avgElapsedTime = avgElapsedTime;
	}
	
	public final String getCsvFormat() {
		setCsvFormat(toCsvFormat ());
		return this.csvFormat;
	}
	public final void setCsvFormat(String csvFormat) {
		this.csvFormat = csvFormat;
	}
	
	public final String toCsvFormat () {
		return  this.getUuid() + ";" +
				this.getInetAddres() + ";" +
				this.getInetPort() + ";" +
				((this.getStopTime()>0L)?(new Timestamp(this.getStopTime())):" - ") + ";" +
				this.getElapsedArray().size() + ";"; 
	}
} 