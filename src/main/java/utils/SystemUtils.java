package utils;
import java.io.File;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	public final Date getDateFromString(final String date, final String format) {
		Date result = null;
		try {
			// format example: "dd-mm-yyyy"
			result = new SimpleDateFormat(format).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static List<String> getFilesFromPath (final String path, final String extension) {
		List<String> filesList = new ArrayList<String>();
		File dir = new File(path);
		for (File file : dir.listFiles()) {
			if (file.getName().toLowerCase().endsWith((extension))) {
				filesList.add(file.getName());
			}
		}
		return filesList;
	}
}
