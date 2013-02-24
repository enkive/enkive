package com.linuxbox.ediscovery.servlet;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This is a failed attempt to create a servlet that would bypass the webscript
 * framework. The problem was that the connector retrieved did not have the
 * authorization ticket attached to it, meaning we would get a failed access on
 * Enkive. It's being left here so that someone might be able to fix the problem
 * down the road.
 * 
 * Something like the following would need to be added to web.xml so this servlet
 * could run:
 * 
 *	<servlet>
 *		<servlet-name>Enkive Get Attachment Servlet</servlet-name>
 *		<servlet-class>com.linuxbox.ediscovery.servlet.GetAttachmentServlet</servlet-class>
 *	</servlet>
 *	
 *	<servlet-mapping>
 *		<servlet-name>Enkive Get Attachment Servlet</servlet-name>
 *		<url-pattern>/direct/get-attachment</url-pattern>
 *	</servlet-mapping>
 * 
 * @author ivancich
 * 
 */
public class GetAttachmentServlet extends HttpServlet {
	private static final long serialVersionUID = -7024079959063195016L;

	private static final String ENKIVE_ENDPOINT_NAME = "enkive";
	private static String ATTACHMENT_RETRIEVE_REST_URL = "/attachment/retrieve?attachmentid=";

	private static byte[] emptyByteArray = {};
	private ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(
			emptyByteArray);
	private ConnectorContext emptyConnectorContext = new ConnectorContext(null,
			buildDefaultHeaders());

	public GetAttachmentServlet() {
		super();
	}

	private static Map<String, String> buildDefaultHeaders() {
		Map<String, String> headers = new HashMap<String, String>(1, 1.0f);
		headers.put("Accept-Language",
				I18NUtil.getLocale().toString().replace('_', '-'));
		return headers;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			ServletContext context = getServletContext();
			WebApplicationContext appContext = (XmlWebApplicationContext) context
					.getAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.Spring MVC Dispatcher Servlet");
			ConnectorService connectorService = (ConnectorService) appContext
					.getBean("connector.service");
			Connector connector = connectorService
					.getConnector(ENKIVE_ENDPOINT_NAME);
			final String attachmentId = req.getParameter("attachmentid");
			final String uri = ATTACHMENT_RETRIEVE_REST_URL + attachmentId;
			// Response connectorResp = connector.call(uri,
			// emptyConnectorContext);
			Response connectorResp = connector.call(uri, emptyConnectorContext,
					emptyInputStream);

			if (connectorResp.getStatus().getCode() == ResponseStatus.STATUS_FORBIDDEN) {
				resp.setStatus(connectorResp.getStatus().getCode());
				BufferedWriter resWriter = new BufferedWriter(resp.getWriter());
				resWriter
						.write("You must be logged in to download attachments.");
				resWriter.close();
			} else {
				resp.setStatus(connectorResp.getStatus().getCode());
				// use a pre-defined MIME type for initial development; this
				// would be more flexible down the road
				resp.setContentType("image/jpeg");

				for (Entry<String, String> entry : connectorResp.getStatus()
						.getHeaders().entrySet()) {
					resp.setHeader(entry.getKey(), entry.getValue());
				}

				InputStream in = connectorResp.getResponseStream();
				OutputStream out = resp.getOutputStream();
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
