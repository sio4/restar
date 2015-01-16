package so.sauru.web.restar;

import java.io.IOException;

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
	private Logger logger = null;

	@SuppressWarnings("unchecked")
	public void init(FilterConfig config) throws ServletException {
		logger = LogManager.getLogger(this.getClass().getSimpleName());

		String lp = config.getInitParameter("loginPage");
		if (lp != null && !lp.isEmpty()) {
			loginPage = lp;
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
		logger.debug("- config: {}, {}", loginPage, callBack.getName());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain next) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		if (callBack == null) {
			throw new ServletException("configuration error: callBack");
		}

		try {
			if (callBack.newInstance().isValid(req) == true) {
				logger.trace("session validated. next...");
				next.doFilter(request, response);
				return;
			}
		} catch (IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		logger.debug("invalid session. forward to login page: {}", req.toString());
		req.getRequestDispatcher(loginPage).forward(req, resp);
	}

	public void destroy() {
	}
}