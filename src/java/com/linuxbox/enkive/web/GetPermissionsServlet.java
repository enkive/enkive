package com.linuxbox.enkive.web;

import static com.linuxbox.enkive.web.WebConstants.USER_AUTHORITIES;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.permissions.PermissionService;

public class GetPermissionsServlet extends EnkiveServlet {

	private static final long serialVersionUID = 7260328900686039838L;
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web.permissions");
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PermissionService permService = getPermissionService();

		try {
			Collection<String> userAuthorities = permService
					.getCurrentUserAuthorities();
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put(USER_AUTHORITIES, userAuthorities);
			String jsonResultString = jsonResponse.toString();
			try {
				resp.getWriter().write(jsonResultString);
			} catch (IOException e) {
				LOGGER.error("Could not write JSON response");
			}
		} catch (JSONException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, resp);
			LOGGER.error("Could not serialize JSON", e);
		} catch (CannotGetPermissionsException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, resp);
			LOGGER.error("Could not get user permissions", e);
		}

	}

}
