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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import so.sauru.Utils;
import so.sauru.web.restar.Controller.ControllerException;

/**
 * @author sio4
 *
 */
public abstract class Router extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String restarVersionString = "RESTar/0.0.1";
	private static final String routerVersionString = "Router/0.0.1";

	/* KEYNAMEs */
	public static final String PATH = "path";
	public static final String METHOD = "method";
	public static final String OPERATION = "operation";
	public static final String PARAMS = "params";
	public static final String ROUTE = "route";
	public static final String MODEL = "model";
	public static final String ROUTE_PATH = "route_path";
	public static final String ID = "id";
	public static final String PARENT = "parent";
	public static final String PID = "pid";

	private String packageName;
	private String cPackageName;
	private boolean metaEnabled = false;

	private String extension = "json";

	Logger logger = LogManager.getLogger("Router");

	public Router() {
		super();
		this.packageName = this.getClass().getPackage().getName();
		this.cPackageName = packageName + ".controller";
	}

	protected boolean setMetaMode(boolean mode) {
		metaEnabled = mode;
		return metaEnabled;
	}

	private Class<?> getClazz(String pName, String cName) {
		if (cName == null)
			return null;
		cName = cName.substring(0, 1).toUpperCase() + cName.substring(1);
		cName = pName + "." + cName;
		try {
			return Class.forName(cName);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private HashMap<String, String> getParams(HttpServletRequest req) {
		HashMap<String, String> params = new HashMap<String, String>();

		/* get GET parameters */
		Enumeration<String> param_names = req.getParameterNames();
		while (param_names.hasMoreElements()) {
			String name = param_names.nextElement();
			params.put(name, req.getParameter(name));
		}
		return params;
	}

	private String getMethod(HttpServletRequest req) {
		String method;

		switch (req.getMethod()) {
		case "GET":
			method = "index";
			break;
		case "POST":
			method = "create";
			break;
		default:
			logger.error("oops! unsupported method '{}'.", req.getMethod());
			method = "undefind";
			break;
		}
		return method;
	}

	private HashMap<String, Object> getRoute(HttpServletRequest req)
			throws ServletException {
		HashMap<String, Object> route = new HashMap<String, Object>();
		ArrayList<HashMap<String, String>> route_path;

		route_path = new ArrayList<HashMap<String, String>>();
		Pattern regexExt = Pattern.compile("(.+)\\.([A-z]+)");
		Pattern regexPath = Pattern.compile("/([^/?]+)");

		Matcher matcher;
		String path_remind = req.getPathInfo();

		// get and remove extension first
		matcher = regexExt.matcher(path_remind);
		if (matcher.find()) {
			extension = matcher.group(2);
			path_remind = matcher.group(1);
		} else {
			logger.trace("no extension found. use default ({})", extension);
		}

		matcher = regexPath.matcher(path_remind);
		String model = null;
		while (path_remind.length() > 1 && matcher.hitEnd() == false) {
			String id = "*";
			matcher.find();	// find model first.
			model = matcher.group(1);
			if (matcher.hitEnd() == false) {
				matcher.find();	// find id then.
				id = matcher.group(1);
			}

			if (getClazz(cPackageName, model) == null) {
				String error = "Invalid URI '" + req.getPathInfo() + "'. "
						+ "controller for '" + model + "' not found.";
				logger.error(error);
				throw new ServletException(error);
			}
			HashMap<String, String> p = new HashMap<String, String>();
			p.put(MODEL, model);
			p.put(ID, id);
			route_path.add(p);
		}
		route.put(MODEL, model);
		route.put(ROUTE_PATH, route_path);
		route.put(PATH, req.getPathInfo());
		route.put(PARAMS, getParams(req));
		route.put(METHOD, getMethod(req));
		logger.debug("ROUTE to {}", route);
		return route;
	}

	@Override
	public String toString() {
		return restarVersionString + " " + routerVersionString + " ";
	}

	private void responseByExt(HttpServletResponse resp, Object data)
			throws IOException {
		logger.debug("response as {}...", extension);
		PrintWriter out = resp.getWriter();
		resp.resetBuffer();

		if (extension.equals("json")) {
			resp.setContentType("application/json");
			out.println(Utils.toJson(data));
		}
		out.close();
	}

	private void doVersionResponse(HttpServletResponse resp)
			throws IOException {
		logger.debug("path is '/'. abort with version string");

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("version", this.toString());
		data.put("message", "Hello World");
		responseByExt(resp, data);
	}

	private void abort500(HttpServletResponse resp, Exception e)
			throws IOException {
		PrintWriter out = resp.getWriter();
		resp.setStatus(500);
		out.println(e.getMessage());
		e.printStackTrace();
		logger.error("uncontrollable error! abort with status 500");
		out.close();
	}

	protected void doResponse(HttpServletRequest req, HttpServletResponse resp,
			HashMap<String, Object> params) throws IOException,
			ServletException {
		logger.trace("doResponse params: {}", params.toString());

		try {
			HashMap<String, Object> route = getRoute(req);
			String model = (String) route.get(MODEL);

			customInit();

			HashMap<String, Object> response = getResponse(route, 0);
			if (!metaEnabled) {
				response.put("meta", route);
				responseByExt(resp, response);
			} else {
				responseByExt(resp, response.get(model));
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			logger.debug("request is point on ROOT.");
			doVersionResponse(resp);
		} catch (NullPointerException e) {
			logger.error("nullpointer exception!");
			abort500(resp, e);
		} catch (ServletException e) {
			logger.error("servlet exception: ", e.getMessage());
			abort500(resp, e);
		} catch (ControllerException e) {
			logger.error("controller exception: ", e.getMessage());
			abort500(resp, e);
		}
	}

	/**
	 * is called by <tt>doGet(...)</tt> automatically before URI parsing, and
	 * compound responses of related classes/methods recursively. the recursion
	 * of this method will generate nested object tree, so it can be handled by
	 * JsonWirter or other object formatter.
	 * 
	 * @param message
	 *            hash-map structured task informations
	 * @param level
	 *            nest level (the initial value is zero)
	 * @return the final, structured response
	 * @throws ControllerException
	 */
	private HashMap<String, Object>
			getResponse(HashMap<String, Object> route, int level)
					throws ControllerException {
		HashMap<String, Object> result = new HashMap<String, Object>();
		String model = "error";
		ArrayList<HashMap<String, String>> route_path;
		route_path = Utils.toArrayListHashMapStrStr(route.get(ROUTE_PATH));
		logger.debug("getResponse lev-{} route_path {}.", level, route_path);

		if (route_path.size() > level) {
			model = route_path.get(level).get(MODEL);
			String id = route_path.get(level).get(ID);
			Class<?> cClass = getClazz(cPackageName, model);
			if (cClass == null) {
				logger.error("oops! no class for " + model
						+ "! it's impossible! what's going on?");
				return null;
			}

			try {
				HashMap<String, Object> mesg = new HashMap<String, Object>();
				HashMap<String, Object> rslt = new HashMap<String, Object>();
				mesg.put(ID, id);
				mesg.put(PARAMS, route.get(PARAMS)); // XXX is for last or all?
				mesg.put(OPERATION, route.get(METHOD));

				Controller ctrlr = (Controller) cClass.newInstance();
				Class<?>[] params = { HashMap.class };
				Method m = cClass.getMethod((String) route.get(METHOD), params);
				logger.debug("invoking {}#{}...", cClass.getName(), m.getName());
				rslt = Utils.toHashMapStrObj(m.invoke(ctrlr, mesg));
				if (rslt == null) {
					logger.error("result is null!");
					return null;
				}
				result.putAll(rslt);
				try {
					String rt = rslt.get(model).getClass().getName();
					logger.trace("- result type: " + rt);
				} catch (NullPointerException e) {
					logger.trace("oops! result is null!");
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchMethodException e) {	// for getMethod
				e.printStackTrace();
				return null;
			} catch (SecurityException e) {	// for getMethod
				e.printStackTrace();
				return null;
			} catch (IllegalArgumentException e) {	// for invoke
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {	// for invoke
				e.printStackTrace();
				return null;
			}
		}

		if (route_path.size() > level + 1) {
			logger.trace("callstack: " + route_path.toString());
			Iterator<HashMap<String, Object>> iter = Utils
					.toArrayListHashMapStrObj(result.get(model))
					.iterator();
			while (iter.hasNext()) {
				HashMap<String, Object> elem = iter.next();
				HashMap<String, Object> rslt = null;
				HashMap<String, Object> params = null;

				HashMap<String, Object> mesg = new HashMap<String, Object>(
						route);
				params = Utils.toHashMapStrObj(mesg.get(PARAMS));
				params.put(model + "_id", elem.get(ID));
				rslt = getResponse(mesg, level + 1);
				if (rslt != null) {
					elem.putAll(rslt);
				} else {
					elem.put("ERROR", rslt);
					logger.error("result is null.");
				}
			}
		}
		return result;
	}

	/**
	 * restar.Router version of <tt>GET</tt> <tt>HttpServletRequest</tt>
	 * Handler. It parses the <tt>URI</tt> of <tt>GET</tt> request for RESTful
	 * API and calls registered <tt>root</tt> Controller class.
	 * 
	 * @see Controller
	 * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HashMap<String, Object> params = new HashMap<String, Object>();
		logger.trace("--- start '{}' --------------------", req.getPathInfo());

		/* get GET parameters */
		Enumeration<String> pNs = req.getParameterNames();
		while (pNs.hasMoreElements()) {
			String pN = pNs.nextElement();
			params.put(pN, req.getParameter(pN));
		}

		doResponse(req, resp, params);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HashMap<String, Object> params = new HashMap<String, Object>();
		logger.trace("--- start '{}' --------------------", req.getPathInfo());

		/* get POST parameters */
		BufferedReader br = req.getReader();
		StringBuilder sb = new StringBuilder();
		String json;
		while ((json = br.readLine()) != null) {
			sb.append(json);
		}
		json = sb.toString();
		logger.debug("json: {}", json);
		Gson g = new Gson();
		params = Utils.toHashMapStrObj(g.fromJson(json, params.getClass()));

		doResponse(req, resp, params);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPut(req, resp);
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doDelete(req, resp);
	}

	public abstract void customInit();
}
