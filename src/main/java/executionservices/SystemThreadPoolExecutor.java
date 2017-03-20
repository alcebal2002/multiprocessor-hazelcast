package executionservices;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit; 
  
public class SystemThreadPoolExecutor extends ThreadPoolExecutor { 
        
    private long minExecutionTime = Long.MAX_VALUE; 
    private long maxExecutionTime = 0;

    public SystemThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, 
    							BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) { 
    	super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    } 

    protected void afterExecute (Runnable r, Throwable t ) { 
            
	    try { 
/*	            
		    long elapsedTimeMillis = ((RunnableWorkerThread)r).getElapsedTimeMillis(); 
		                    
		    if (elapsedTimeMillis < minExecutionTime) minExecutionTime = elapsedTimeMillis; 
		    if (elapsedTimeMillis > maxExecutionTime) maxExecutionTime = elapsedTimeMillis; 
*/	
	    } finally { 
	    	super.afterExecute(r, t); 
	    } 
    } 
    
    public long getMinExecutionTime () { 
    	return this.minExecutionTime; 
    } 
    
    public long getMaxExecutionTime () { 
    	return this.maxExecutionTime; 
    }
    
} 