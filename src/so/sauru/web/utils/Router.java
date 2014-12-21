package so.sauru.web.utils;

import java.io.IOException;
import java.io.PrintWriter;
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
		private Pattern repWithId = Pattern.compile("/([^/]+)/([^/]+)");
		private Pattern repWithoutId = Pattern.compile("/([^/]+)[/]*");

		private String id;
		private String controller;
		private String className;

		public RestRequest(String pathInfo) throws ServletException {
			Matcher matcher;

			matcher = repWithoutId.matcher(pathInfo);
			if (matcher.find()) {
				controller = matcher.group(1);
				id = "*";
			}

			matcher = repWithId.matcher(pathInfo);
			if (matcher.find()) {
				controller = matcher.group(1);
				id = matcher.group(2);
			}

			sRegist = getServletContext().getServletRegistrations();
			if (sRegist.containsKey(controller)) {
				className = sRegist.get(controller).getClassName();
				logger.debug("found " + controller + " and " + id);
				return;
			} else {
				logger.error("Invalid URI " + pathInfo);
				throw new ServletException("Invalid URI");
			}
		}

		public String getControllerName() {
			return controller;
		}

		public String getClassName() {
			return className;
		}

		public String getId() {
			return id;
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
			RestRequest resourceValues = new RestRequest(req.getPathInfo());
			String controller = resourceValues.getControllerName();
			String id = resourceValues.getId();
			out.println("CTLR " + controller + " for " + id + " selected.");
			out.println("-----------------");
			ctx.getNamedDispatcher(controller).include(req, resp);
		} catch (ServletException e) {
			resp.setStatus(400);
			resp.resetBuffer();
			e.printStackTrace();
			// out.println(e.toString());
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
