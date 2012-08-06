package com.linuxbox.enkive.web;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.statistics.KeyConsolidationHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class KeyNameServlet extends EnkiveServlet {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web.StatsServlet");
	private static final long serialVersionUID = 7062366416188559812L;

	private StatsClient client;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		client = getStatsClient();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		LOGGER.info("KeyNameServlet doGet started");
		try {
			try {
				Set<GathererAttributes> attributes = client.getAttributes();
				JSONArray result = new JSONArray();
				
				for(GathererAttributes attribute: attributes){
					if(!attribute.getName().equals("CollectionStatsService")){
						Map<String, Object> keyMethods = new HashMap<String, Object>();
						for(KeyConsolidationHandler key: attribute.getKeys()){
							String builtKey = "";
							for(String keyPart: key.getKey()){
								builtKey = builtKey + keyPart + ".";
							}
							builtKey = builtKey.substring(0, builtKey.length()-1);
							if(key.getMethods() != null){
								Map<String, String[]> realMap = new HashMap<String, String[]>();
								realMap.put(builtKey, (String[])key.getMethods().toArray());
								keyMethods.put(key.getHumanKey(), realMap);
//								keyMethods.put(builtKey, (String[])key.getMethods().toArray());
							}
						}
						Map<String, Object> temp = new HashMap<String, Object>();
						temp.put(attribute.getName(), keyMethods);
						result.put(temp);
					}
				}
				
				try {	
					JSONObject statistics = new JSONObject();
					statistics.put("results", result);
					resp.getWriter().write(statistics.toString());
				} catch (IOException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, resp);
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				} catch (JSONException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, resp);
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				}
			} catch (CannotRetrieveException e) {
				respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
				if (LOGGER.isErrorEnabled())
					LOGGER.error("CannotRetrieveException", e);
			} catch (NullPointerException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("NullException thrown", e);
			}
		} catch (IOException e) {
			LOGGER.error("IOException thrown", e);
		}
		LOGGER.info("StatsServlet doGet finished");
	}
}
