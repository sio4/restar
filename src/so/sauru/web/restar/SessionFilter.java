package so.sauru.web.restar;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionFilter implements Filter {
	private Class<? extends SessionHandler> callBack = null;
	private static String loginPage = "/login";
	private static ArrayList<String> exclude = new ArrayList<String>();
	private static ArrayList<String> include = new ArrayList<String>();
	private Logger logger = null;

	@SuppressWarnings("unchecked")
	public void init(FilterConfig config) throws ServletException {
		logger = LogManager.getLogger(this.getClass().getSimpleName());

		String lp = config.getInitParameter("loginPage");
		if (lp != null && !lp.isEmpty()) {
			loginPage = lp;
		}

		String ex = config.getInitParameter("exclude");
		if (ex != null && !ex.isEmpty()) {
			for (String e : ex.split(" ")) {
				exclude.add(e);
			}
		}

		String in = config.getInitParameter("include");
		if (in != null && !in.isEmpty()) {
			for (String e : in.split(" ")) {
				include.add(e);
			}
		}

		String shn = config.getInitParameter("sessionHandler");
		if (shn == null || shn.isEmpty()) {
			shn = "so.sauru.web.restar.SessionHandler";
		}
		try {
			callBack = (Class<? extends SessionHandler>) Class.forName(shn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ServletException("cannot initiate session fileter.");
		}
		logger.info("session filter initiated successfully.");
		logger.debug("  with config: {}, {}", loginPage, callBack.getName());
		logger.debug("  with exclude path: {}", exclude.toString());
		logger.debug("  with include path: {}", include.toString());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain next) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String path = req.getRequestURI();

		// Handle exception first.
		if (isExcluded(path)) {
			setAccessHeaders(resp);
			next.doFilter(request, response);
			return;
		}

		if (callBack == null) {
			throw new ServletException("configuration error: callBack");
		}

		try {
			if (callBack.newInstance().isValid(req) == true) {
				logger.trace("session validated. next...");
				setAccessHeaders(resp);
				next.doFilter(request, response);
				return;
			}
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		logger.debug("invalid session. forward to: {}", loginPage);
		req.setAttribute("origin", path);
		req.getRequestDispatcher(loginPage).forward(req, resp);
	}

	private boolean isExcluded(String path) {
		for (String p : include) {
			if (path.startsWith(p)) {
				logger.debug("path {} match on {}. included.", path, p);
				return false;
			}
		}
		for (String p : exclude) {
			if (path.startsWith(p)) {
				logger.debug("path {} match on {}. excluded.", path, p);
				return true;
			}
		}
		return false;
	}

	private void setAccessHeaders(HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Credentials", "true");
		resp.addHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
		resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
	}

	public void destroy() {
	}
}