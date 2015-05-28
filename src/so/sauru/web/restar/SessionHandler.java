package so.sauru.web.restar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionHandler {
	protected HttpSession sess = null;
	protected Logger logger = null;

	public SessionHandler () {
		logger = LogManager.getLogger(getClass().getSimpleName());
	}

	public boolean isValid(HttpServletRequest req) {
		sess = req.getSession();
		String cu = (String) sess.getAttribute("current_user");

		if (cu != null && !cu.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean setUser(HttpServletRequest req, String cu) {
		sess = req.getSession();
		sess.setAttribute("current_user", cu);
		return true;
	}
}
