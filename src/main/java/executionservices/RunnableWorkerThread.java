package executionservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import datamodel.ExecutionTask;
import datamodel.FxRate;
import utils.HazelcastInstanceUtils;

/*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements; 
*/

public class RunnableWorkerThread implements Runnable { 

	// Logger
	private static Logger logger = LoggerFactory.getLogger(RunnableWorkerThread.class);

	private ExecutionTask taskItem; 
	private int processTime; // used when simulating executions
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

	@SuppressWarnings("unchecked")
	private void processCommand() { 
		try { 
			// PUT HERE THE CODE OF THE COMMAND TO BE EXECUTED FOR EACH THREAD
			
			/*
			// Example: Jsoup Web Crawler
			
			String url = "http://www.yahoo.es";
			Document doc = Jsoup.connect(url).get();
	        Elements links = doc.select("a[href]"); 
    		System.out.println (" * Found " + links.size() + " links in " + url);
			*/
			
			
			// Example: Simulating execution time specified in processTime parameter
			/*
			long counter = 0; 

			long startTime = System.currentTimeMillis(); 

			while ((System.currentTimeMillis() - startTime) < processTime) { 
				counter++; 
				if (counter == Long.MAX_VALUE) { 
					counter=0; 
				} 
			}
			*/
			FxRate originalFxRate = (FxRate)taskItem.getContent();
			int positionId = originalFxRate.getPositionId();
			String currencyPair = originalFxRate.getCurrencyPair();
			float opening = originalFxRate.getOpen();
			
			logger.debug ("Processing " + currencyPair + "-" + positionId);
			
			List<FxRate> fxList = (List<FxRate>) HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getHistoricalMapName()).get(currencyPair);
			FxRate targetFxRate = null;
			String previousFound = "";
			
			Map<String,Integer> mapResults = new HashMap<String,Integer>();
			
			int countUp = 0;
			int countDown = 0;

			for (int i=positionId+1; i<fxList.size(); i++) {
				targetFxRate = fxList.get(i);
				logger.debug ("Comparing against " + targetFxRate.getCurrencyPair() + "-" + targetFxRate.getPositionId());
				
				if (targetFxRate.getHigh() > opening * taskItem.getIncreasePercentage()) {
					if (previousFound.equals("down")) {
						break;
					}
					
					if (mapResults.containsKey(currencyPair+"-UP["+countUp+"]")) {
						mapResults.put(currencyPair+"-UP["+countUp+"]",mapResults.get(currencyPair+"-UP["+countUp+"]")+1);
					} else {
						mapResults.put(currencyPair+"-UP["+countUp+"]",1);
					}
					
					/*
					//if (mapUp.containsKey((""+countUp))) {
					if (HazelcastInstanceUtils.getMap(currencyPair+"-mapUp").containsKey((""+countUp))) {
						HazelcastInstanceUtils.putIntoMap(currencyPair+"-mapUp",(""+countUp),(Integer)HazelcastInstanceUtils.getFromMap(currencyPair+"-mapUp", (""+countUp))+1);
						//mapUp.put((""+countUp), mapUp.get((""+countUp)) + 1);
					} else {
						HazelcastInstanceUtils.putIntoMap(currencyPair+"-mapUp",(""+countUp),1);
						//mapUp.put((""+countUp),1);
					}
					*/
					
					previousFound = "up";
					opening = opening * taskItem.getIncreasePercentage();
					countUp++;
				} else if (targetFxRate.getLow() > opening * taskItem.getDecreasePercentage()) {
					if (previousFound.equals("up")) {
						break;
					}
					
					if (mapResults.containsKey(currencyPair+"-DOWN["+countDown+"]")) {
						mapResults.put(currencyPair+"-DOWN["+countDown+"]",mapResults.get(currencyPair+"-DOWN["+countDown+"]")+1);
					} else {
						mapResults.put(currencyPair+"-DOWN["+countDown+"]",1);
					}

					/*
					//if (mapDown.containsKey((""+countDown))) {
					if (HazelcastInstanceUtils.getMap(currencyPair+"-mapDown").containsKey((""+countDown))) {
						HazelcastInstanceUtils.putIntoMap(currencyPair+"-mapDown",(""+countDown),(Integer)HazelcastInstanceUtils.getFromMap(currencyPair+"-mapDown", (""+countDown))+1);
						//mapDown.put((""+countDown), mapDown.get((""+countDown)) + 1);
					} else {
						HazelcastInstanceUtils.putIntoMap(currencyPair+"-mapDown",(""+countDown),1);
					}
					*/
					previousFound = "down";
					opening = opening * taskItem.getDecreasePercentage();
					countDown++;			
				}
			}
			// Put results into Hazelcast Results Map 
			HazelcastInstanceUtils.putIntoQueue(HazelcastInstanceUtils.getResultsQueueName(), mapResults);
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