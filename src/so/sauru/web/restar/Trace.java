/**
 * 
 */
package so.sauru.web.restar;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author sio4
 *
 */
public class Trace {
	long sms;
	long lms;
	SimpleDateFormat formatter = new SimpleDateFormat("mm:ss.SSS");
	Logger logger = LogManager.getLogger("");
	String name;

	public Trace(String name) {
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.name = name;
		this.reset();
	}

	public String reset() {
		sms = System.currentTimeMillis();
		lms = sms;
		return tag("reset");
	}

	public String tag(String tag) {
		long cms = System.currentTimeMillis();
		String tot = formatter.format((cms - sms));
		String del = formatter.format((cms - lms));
		lms = cms;
		String message = del + "(" + tot + ") - " + name + "/" + tag;
		logger.trace(message);
		return message;
	}

	/**
	 * sleep execution while given second.
	 * 
	 * @param ms
	 *            time in millisecond.
	 */
	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}
}
