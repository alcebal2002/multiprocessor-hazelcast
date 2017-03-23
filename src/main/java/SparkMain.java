import static spark.Spark.get;
import static spark.Spark.halt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.IMap;

import datamodel.ClientDetails;
import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.Spark;
import utils.HazelcastInstanceUtils;
import utils.SystemUtils;

public class SparkMain {

	@SuppressWarnings("deprecation")
	private static Configuration freemarkerConfig = new Configuration();

	public static void main(String[] args) {

		freemarkerConfig.setClassForTemplateLoading(SparkMain.class, SystemUtils.getTemplatesPath());

		Spark.staticFileLocation(SystemUtils.getPublicPath());
		
		get("/", (req, res) -> "Welcome to Spark !");
        get("/stop", (req, res) -> halt(401, "Go away!"));
        get("/monitor", (req, res) -> {
        	StringWriter writer = new StringWriter();
        	try {
				IMap<String,ClientDetails> monitorMap = HazelcastInstanceUtils.getMap(HazelcastInstanceUtils.getMonitorMapName());
//				if (monitorMap != null && monitorMap.size() > 0) {
				Map<String, Object> root = new HashMap<String, Object>();
				root.put( HazelcastInstanceUtils.getMonitorMapName (), monitorMap );
				Template resultTemplate = freemarkerConfig.getTemplate(SystemUtils.getResultTemplateFileName());
				resultTemplate.process(root, writer);
//				}
        	} catch (Exception ex) {
        		SystemUtils.printLog("Exception: " + ex.getClass() + " - " + ex.getMessage());
        	}
			return writer;
        });
    }
}