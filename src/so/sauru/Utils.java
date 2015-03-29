package so.sauru;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Utils {

	public static Object toNumIfNum(String str) {
		/* is boolean? */
		if (str.equalsIgnoreCase("true")) {
			return true;
		} else if (str.equalsIgnoreCase("false")) {
			return false;
		}
		/* is numeric? */
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e1) {
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e2) {
				try {
					return Double.parseDouble(str);
				} catch (NumberFormatException e3) {
					return str;
				}

			}
		}
	}

	/**
	 * replacement of casting as from Object to
	 * <tt>HashMap&lt;String, Object&gt;</tt> without annoying warning.
	 * 
	 * @param object
	 * @return object it self which is casted as return type.
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object>
			toHashMapStrObj(Object object) {
		if (object == null) {
			return null;
		}
		return (HashMap<String, Object>) object;
	}

	/**
	 * replacement of casting as from Object to
	 * <tt>HashMap&lt;String, String&gt;</tt> without annoying warning.
	 * 
	 * @param object
	 * @return object it self which is casted as return type.
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, String>
			toHashMapStrStr(Object object) {
		if (object == null) {
			return null;
		}
		return (HashMap<String, String>) object;
	}

	/**
	 * replacement of casting as from Object to
	 * <tt>ArrayList&lt;HashMap&lt;String, Object&gt;&gt;</tt> without annoying
	 * warning.
	 * 
	 * @param object
	 * @return object it self which is casted as return type.
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<HashMap<String, Object>>
			toArrayListHashMapStrObj(Object object) {
		if (object == null) {
			return null;
		}
		return (ArrayList<HashMap<String, Object>>) object;
	}

	/**
	 * replacement of casting as from Object to
	 * <tt>ArrayList&lt;HashMap&lt;String, * String&gt;&gt;</tt> without
	 * annoying warning.
	 * 
	 * @param object
	 * @return object it self which is casted as return type.
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<HashMap<String, String>>
			toArrayListHashMapStrStr(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof ArrayList) {
			return (ArrayList<HashMap<String, String>>) object;
		} else {
			return null;
		}
	}

	public static String asSingleName(String plural) {
		// FIXME quick and dirty. but working for current usage...
		return plural.substring(0, plural.length() - 1);
	}

	/**
	 * convert object(<tt>ArrayList</tt> or <tt>HashMap</tt>) to JSON string.
	 *
	 * @param obj
	 *            source object to be JSON string.
	 * @return JSON string.
	 */
	public static String toJson(Object obj) {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
		return gson.toJson(obj);
	}
}
