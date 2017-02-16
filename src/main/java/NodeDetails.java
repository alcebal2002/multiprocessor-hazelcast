import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeDetails implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String nodeId;
	private String inetAddres;
	private int inetPort;

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
	private List<Long> elapsedArray;

	
	/**
	 * @param nodeId
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
	public NodeDetails(String nodeId, String inetAddres, int inetPort, int poolCoreSize, int poolMaxSize,
			int queueCapacity, int timeoutSecs, int processTime, int retrySleepTime, int retryMaxAttempts,
			int initialSleep, int monitorSleep, int taskNumber, long startTime) {
		this.nodeId = nodeId;
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

	protected final String getNodeId() {
		return this.nodeId;
	}
	protected final void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	protected final String getInetAddres() {
		return inetAddres;
	}
	protected final void setInetAddres(String inetAddres) {
		this.inetAddres = inetAddres;
	}
	protected final int getInetPort() {
		return inetPort;
	}
	protected final void setInetPort(int inetPort) {
		this.inetPort = inetPort;
	}
	protected final int getPoolCoreSize() {
		return poolCoreSize;
	}
	protected final void setPoolCoreSize(int poolCoreSize) {
		this.poolCoreSize = poolCoreSize;
	}
	protected final int getPoolMaxSize() {
		return poolMaxSize;
	}
	protected final void setPoolMaxSize(int poolMaxSize) {
		this.poolMaxSize = poolMaxSize;
	}
	protected final int getQueueCapacity() {
		return queueCapacity;
	}
	protected final void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	protected final int getTimeoutSecs() {
		return timeoutSecs;
	}
	protected final void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}
	protected final int getProcessTime() {
		return processTime;
	}
	protected final void setProcessTime(int processTime) {
		this.processTime = processTime;
	}
	protected final int getRetrySleepTime() {
		return retrySleepTime;
	}
	protected final void setRetrySleepTime(int retrySleepTime) {
		this.retrySleepTime = retrySleepTime;
	}
	protected final int getRetryMaxAttempts() {
		return retryMaxAttempts;
	}
	protected final void setRetryMaxAttempts(int retryMaxAttempts) {
		this.retryMaxAttempts = retryMaxAttempts;
	}
	protected final int getInitialSleep() {
		return initialSleep;
	}
	protected final void setInitialSleep(int initialSleep) {
		this.initialSleep = initialSleep;
	}
	protected final int getMonitorSleep() {
		return monitorSleep;
	}
	protected final void setMonitorSleep(int monitorSleep) {
		this.monitorSleep = monitorSleep;
	}
	protected final int getTaskNumber() {
		return taskNumber;
	}
	protected final void setTaskNumber(int taskNumber) {
		this.taskNumber = taskNumber;
	}
	protected final long getStartTime() {
		return startTime;
	}
	protected final void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	protected final long getStopTime() {
		return stopTime;
	}
	protected final void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	protected final List<Long> getElapsedArray() {
		return elapsedArray;
	}
	protected final void addElapsedTime(long elapsedTime) {
		this.elapsedArray.add(elapsedTime);
	} 
} 