package so.sauru.web.restar;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Router extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String restarVersionString = "RESTar/0.0.1";
	private static final String routerVersionString = "Router/0.0.1";
	private String packageName;
	private String cPackageName;

	private Class<?> controller;
	private Class<?> scope;
	private String extension = "html";

	Logger logger = LogManager.getLogger("Router");

	public Router() {
		super();
		this.packageName = this.getClass().getPackage().getName();
		this.cPackageName = packageName + ".controller";
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

	public class RestRequest {
		private Pattern regexExt = Pattern.compile("(.+)\\.([A-z]+)");
		private Pattern regexPath = Pattern.compile("/([^/?]+)(.*)");
		private HashMap<Integer, String> ctrlMap = new HashMap<Integer, String>();

		private String scopeName = null;
		private String ctrlName = null;
		private String sid = null;
		private String cid = "*";

		public RestRequest(String pathInfo) throws ServletException {
			Matcher matcher;
			String rem = pathInfo;
			extension = "html";

			matcher = regexExt.matcher(rem);
			if (matcher.find()) {
				extension = matcher.group(2);
				rem = matcher.group(1);
				logger.trace("extension '" + extension + "' provided.");
			} else {
				logger.trace("no extension found. use default. " + extension);
			}

			int i = 0;
			while (rem.length() > 0) {
				matcher = regexPath.matcher(rem);
				if (matcher.find()) {
					ctrlMap.put(i, matcher.group(1));
					rem = matcher.group(2);
					logger.trace("get " + matcher.group(1) + ", remind " + rem);
					i++;
				} else {
					break;
				}
			}
			logger.trace("ctrlMap has " + ctrlMap.size() + " elements.");

			switch (ctrlMap.size()) {
			case 4:
				cid = ctrlMap.get(3);
			case 3:
				ctrlName = ctrlMap.get(2);
				sid = ctrlMap.get(1);
				scopeName = ctrlMap.get(0);
				break; // break for 4 and 3. 4 includes 3.
			case 2:
				cid = ctrlMap.get(1);
			case 1:
				ctrlName = ctrlMap.get(0);
				break; // break for 2 and 1. 2 includes 1.
			}

			controller = getClazz(cPackageName, ctrlName);
			scope = getClazz(cPackageName, scopeName);
			if (ctrlName != null && controller != null) {
				if (scopeName == null || scope != null) {
					return;
				} else {
					logger.error("Invalid URI(s) " + pathInfo);
					throw new ServletException("Invalid URI(s) " + pathInfo);
				}
			} else {
				logger.error("Invalid URI(c) " + pathInfo);
				throw new ServletException("Invalid URI(c) " + pathInfo);
			}
		}

		public String getCId() {
			return cid;
		}

		public String getSId() {
			return sid;
		}
	}

	private Object getData(Class<?> ctrl, HashMap<String, Object> params) {
		if (ctrl == null) {
			return null;
		} else {
			try {
				Controller o = (Controller) ctrl.newInstance();
				logger.trace("new instance of " + o.getClass().getName());
				return o.index(params);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return restarVersionString + "\n" + routerVersionString + "\n";
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		out.println("GET request handling");
		out.println("pathinfo: " + req.getPathInfo());
		out.println("params: " + req.getParameterMap());

		if (req.getPathInfo().equals("/")) {
			resp.resetBuffer();
			out.println(this.toString());
			out.close();
			return;
		}

		try {
			RestRequest resources = new RestRequest(req.getPathInfo());
			HashMap<String, Object> data = new HashMap<String, Object>();
			HashMap<String, Object> params = new HashMap<String, Object>();
			String cid = resources.getCId();
			String sid = resources.getSId();
			Object o;

			if (scope != null) {
				logger.trace("S:" + scope.getSimpleName() + "/" + sid);
				params.put("id", sid);
				params.put("response", data);
				o = getData(scope, params);
				logger.trace("returns " + o.getClass().getSimpleName());
				if (o instanceof HashMap<?, ?>) {
					data.putAll((Map<? extends String, ? extends Object>) o);
				}
				params.clear();

				params.put("sid", sid);
				params.put("response", data);
				params.put("scope_name", scope.getSimpleName().toLowerCase());
			}

			if (controller != null) {
				logger.trace("C:" + controller.getSimpleName() + "/" + cid);
				params.put("id", cid);
				params.put("response", data);
				o = getData(controller, params);
				logger.trace("returns " + o.getClass().getSimpleName());
				if (o instanceof HashMap<?, ?>) {
					data.putAll((Map<? extends String, ? extends Object>) o);
				}
			}
			params.put("path", req.getPathInfo());

			out.println(params);

			if (extension.compareTo("json") == 0) {
				resp.resetBuffer();
				req.setAttribute("data", params);
				req.getRequestDispatcher("/JsonWriter").forward(req, resp);
			}
		} catch (ServletException e) {
			resp.setStatus(400);
			resp.resetBuffer();
			e.printStackTrace();
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
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

}
