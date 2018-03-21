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
import utils.ApplicationProperties;
import utils.Constants;
import utils.HazelcastInstanceUtils;

public class SparkMain {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(SparkMain.class);

	@SuppressWarnings("deprecation")
	private static Configuration freemarkerConfig = new Configuration();

	public static void main(String[] args) throws Exception {
		
		logger.info("Starting SPARK REST Framework");

		freemarkerConfig.setClassForTemplateLoading(SparkMain.class, ApplicationProperties.getStringProperty(Constants.SPARK_TEMPLATE_PATH));

		Spark.staticFileLocation(ApplicationProperties.getStringProperty(Constants.SPARK_PUBLIC_PATH));
		
		get("/", (req, res) -> ApplicationProperties.getStringProperty(Constants.SPARK_WELCOME_MESSAGE));
        get("/stop", (req, res) -> halt(401, ApplicationProperties.getStringProperty(Constants.SPARK_BYE_MESSAGE)));
        get("/monitor", (req, res) -> {
        	StringWriter writer = new StringWriter();
        	
        	try {
        		
        		HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();

    			boolean refreshPage = false;

    			Iterator<Entry<String, WorkerDetail>> iter = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName()).entrySet().iterator();

    			while (iter.hasNext()) {
    	            Entry<String, WorkerDetail> entry = iter.next();
    	            if (entry.getValue().getActiveStatus()) refreshPage = true;
    	            
    	        }
    			
        		Map<String, Object> root = new HashMap<String, Object>();
				root.put( "refreshPage", refreshPage );
        		root.put( HazelcastInstanceUtils.getMonitorMapName (), hzClient.getMap(HazelcastInstanceUtils.getMonitorMapName()) );
				Template resultTemplate = freemarkerConfig.getTemplate(ApplicationProperties.getStringProperty(Constants.SPARK_TEMPLATE_FILE_NAME));
				resultTemplate.process(root, writer);
    		} catch (Exception ex) {
        		 logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
        	}
			return writer;
        });
    }
}