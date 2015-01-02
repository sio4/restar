package so.sauru.web.restar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(description = "Json Writer", urlPatterns = { "/JsonWriter" })
public class JsonWriter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * generate JSON formatted response with <tt>'data'</tt> attribute of the
	 * <tt>request</tt>.
	 */
	public JsonWriter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
		out.println(gson.toJson(req.getAttribute("data")));
	}

}
