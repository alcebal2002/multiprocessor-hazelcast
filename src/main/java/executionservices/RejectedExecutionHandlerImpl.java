package executionservices;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import utils.HazelcastManager;

public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
	
	private boolean printDetails = true;
	
	public RejectedExecutionHandlerImpl () {
	}
	
	public RejectedExecutionHandlerImpl (boolean printDetails) {
		this.printDetails = printDetails;
	}
	
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    	 try {
             /*
              * This does the actual put into the queue. Once the max threads have been reached, the tasks will then queue up.
              */
    		 //if (printDetails) HazelcastManager.printLog(r.toString() + " Rejected. Adding to the queue",true);
    		 executor.getQueue().put(r);
         } catch (InterruptedException e) {
        	 HazelcastManager.printLog(r.toString() + " Rejected and Discarded due to InterruptedException",true);
         }
    	
		 /*
		  * Option to send the task back to the Hazelcast distributed queue
		 */

/*        
        if (printDetails) HazelcastManager.printLog(r.toString() + " Rejected. Sending " + r.toString() + " back to Hazelcast " + HazelcastManager.getTaskQueueName() + " queue",true);
		
		try {
			HazelcastManager.getInstance().getQueue( HazelcastManager.getTaskQueueName()).put(((RunnableWorkerThread)r).getTaskItem());
		} catch (InterruptedException e) {
			if (printDetails) HazelcastManager.printLog(r.toString() + " Exception. Unable to send " + r.toString() + " back to the queue",true);
		}
*/		

		 /*
		  * Option to set numAttempts per RunnableWorkerThread
		 */
/*
    	int numAttempts=((RunnableWorkerThread)r).getRetryMaxAttempts();
		int retrySleepTime=((RunnableWorkerThread)r).getRetrySleepTime();

		if (numAttempts > 0) {
			if (printDetails) HazelcastManager.printLog(r.toString() + " Rejected. Retry in " + retrySleepTime + " ms. Updating retries to " + (numAttempts-1),true);
			((RunnableWorkerThread)r).setRetryMaxAttempts(numAttempts-1);
			
			try {
				Thread.sleep(retrySleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.execute (r);
		} else {
			if (printDetails) HazelcastManager.printLog(r.toString() + " Discarded. No more retries",true);
		}
*/
    }
}