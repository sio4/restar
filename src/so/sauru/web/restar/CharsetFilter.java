/*****************************************************************************
 * RESTar, simple RestFul API framework.
 *   Page: http://sr-web.github.io/restar
 *   Source: https://github.com/sr-web/restar
 *   Author: Yonghwan SO <sio4@users.sf.net>
 * 
 * Copyright (c) 2014-2015 Yonghwan SO <sio4@users.sf.net>
 * 
 * This file is part of RESTar.
 * 
 * RESTar is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * RESTar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * RESTar. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
