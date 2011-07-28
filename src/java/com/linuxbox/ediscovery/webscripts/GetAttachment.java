package com.linuxbox.ediscovery.webscripts;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.ScriptRemoteConnector;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.AlfrescoConnector;
import org.springframework.extensions.webscripts.connector.Response;

public class GetAttachment extends AbstractWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		ScriptDetails script = getExecuteScript(req.getContentType());
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> scriptModel = createScriptParameters(req, res,
				script, model);
		Map<String, Object> returnModel = new HashMap<String, Object>();
		scriptModel.put("model", returnModel);
		executeScript(script.getContent(), scriptModel);
		mergeScriptModelIntoTemplateModel(script.getContent(), returnModel,
				model);

		ScriptProcessor scriptProcessor = getContainer()
				.getScriptProcessorRegistry().getScriptProcessor(
						script.getContent());

		ScriptRemote remote = (ScriptRemote) scriptProcessor
				.unwrapValue(scriptModel.get("remote"));

		ScriptRemoteConnector connector = remote.connect("alfresco");

		Response ticket = connector.get("/enkive/ticket");
		
		String ticketText = "";
		if(ticket.getStatus().getCode() == 200)
			ticketText = "?alf_ticket=" + ticket.getText();
				
		res.setStatus(302);
		res.setHeader("Location", connector.getEndpoint() +
		"/enkive/attachment/"
		+ req.getParameterValues("attachmentid")[0] + ticketText);
		res.setHeader( "Connection", "close" );

		Map<String, Object> templateModel = createTemplateParameters(req, res,
				model);

		String templatePath = getDescription().getId() + "." + req.getFormat();
		// render response according to requested format
		renderTemplate(templatePath, templateModel, res.getWriter());

	}

	private void mergeScriptModelIntoTemplateModel(ScriptContent scriptContent,
			Map<String, Object> scriptModel, Map<String, Object> templateModel) {
		// determine script processor
		ScriptProcessor scriptProcessor = getContainer()
				.getScriptProcessorRegistry().getScriptProcessor(scriptContent);
		if (scriptProcessor != null) {
			for (Map.Entry<String, Object> entry : scriptModel.entrySet()) {
				// retrieve script model value
				Object value = entry.getValue();
				Object templateValue = scriptProcessor.unwrapValue(value);
				templateModel.put(entry.getKey(), templateValue);
			}
		}
	}

}
