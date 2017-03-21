package executionservices;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit; 
  
public class SystemThreadPoolExecutor extends ThreadPoolExecutor { 
        
    private ArrayList<Long> executionArray = new ArrayList<Long>();
	
	private long minExecutionTime = Long.MAX_VALUE; 
    private long maxExecutionTime = 0;

    public SystemThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
    							BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) { 
    	super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    } 

    protected void afterExecute (Runnable r, Throwable t ) { 
            
	    try {
		    long elapsedTimeMillis = ((RunnableWorkerThread)r).getElapsedTimeMillis(); 
	    	executionArray.add(elapsedTimeMillis);
		                    
		    if (elapsedTimeMillis < minExecutionTime) minExecutionTime = elapsedTimeMillis; 
		    if (elapsedTimeMillis > maxExecutionTime) maxExecutionTime = elapsedTimeMillis;
		    
	
	    } finally { 
	    	super.afterExecute(r, t); 
	    } 
    } 
    
    public int getNumExecutions () { 
    	return this.executionArray.size(); 
    } 

    public long getMinExecutionTime () { 
    	return this.minExecutionTime; 
    } 
    
    public long getMaxExecutionTime () { 
    	return this.maxExecutionTime; 
    }
    
    public long getAvgExecutionTime () {
    	long avgExecutionTime = 0;
    	if (executionArray.size()>0) {
	    	for (int i=0; i < executionArray.size(); i++) {
				avgExecutionTime += executionArray.get(i);
			}
			avgExecutionTime = avgExecutionTime / executionArray.size();
    	}
    	return avgExecutionTime; 
    } 
} 