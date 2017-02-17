import static spark.Spark.get;
import static spark.Spark.halt;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.IMap;

public class SparkMain {

	public static void main(String[] args) {
        get("/", (req, res) -> "Welcome to Spark !");
        get("/stop", (req, res) -> halt(401, "Go away!"));
        get("/view", (req, res) -> "view");
        
        get("/monitor", (req, res) -> {
        	
    		List<Long> elapsedArrayList;
    		long avgElapsedTime;
    		String result = "No " + HazelcastManager.getMonitorMapName() + " found<br/>";
    		
			IMap<String,NodeDetails> monitorMap = HazelcastManager.getInstance().getMap(HazelcastManager.getMonitorMapName());
			if (monitorMap != null && monitorMap.size() > 0) {
				result = "Node;Start Time;Stop Time;# Tasks processed;Avg time<br/>";
				for (Map.Entry<String,NodeDetails> nodeEntry : monitorMap.entrySet()) {

					elapsedArrayList = nodeEntry.getValue().getElapsedArray();
					
					avgElapsedTime = 0L;
					if (elapsedArrayList.size() > 0) {
						for (int i=0; i < elapsedArrayList.size(); i++) {
							avgElapsedTime += elapsedArrayList.get(i);
						}
						avgElapsedTime = avgElapsedTime / elapsedArrayList.size();
					}
					
					result += nodeEntry.getValue().getInetAddres() + ":" +  nodeEntry.getValue().getInetPort() + ";" +
							new Timestamp(nodeEntry.getValue().getStartTime()) + ";" +
							((nodeEntry.getValue().getStopTime()>0L)?(new Timestamp(nodeEntry.getValue().getStopTime())):" - ") + ";" +
							elapsedArrayList.size() + ";" + 
							avgElapsedTime+"<br/>";
				}
			}
        	return (result);
        });
    }
}