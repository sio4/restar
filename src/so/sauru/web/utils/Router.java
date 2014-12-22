package so.sauru.web.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Router extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Logger logger = LogManager.getLogger("Router");
	ServletContext ctx;
	Map<String, ? extends ServletRegistration> sRegist;

	public class RestRequest {
		private Pattern regexPath = Pattern.compile("/([^/?]+)(.*)");
		private HashMap<Integer, String> ctrlMap = new HashMap<Integer, String>();

		private String scope = null;
		private String scope_id = null;
		private String id = "*";
		private String controller = null;

		public RestRequest(String pathInfo) throws ServletException {
			Matcher matcher;
			String rem = pathInfo;

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
			logger.debug("ctrlMap has " + ctrlMap.size() + "elements.");

			switch (ctrlMap.size()) {
			case 4:
				id = ctrlMap.get(3);
			case 3:
				controller = ctrlMap.get(2);
				scope_id = ctrlMap.get(1);
				scope = ctrlMap.get(0);
				break;	// break for 4 and 3. 4 includes 3.
			case 2:
				id = ctrlMap.get(1);
			case 1:
				controller = ctrlMap.get(0);
				break;	// break for 2 and 1. 2 includes 1.
			}

			if (controller != null && sRegist.containsKey(controller)) {
				if (scope == null || sRegist.containsKey(scope)) {
					logger.info("handle " + controller + "/" + id + ", scope: "
							+ scope + "/" + scope_id);
					return;
				} else {
					logger.error("Invalid URI(s) " + pathInfo);
					throw new ServletException("Invalid URI " + pathInfo);
				}
			} else {
				logger.error("Invalid URI(c) " + pathInfo);
				throw new ServletException("Invalid URI " + pathInfo);
			}
		}

		public String getControllerName() {
			return controller;
		}

		public String getId() {
			return id;
		}

		public String getScope() {
			return scope;
		}

		public String getScopeId() {
			return scope_id;
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ctx = getServletContext();
		sRegist = ctx.getServletRegistrations();

		logger.debug("servlets: " + sRegist.keySet());

		PrintWriter out = resp.getWriter();
		out.println("GET request handling");
		out.println("pathinfo: " + req.getPathInfo());
		out.println("params: " + req.getParameterMap());

		try {
			RestRequest resources = new RestRequest(req.getPathInfo());
			String controller = resources.getControllerName();
			String cid = resources.getId();
			String scope = resources.getScope();
			String sid = resources.getScopeId();
			out.println("Controller " + controller + " for " + cid + " selected.");
			out.println("  in Scope " + scope + " for " + sid + " selected.");
			out.println("Scope -----------------");
			ctx.getNamedDispatcher(scope).include(req, resp);
			out.println("Controller -----------------");
			ctx.getNamedDispatcher(controller).include(req, resp);
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
