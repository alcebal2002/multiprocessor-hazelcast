import static spark.Spark.get;
import static spark.Spark.halt;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hazelcast.core.IMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.Spark;

public class SparkMain {

	@SuppressWarnings("deprecation")
	private static Configuration freemarkerConfig = new Configuration();

	public static void main(String[] args) {

		freemarkerConfig.setClassForTemplateLoading(SparkMain.class,"/templates/");

		Spark.staticFileLocation("/public");
		
		get("/", (req, res) -> "Welcome to Spark !");
        get("/stop", (req, res) -> halt(401, "Go away!"));
        get("/test_freemarker", (req, res) -> {
    		 
            StringWriter writer = new StringWriter();
 
            try {
                Template formTemplate = freemarkerConfig.getTemplate("form.ftl");
 
                formTemplate.process(null, writer);
            } catch (Exception e) {
                Spark.halt(500);
            }
 
            return writer;
        });
        
        Spark.post("/test_freemarker_result", (request, response) -> {
            StringWriter writer = new StringWriter();
 
            try {
                String name = request.queryParams("name") != null ? request.queryParams("name") : "anonymous";
                String email = request.queryParams("email") != null ? request.queryParams("email") : "unknown";
 
                Template resultTemplate = freemarkerConfig.getTemplate("result.ftl");
 
                Map<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("email", email);
 
                resultTemplate.process(map, writer);
            } catch (Exception e) {
                Spark.halt(500);
            }
 
            return writer;
        });
        
        get("/monitor", (req, res) -> {
        	StringWriter writer = new StringWriter();
    		
			IMap<String,NodeDetails> monitorMap = HazelcastManager.getInstance().getMap(HazelcastManager.getMonitorMapName());
			if (monitorMap != null && monitorMap.size() > 0) {
				Map<String, Object> root = new HashMap<String, Object>();
				root.put( "monitorMap", monitorMap );
				Template resultTemplate = freemarkerConfig.getTemplate("result.ftl");
				resultTemplate.process(root, writer);
			}
			return writer;
        });
    }
}