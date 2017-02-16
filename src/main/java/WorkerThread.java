
import java.sql.Timestamp;

import com.hazelcast.core.IMap;

/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements; 
*/

public class WorkerThread implements Runnable { 

	private String taskItem; 
	private int processTime; 
	private int retrySleepTime; 
	private int retryMaxAttempts; 
	private long elapsedTimeMillis;
	private String nodeId;
	private static final String monitorMapName = "monitorMap";

	public WorkerThread(final int processTime, final String taskItem, final int retrySleepTime, final int retryMaxAttempts, final String nodeId) { 
		this.taskItem=taskItem; 
		this.processTime=processTime; 
		this.retrySleepTime=retrySleepTime; 
		this.retryMaxAttempts=retryMaxAttempts;
		this.nodeId = nodeId;
	} 

	@Override 
	public void run() { 
		printLog(Thread.currentThread().getName()+" Start. Command = "+taskItem,true); 
		long startTime = System.currentTimeMillis(); 

		processCommand(); 

		long stopTime = System.currentTimeMillis(); 
		elapsedTimeMillis = stopTime - startTime; 
		IMap<String, NodeDetails> monitorMap = HazelcastManager.getInstance().getMap(monitorMapName);
		
		monitorMap.lock(nodeId);
		NodeDetails nodeDetails = monitorMap.get(nodeId);
		nodeDetails.getElapsedArray().add(elapsedTimeMillis);
		monitorMap.put(nodeId,nodeDetails);
		monitorMap.unlock(nodeId);
		
		printLog(Thread.currentThread().getName()+" End. Command = "+taskItem+" ["+elapsedTimeMillis+"ms]",true); 
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
		return this.taskItem; 
	} 

	private static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
}