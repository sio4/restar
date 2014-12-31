package so.sauru;

import java.util.ArrayList;
import java.util.HashMap;

public final class Utils {

	/**
	 * replacement of casting as from Object to HashMap<String, Object> without
	 * annoying warning.
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
	 * replacement of casting as from Object to HashMap<String, String> without
	 * annoying warning.
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
	 * replacement of casting as from Object to ArrayList<HashMap<String,
	 * Object>> without annoying warning.
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
	 * replacement of casting as from Object to ArrayList<HashMap<String,
	 * String>> without annoying warning.
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
		return (ArrayList<HashMap<String, String>>) object;
	}
}
