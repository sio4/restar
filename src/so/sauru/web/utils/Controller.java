package so.sauru.web.utils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public abstract Object index(String id);

	public abstract Object index(HttpServletRequest req,
			HttpServletResponse resp);

}
