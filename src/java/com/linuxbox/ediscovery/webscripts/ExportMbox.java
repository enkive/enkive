package com.linuxbox.ediscovery.webscripts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


public class ExportMbox extends AbstractWebScript {

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		res.setContentType("text/plain");
		res.setHeader("Content-disposition", "attachment; filename=export.mbox");

		Map<String, Object> model = new HashMap<String, Object>();
		
		ScriptDetails script = getExecuteScript(req.getContentType());
        Map<String, Object> scriptModel = createScriptParameters(req, res, script, model);

        // add return model allowing script to add items to template model
        Map<String, Object> returnModel = new HashMap<String, Object>();
        scriptModel.put("model", returnModel);
        executeScript(script.getContent(), scriptModel);
        mergeScriptModelIntoTemplateModel(script.getContent(), returnModel, model);
		
		Map<String, Object> templateModel = createTemplateParameters(req, res, model);
		
		String templatePath = getDescription().getId() + "." + req.getFormat();
        // render response according to requested format
        renderTemplate(templatePath, templateModel, res.getWriter());
		
	}
	
    /**
     * Merge script generated model into template-ready model
     *
     * @param scriptContent    script content
     * @param scriptModel      script model
     * @param templateModel    template model
     */
    private void mergeScriptModelIntoTemplateModel(ScriptContent scriptContent, Map<String, Object> scriptModel, Map<String, Object> templateModel)
    {
        // determine script processor
        ScriptProcessor scriptProcessor = getContainer().getScriptProcessorRegistry().getScriptProcessor(scriptContent);
        if (scriptProcessor != null)
        {
            for (Map.Entry<String, Object> entry : scriptModel.entrySet())
            {
                // retrieve script model value
                Object value = entry.getValue();
                Object templateValue = scriptProcessor.unwrapValue(value);
                templateModel.put(entry.getKey(), templateValue);
            }
        }
    }
	
}
