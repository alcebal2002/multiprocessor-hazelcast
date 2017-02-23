package executionservices;

import com.hazelcast.core.IMap;

import datamodel.ExecutionTask;
import datamodel.NodeDetails;
import utils.HazelcastManager;

/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements; 
*/

public class RunnableWorkerThread implements Runnable { 

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
		HazelcastManager.printLog(Thread.currentThread().getName()+" Start. Command = "+taskItem,true); 
		long startTime = System.currentTimeMillis(); 

		processCommand(); 

		long stopTime = System.currentTimeMillis(); 
		elapsedTimeMillis = stopTime - startTime; 
		IMap<String, NodeDetails> monitorMap = HazelcastManager.getInstance().getMap(HazelcastManager.getMonitorMapName());
		
		monitorMap.lock(nodeId);
		NodeDetails nodeDetails = monitorMap.get(nodeId);
		nodeDetails.getElapsedArray().add(elapsedTimeMillis);
		monitorMap.put(nodeId,nodeDetails);
		monitorMap.unlock(nodeId);
		
		HazelcastManager.printLog(Thread.currentThread().getName()+" End. Command = "+taskItem+" ["+elapsedTimeMillis+"ms]",true); 
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


	public void setRetryMaxAttempts (int data) { 
		this.retryMaxAttempts=data; 
	} 

	@Override 
	public String toString(){ 
		return this.taskItem.getTaskId(); 
	} 
}