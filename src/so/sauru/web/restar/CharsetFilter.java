package so.sauru.web.restar;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class CharsetFilter implements Filter {
	private String encoding;
	private String contentType;

	public void init(FilterConfig config) throws ServletException {
		encoding = config.getInitParameter("Encoding");
		contentType = config.getInitParameter("ContentType");

		if (encoding == null) {
			encoding = "UTF-8";
		}
		if (contentType == null) {
			contentType = "text/html; charset=UTF-8";
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain next) throws IOException, ServletException {
		if (null == request.getCharacterEncoding())
			request.setCharacterEncoding(encoding);

		response.setContentType(contentType);
		response.setCharacterEncoding(encoding);

		next.doFilter(request, response);
	}

	public void destroy() {
	}
}
