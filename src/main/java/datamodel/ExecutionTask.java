package datamodel;
import java.io.Serializable;

public class ExecutionTask implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String taskId = null;
	private String taskType = null;
	private String content = null;
	private long creationTimestamp = 0L;
	private long elapsedExecutionTime = 0L;

	public ExecutionTask(String taskType) {
		this.taskType = taskType;
	}

	public ExecutionTask(String taskId, String taskType, String content, long creationTimestamp) {
		this.taskId = taskId;
		this.taskType = taskType;
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

	public final String getContent () {
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