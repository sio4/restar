package so.sauru.web.restar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Controller {
	private static String className;

	protected HashMap<String, Object> resp;
	protected HashMap<String, Object> parent;
	protected ArrayList<HashMap<String, Object>> parents;
	protected ArrayList<HashMap<?, ?>> list = new ArrayList<HashMap<?, ?>>();

	protected Logger logger;

	public Controller() {
		className = this.getClass().getSimpleName().toLowerCase();
		logger = LogManager.getLogger(className);
	}

	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> assemble(Object list,
			HashMap<String, Object> params) {
		resp = (HashMap<String, Object>) params.get("response");
		if (params.containsKey("scope_name")) {
			String scope_name = (String) params.get("scope_name");
			Object o = resp.get(scope_name);
			if (o instanceof ArrayList) {
				parents = (ArrayList<HashMap<String, Object>>) o;
				Iterator<HashMap<String, Object>> iter = parents.iterator();
				while (iter.hasNext()) {
					HashMap<String, Object> p = iter.next();
					p.put(className, list);
				}
			} else if (o instanceof HashMap) {
				parent = (HashMap<String, Object>) o;
				parent.put(className, list);
			}
		} else {
			resp.put(className, list);
			params.put("response", resp);
			params.put("scope_name", className);
		}
		return resp;
	}

	public abstract HashMap<String, Object> index(HashMap<String, Object> message);

}
