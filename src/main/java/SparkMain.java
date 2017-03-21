import static spark.Spark.get;
import static spark.Spark.halt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.IMap;

import datamodel.NodeDetails;
import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.Spark;
import utils.HazelcastManager;

public class SparkMain {

	@SuppressWarnings("deprecation")
	private static Configuration freemarkerConfig = new Configuration();

	public static void main(String[] args) {

		freemarkerConfig.setClassForTemplateLoading(SparkMain.class, HazelcastManager.getTemplatesPath());

		Spark.staticFileLocation(HazelcastManager.getPublicPath());
		
		get("/", (req, res) -> "Welcome to Spark !");
        get("/stop", (req, res) -> halt(401, "Go away!"));
        get("/monitor", (req, res) -> {
        	StringWriter writer = new StringWriter();
        	try {
				IMap<String,NodeDetails> monitorMap = HazelcastManager.getMap(HazelcastManager.getMonitorMapName());
//				if (monitorMap != null && monitorMap.size() > 0) {
				Map<String, Object> root = new HashMap<String, Object>();
				root.put( HazelcastManager.getMonitorMapName (), monitorMap );
				Template resultTemplate = freemarkerConfig.getTemplate(HazelcastManager.getResultTemplateFileName());
				resultTemplate.process(root, writer);
//				}
        	} catch (Exception ex) {
        		HazelcastManager.printLog("Exception: " + ex.getClass() + " - " + ex.getMessage());
        	}
			return writer;
        });
    }
}