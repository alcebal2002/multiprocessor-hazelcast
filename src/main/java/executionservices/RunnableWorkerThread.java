package executionservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datamodel.ExecutionTask;

/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements; 
*/

public class RunnableWorkerThread implements Runnable { 

	// Logger
	private static Logger logger = LoggerFactory.getLogger(RunnableWorkerThread.class);

	private ExecutionTask taskItem; 
	private int processTime; 
	private int retrySleepTime; 
	private int retryMaxAttempts; 
	private long elapsedTimeMillis;
	private String nodeId;

	public RunnableWorkerThread(final int processTime, final ExecutionTask taskItem, final int retrySleepTime, final int retryMaxAttempts, final String nodeId) { 
		this.taskItem=taskItem; 
		this.processTime=processTime; 
		this.retrySleepTime=retrySleepTime; 
		this.retryMaxAttempts=retryMaxAttempts;
		this.nodeId = nodeId;
	} 

	@Override 
	public void run() {
		logger.debug (Thread.currentThread().getName()+" Start. Command = "+taskItem.getTaskId()); 
		long startTime = System.currentTimeMillis(); 

		processCommand(); 

		long stopTime = System.currentTimeMillis(); 
		elapsedTimeMillis = stopTime - startTime;
		taskItem.setElapsedExecutionTime(elapsedTimeMillis);
		logger.debug (Thread.currentThread().getName()+" End. Command = "+taskItem.getTaskId()+" ["+elapsedTimeMillis+"ms]");
	} 

	private void processCommand() { 
		try { 
			// PUT HERE THE CODE OF THE COMMAND TO BE EXECUTED FOR EACH THREAD 
			//Thread.sleep(processTime);
/*
			String url = "http://www.yahoo.es";
			Document doc = Jsoup.connect(url).get();
	        Elements links = doc.select("a[href]"); 
    		System.out.println (" * Found " + links.size() + " links in " + url);
*/
			long counter = 0; 

			long startTime = System.currentTimeMillis(); 

			while ((System.currentTimeMillis() - startTime) < processTime) { 
				counter++; 
				if (counter == Long.MAX_VALUE) { 
					counter=0; 
				} 
			} 

		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 

	public int getRetrySleepTime () { 
		return this.retrySleepTime; 
	} 

	public int getRetryMaxAttempts () { 
		return this.retryMaxAttempts; 
	} 
	
	public long getElapsedTimeMillis () { 
		return this.elapsedTimeMillis; 
	}
	
	public String getNodeId () {
		return this.nodeId;
	}


	public void setRetryMaxAttempts (int data) { 
		this.retryMaxAttempts=data; 
	} 

	public final ExecutionTask getTaskItem() {
		return taskItem;
	}

	public final void setTaskItem(ExecutionTask taskItem) {
		this.taskItem = taskItem;
	}

	@Override 
	public String toString(){ 
		return this.taskItem.getTaskId(); 
	}
	
}