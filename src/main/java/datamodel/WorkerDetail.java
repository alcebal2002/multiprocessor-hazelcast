package datamodel;
import java.io.Serializable;
import java.sql.Timestamp;

public class WorkerDetail implements Serializable {
	
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
	private int refreshAfter;

	private int taskNumber = 0; 
	private long startTime = 0L; 
	private long stopTime = 0L;
	private long totalElapsedTime = 0L; 
	private int totalExecutions = 0;
	private long avgExecutionTime = 0L;

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
	 * @param refreshAfter
	 * @param taskNumber
	 * @param startTime
	 * @param stopTime
	 * @param elapsedArray
	 */
	public WorkerDetail(String uuid, String inetAddres, String inetPort, int poolCoreSize, int poolMaxSize,
			int queueCapacity, int timeoutSecs, int processTime, int retrySleepTime, int retryMaxAttempts,
			int initialSleep, int monitorSleep, int refreshAfter, int taskNumber, long startTime) {
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
		this.refreshAfter = refreshAfter;
		this.taskNumber = taskNumber;
		this.startTime = startTime;
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
	public final int getRefreshAfter() {
		return refreshAfter;
	}
	public final void setRefreshAter(int refreshAfter) {
		this.refreshAfter = refreshAfter;
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

	public final long getAvgExecutionTime() {
		return avgExecutionTime;
	}

	public final void setAvgExecutionTime(long avgExecutionTime) {
		this.avgExecutionTime = avgExecutionTime;
	}

	public final int getTotalExecutions() {
		return totalExecutions;
	}

	public final void setTotalExecutions(int totalExecutions) {
		this.totalExecutions = totalExecutions;
	}

	public final long getTotalElapsedTime() {
		return totalElapsedTime;
	}

	public final void setTotalElapsedTime(long totalElapsedTime) {
		this.totalElapsedTime = totalElapsedTime;
	}
/*
	public final String getCsvFormat() {
		setCsvFormat(toCsvFormat ());
		return this.csvFormat;
	}
	public final void setCsvFormat(String csvFormat) {
		this.csvFormat = csvFormat;
	}
*/
	public final String toCsvFormat () {
		return  this.getUuid() + ";" +
				this.getInetAddres() + ";" +
				this.getInetPort() + ";" +
				this.getStartTimeString() + ";" +
				this.getStopTimeString() + ";" +
				this.getTotalElapsedTime() + ";" +
				this.getTotalExecutions();
				//((this.getStopTime()>0L)?(new Timestamp(this.getStopTime())):" - ") + ";"; 
	}
} 