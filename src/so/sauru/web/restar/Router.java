package so.sauru.web.restar;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

import so.sauru.Utils;

/**
 * @author sio4
 *
 */
public abstract class Router extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String restarVersionString = "RESTar/0.0.1";
	private static final String routerVersionString = "Router/0.0.1";
	private String packageName;
	private String cPackageName;

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
					/* argument, add controller-argument pair to list. */
					HashMap<String, String> x = new HashMap<String, String>();
					x.put("controller", cont);
					x.put("argument", matcher.group(1));
					cChain.add(x);
					cont = null;
				}
				rem = matcher.group(2);
				logger.trace("get " + matcher.group(1) + ", remind " + rem);
			}
			if (cont != null) {
				/* add remaining controller without argument. */
				HashMap<String, String> x = new HashMap<String, String>();
				x.put("controller", cont);
				x.put("argument", "*");
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
		PrintWriter out = resp.getWriter();

		customInit();

		logger.trace("--- start ----------------------------------------------");
		logger.debug("pathinfo: " + req.getPathInfo());
		logger.debug("params: " + req.getParameterMap());
		if (req.getPathInfo().equals("/")) {
			/* just show version string if no path given. */
			resp.resetBuffer();
			out.println(this.toString());
			out.close();
			return;
		}

		try {
			RestRequest resources = new RestRequest(req.getPathInfo());
			HashMap<String, Object> message = new HashMap<String, Object>();

			String rootName = resources.cChain.get(0).get("controller");

			message.put("path", req.getPathInfo());
			message.put("children", resources.cChain);
			message.put("object", rootName);
			message.put("response", getResponse(message, 0).get(rootName));

			if (extension.compareTo("json") == 0) {
				resp.resetBuffer();
				req.setAttribute("data", getResponse(message, 0).get(rootName));
				req.getRequestDispatcher("/JsonWriter").forward(req, resp);
			}
		} catch (ServletException e) {
			resp.setStatus(400);
			resp.resetBuffer();
			out.println(e.toString());
			e.printStackTrace();
		}
		out.close();
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
	 */
	private HashMap<String, Object>
			getResponse(HashMap<String, Object> message, int level) {
		String controllerName = "error";
		HashMap<String, Object> result = new HashMap<String, Object>();
		ArrayList<HashMap<String, String>> children;
		children = Utils.toArrayListHashMapStrStr(message.get("children"));
		logger.debug("getResponse called with level " + level + ".");

		if (children.size() > level) {
			controllerName = children.get(level).get("controller");
			String argument = children.get(level).get("argument");
			Class<?> cClass = getClazz(cPackageName, controllerName);
			if (cClass == null) {
				logger.error("oops! no class for " + controllerName
						+ "! it's impossible! what's going on?");
				return null;
			}
			logger.trace("- CZ: " + cClass.getSimpleName() + "/" + argument);

			try {
				HashMap<String, Object> mesg = new HashMap<String, Object>();
				HashMap<String, Object> rslt = new HashMap<String, Object>();
				mesg.put("argument", argument);
				Controller cInst = (Controller) cClass.newInstance();
				logger.trace("- new instance of " + cInst.getClass().getName());
				rslt = cInst.index(mesg);
				if (rslt != null) {
					result.putAll(rslt);
					logger.trace("- result: " + result.toString());
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}

		if (children.size() > level + 1) {
			logger.trace("remainder: " + children.toString());
			Iterator<HashMap<String, Object>> iter = Utils
					.toArrayListHashMapStrObj(result.get(controllerName))
					.iterator();
			while (iter.hasNext()) {
				HashMap<String, Object> elem = iter.next();
				HashMap<String, Object> rslt = null;

				HashMap<String, Object> mesg = new HashMap<String, Object>(
						message);
				rslt = getResponse(mesg, level + 1);
				if (rslt != null) {
					elem.putAll(rslt);
				} else {
					elem.put("ERROR", rslt);
					logger.error("result is null.");
				}
			}
		}
		/*
		logger.trace("XXX"
				+ ((ArrayList<?>) result.get(controllerName)).get(0).getClass()
						.getSimpleName());
						*/
		return result;
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

	public abstract void customInit();
}
