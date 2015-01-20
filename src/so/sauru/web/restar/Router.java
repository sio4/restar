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
	public static final String CHILDREN = "children";
	public static final String CONTROLLER = "controller";
	public static final String ID = "id";
	public static final String PARENT = "parent";
	public static final String PID = "pid";

	private String packageName;
	private String cPackageName;
	private boolean metaEnabled = false;

	private String extension = "html";

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

	/**
	 * @author sio4
	 *
	 */
	public class RestRequest {
		private Pattern regexExt = Pattern.compile("(.+)\\.([A-z]+)");
		private Pattern regexPath = Pattern.compile("/([^/?]+)(.*)");
		private ArrayList<HashMap<String, String>> cChain;

		/**
		 * @param pathInfo
		 * @throws ServletException
		 */
		public RestRequest(String pathInfo) throws ServletException {
			Matcher matcher;
			String rem = pathInfo;
			extension = "html";

			/* get and remove extension first */
			matcher = regexExt.matcher(rem);
			if (matcher.find()) {
				extension = matcher.group(2);
				rem = matcher.group(1);
				logger.trace("extension '" + extension + "' provided.");
			} else {
				logger.trace("no extension found. use default. " + extension);
			}

			cChain = new ArrayList<HashMap<String, String>>();
			cChain.clear();
			String cont = null;
			while (rem.length() > 0) {
				matcher = regexPath.matcher(rem);
				if (matcher.find() == false) {
					break;
				}

				if (cont == null) {
					/* controller, check existence of corresponding class. */
					cont = matcher.group(1);
					if (getClazz(cPackageName, cont) == null) {
						String err = "Invalid URI '" + pathInfo + "'. "
								+ "controller for '" + cont + "' not found.";
						logger.error(err);
						throw new ServletException(err);
					}
				} else {
					/* id, add controller-id pair to list. */
					HashMap<String, String> x = new HashMap<String, String>();
					x.put(CONTROLLER, cont);
					x.put(ID, matcher.group(1));
					cChain.add(x);
					cont = null;
				}
				rem = matcher.group(2);
				logger.trace("get '{}', remind: '{}'", matcher.group(1), rem);
			}
			if (cont != null) {
				/* add remaining controller without id. */
				HashMap<String, String> x = new HashMap<String, String>();
				x.put(CONTROLLER, cont);
				x.put(ID, "*");
				cChain.add(x);
				cont = null;
			}
			logger.trace("controller chain: " + cChain.toString());

			return;
		}
	}

	@Override
	public String toString() {
		return restarVersionString + "\n" + routerVersionString + "\n";
	}

	private void rootThenAbort(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.trace("--- start ----------------------------------------------");
		logger.debug("pathinfo: " + req.getPathInfo());
		if (req.getPathInfo().equals("/")) {
			/* just show version string if no path given. */
			PrintWriter out = resp.getWriter();
			resp.resetBuffer();
			out.println(this.toString());
			out.close();
			return;
		}
	}

	private void abort500(HttpServletResponse resp, Exception e)
			throws IOException {
		PrintWriter out = resp.getWriter();
		resp.setStatus(500);
		out.println(e.getMessage());
		e.printStackTrace();
		out.close();
	}

	protected void doResponse(HttpServletRequest req, HttpServletResponse resp,
			HashMap<String, Object> params) throws IOException {
		logger.trace("doResponse params: {}", params.toString());

		try {
			RestRequest resources = new RestRequest(req.getPathInfo());
			HashMap<String, Object> message = new HashMap<String, Object>();
			String ctrlr = resources.cChain.get(0).get(CONTROLLER);
			message.put(PATH, req.getPathInfo());
			message.put(CHILDREN, resources.cChain);
			message.put(CONTROLLER, ctrlr);
			message.put(PARAMS, params);
			message.put(METHOD, req.getMethod());
			logger.trace("doResponse message: {}", message.toString());

			customInit();

			if (extension.compareTo("json") == 0) {
				if (metaEnabled == true) {
					HashMap<String, Object> re = new HashMap<String, Object>();
					re.put("meta", message);
					re.put("objects", getResponse(message, 0).get(ctrlr));
					req.setAttribute("data", re);
				} else {
					req.setAttribute("data", getResponse(message, 0).get(ctrlr));
				}
				req.getRequestDispatcher("/JsonWriter").forward(req, resp);
			}
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
			getResponse(HashMap<String, Object> message, int level)
					throws ControllerException {
		String ctrlrName = "error";
		HashMap<String, Object> result = new HashMap<String, Object>();
		ArrayList<HashMap<String, String>> children;
		children = Utils.toArrayListHashMapStrStr(message.get(CHILDREN));
		logger.debug("getResponse called with level " + level + ".");

		if (children.size() > level) {
			ctrlrName = children.get(level).get(CONTROLLER);
			String id = children.get(level).get(ID);
			Class<?> cClass = getClazz(cPackageName, ctrlrName);
			if (cClass == null) {
				logger.error("oops! no class for " + ctrlrName
						+ "! it's impossible! what's going on?");
				return null;
			}
			logger.trace("- CZ: " + cClass.getSimpleName() + "/" + id);

			try {
				HashMap<String, Object> mesg = new HashMap<String, Object>();
				HashMap<String, Object> rslt = new HashMap<String, Object>();
				mesg.put(ID, id);
				mesg.put(PARAMS, message.get(PARAMS));
				Controller cInst = (Controller) cClass.newInstance();
				logger.trace("- new instance of " + cInst.getClass().getName());
				if (message.get(METHOD).equals("GET")) {
					mesg.put(OPERATION, "index");
					rslt = cInst.index(mesg);
				} else if (message.get(METHOD).equals("POST")) {
					mesg.put(OPERATION, "create");
					rslt = cInst.create(mesg);
				} else {
					logger.error("oops! unsupported method '{}'.",
							message.get(METHOD));
					return null;
				}
				if (rslt != null) {
					result.putAll(rslt);
					try {
						String rt = rslt.get(ctrlrName).getClass().getName();
						logger.trace("- result type: " + rt);
					} catch (NullPointerException e) {
						logger.trace("oops! result is null!");
					}
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}

		if (children.size() > level + 1) {
			logger.trace("callstack: " + children.toString());
			Iterator<HashMap<String, Object>> iter = Utils
					.toArrayListHashMapStrObj(result.get(ctrlrName))
					.iterator();
			while (iter.hasNext()) {
				HashMap<String, Object> elem = iter.next();
				HashMap<String, Object> rslt = null;
				HashMap<String, Object> params = null;

				HashMap<String, Object> mesg = new HashMap<String, Object>(
						message);
				params = Utils.toHashMapStrObj(mesg.get(PARAMS));
				params.put(ctrlrName + "_id", elem.get(ID));
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
		rootThenAbort(req, resp);

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
		rootThenAbort(req, resp);

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
