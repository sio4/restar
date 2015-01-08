/*****************************************************************************
 * RESTar, simple RestFul API framework.
 *   Page: http://sr-web.github.io/restar
 *   Source: https://github.com/sr-web/restar
 *   Author: Yonghwan SO <sio4@users.sf.net>
 * 
 * Copyright (c) 2014-2015 Yonghwan SO <sio4@users.sf.net>
 * 
 * This file is part of RESTar.
 * 
 * RESTar is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * RESTar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * RESTar. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package so.sauru.web.restar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author sio4
 *
 */
public abstract class Controller {
	protected String className;

	protected HashMap<String, Object> resp;
	protected HashMap<String, Object> parent;
	protected ArrayList<HashMap<String, Object>> parents;
	protected ArrayList<HashMap<?, ?>> list = new ArrayList<HashMap<?, ?>>();

	protected Logger logger;

	public Controller() {
		className = this.getClass().getSimpleName().toLowerCase();
		logger = LogManager.getLogger(className);
	}

	/**
	 * deprecated! do not use this!
	 * 
	 * @param list
	 * @param params
	 * @return T.T
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private HashMap<String, Object> assemble(Object list,
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

	/**
	 * controller method for GET request, to get list of objects with select
	 * condition <tt>'argument'</tt>. the parameter named <tt>argument</tt> is
	 * same for all cases, but it must be interpreted by each implementation's
	 * context. <br>
	 * <br>
	 * for example, the <tt>argument</tt> <tt>'dennis'</tt> for
	 * <tt>controller</tt> <tt>person</tt> can be interpreted as <tt>name</tt>
	 * of <tt>person</tt> or <tt>id</tt> of <tt>person</tt> which we need to
	 * select. <br>
	 * another example, the <tt>argument</tt> <tt>'@earth'</tt> for
	 * <tt>person</tt> can be interpreted as <tt>group</tt> of people but for
	 * <tt>account</tt>, it can be interpreted as the <tt>server</tt> we want to
	 * get the list of accounts.
	 * 
	 * @param message
	 *            contains all information about the request, especially
	 *            <tt>'argument'</tt> that is <tt>id</tt> part of REST URI
	 *            <tt>'/controller/id'</tt>.
	 * @return hash-mapped object something like: { "class_of_object" :
	 *         [{object}, {object},...] }
	 */
	public abstract HashMap<String, Object> index(
			HashMap<String, Object> message);

}
