package utils;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationProperties {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
	
	private static Properties applicationProperties;
	
	public static String getStringProperty (final String properyName) 
		throws Exception {
		
		return getProperty(properyName);
	}
	
	public static int getIntProperty (final String properyName) 
		throws Exception {
		
		return Integer.parseInt(getProperty(properyName));
	}
	
	public static boolean getBooleanProperty (final String properyName) 
		throws Exception {
			
		return Boolean.parseBoolean(getProperty(properyName));
	}
	
	public static String getProperty (final String properyName) 
		throws Exception {
		
		if (applicationProperties == null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
	
			try(InputStream resourceStream = loader.getResourceAsStream(Constants.APPLICATION_PROPERTIES)){
				applicationProperties = new Properties();
				applicationProperties.load(resourceStream);
			} catch (Exception ex) {
				logger.error ("Exception: " + ex.getClass() + " - " + ex.getMessage());
				throw ex;
			}
		}
		return applicationProperties.getProperty(properyName);
	}
}
