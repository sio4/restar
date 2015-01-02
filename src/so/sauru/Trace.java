/**
 * 
 */
package so.sauru;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Execution time tracer
 * 
 * @author sio4
 *
 */
public class Trace {
	long sms;
	long lms;
	SimpleDateFormat formatter = new SimpleDateFormat("mm:ss.SSS");
	Logger logger = LogManager.getLogger("");
	String name;

	/**
	 * Initialize Timer with given name and reset to 0.
	 * 
	 * @param name
	 *            name of timer.
	 */
	public Trace(String name) {
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.name = name;
		this.reset();
	}

	/**
	 * Just reset timer to 0. and call <tt>tag("reset")</tt> which is make log
	 * line.
	 * 
	 * @return log message string.
	 */
	public String reset() {
		sms = System.currentTimeMillis();
		lms = sms;
		return tag("reset");
	}

	/**
	 * Tag and print log message to logger.
	 * 
	 * @param tag
	 *            tag to display on log line.
	 * @return log message string.
	 */
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
