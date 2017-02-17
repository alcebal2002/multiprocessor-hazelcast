import java.sql.Timestamp;

public class MyMonitorThread implements Runnable {
    private MyThreadPoolExecutor executor;
    private int seconds;
    private boolean run=true;

    private String inetAddress;

    public MyMonitorThread(MyThreadPoolExecutor executor, int delay, String inetAddress) {
        this.executor=executor;
        this.seconds=delay;
        this.inetAddress = inetAddress;
    }

    public void shutdown() {
        this.run=false;
    }

    @Override
    public void run() {
        while(run) {
					
			try {
				printLog (String.format("["+inetAddress+"][monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
					this.executor.getPoolSize(),
					this.executor.getCorePoolSize(),
					this.executor.getActiveCount(),
					this.executor.getCompletedTaskCount(),
					this.executor.getTaskCount(),
					this.executor.isShutdown(),
					this.executor.isTerminated()),true);
				Thread.sleep(seconds*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
    
	private static void printLog (final String textToPrint, final boolean includeTimeStamp) {
		
		System.out.println (includeTimeStamp?((new Timestamp((new java.util.Date()).getTime())) + " - " + textToPrint):textToPrint);
	}
}