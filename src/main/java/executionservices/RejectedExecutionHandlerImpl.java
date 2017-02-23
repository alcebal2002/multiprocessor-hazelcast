package executionservices;
import java.sql.Timestamp;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		
		int numAttempts=((RunnableWorkerThread)r).getRetryMaxAttempts();
		int retrySleepTime=((RunnableWorkerThread)r).getRetrySleepTime();
		
		if (numAttempts > 0) {
			System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - " + r.toString() + " Rejected. Retry in " + retrySleepTime + " secs. Seeting retries to " + (numAttempts-1));
			((RunnableWorkerThread)r).setRetryMaxAttempts(numAttempts-1);
			try {
				Thread.sleep(retrySleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor.execute (r);
		} else {
			System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - " + r.toString() + " Discarded. No more retries");
		}
    }
}