package utils;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemUtils {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(SystemUtils.class);
	
    public static String getHostName () {
    	String result = "unknown";
        try {
            result = InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {}
        return result;
    }

	public final Date getDateFromString(String date, String format) {
		Date result = null;
		try {
			// format example: "dd-mm-yyyy"
			result = new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
