import static spark.Spark.get;
import static spark.Spark.halt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

import datamodel.WorkerDetail;
import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.Spark;
import utils.HazelcastInstanceUtils;
import utils.SystemUtils;

public class SparkMain {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(SparkMain.class);

	@SuppressWarnings("deprecation")
	private static Configuration freemarkerConfig = new Configuration();

	public static void main(String[] args) {
		
		logger.info("Starting SPARK REST Framework");

		freemarkerConfig.setClassForTemplateLoading(SparkMain.class, SystemUtils.getTemplatesPath());

		Spark.staticFileLocation(SystemUtils.getPublicPath());
		
		get("/", (req, res) -> "Welcome to Spark !");
        get("/stop", (req, res) -> halt(401, "Go away!"));
        get("/monitor", (req, res) -> {
        	StringWriter writer = new StringWriter();
        	try {
        		
        		HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();
//				if (monitorMap != null && monitorMap.size() > 0) {

    			boolean refreshPage = false;

    			Iterator<Entry<String, WorkerDetail>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();

    			while (iter.hasNext()) {
    	            Entry<String, WorkerDetail> entry = iter.next();
    	            if (entry.getValue().getActiveStatus()) refreshPage = true;
    	        }
    			
        		Map<String, Object> root = new HashMap<String, Object>();
				root.put( "refreshPage", refreshPage );
        		root.put( HazelcastInstanceUtils.getMonitorMapName (), hzClient.getMap(HazelcastInstanceUtils.getMonitorMapName()) );
				Template resultTemplate = freemarkerConfig.getTemplate(SystemUtils.getResultTemplateFileName());
				resultTemplate.process(root, writer);
//				}
        	} catch (Exception ex) {
        		 logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
        	}
			return writer;
        });
    }
}