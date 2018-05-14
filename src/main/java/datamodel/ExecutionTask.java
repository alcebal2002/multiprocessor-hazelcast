package datamodel;
import java.io.Serializable;

public class ExecutionTask implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String taskId = null;
	private String taskType = null;
	private int positionId = 0;
//	private String currencyPair = null;
//	private float open = 0;
	private float increasePercentage = 0;
	private float decreasePercentage = 0;
	private int maxLevels = 0;
	private Object content = null;
	private long creationTimestamp = 0L;
	private long elapsedExecutionTime = 0L;

	public ExecutionTask(String taskType) {
		this.taskType = taskType;
	}

	public ExecutionTask(final String taskId, final String taskType, final int positionId, final float increasePercentage, final float decreasePercentage, final int maxLevels, final Object content, final long creationTimestamp) {
		this.taskId = taskId;
		this.taskType = taskType;
		this.positionId = positionId;
		this.increasePercentage = increasePercentage;
		this.decreasePercentage = decreasePercentage;
		this.maxLevels = maxLevels;
		this.content = content;
		this.creationTimestamp = creationTimestamp;
	}

	public final String getTaskId() {
		return this.taskId;
	}
	public final void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public final String getTaskType() {
		return this.taskType;
	}
	public final void setTaskType (String taskType) {
		this.taskType = taskType;
	}

	public final int getPositionId() {
		return this.positionId;
	}
	public final void setPositionId (int positionId) {
		this.positionId = positionId;
	}

	public final float getIncreasePercentage() {
		return this.increasePercentage;
	}
	public final void setIncreasePercentage (float increasePercentage) {
		this.increasePercentage = increasePercentage;
	}

	public final float getDecreasePercentage() {
		return this.decreasePercentage;
	}
	public final void setDecreasePercentage (float decreasePercentage) {
		this.decreasePercentage = decreasePercentage;
	}

	public final int getMaxLevels() {
		return this.maxLevels;
	}
	public final void setMaxLevels (int maxLevels) {
		this.maxLevels = maxLevels;
	}

	public final Object getContent () {
		return this.content;
	}
	public final void setContent (String content) {
		this.content = content;
	}

	public final long getCreationTimestamp() {
		return creationTimestamp;
	}
	public final void setCreationTimestamp (long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public final long getElapsedExecutionTime() {
		return elapsedExecutionTime;
	}
	public final void setElapsedExecutionTime (long elapsedTime) {
		this.elapsedExecutionTime = elapsedTime;
	}
} 