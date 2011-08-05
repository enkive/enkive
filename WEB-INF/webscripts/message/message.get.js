var messageid = context.properties["messageid"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");

var attachments = connector.get("/enkive/messageattachments/" + messageid);

// retrieve the message
var response = connector.get("/enkive/message/" + messageid);

if(response.status == 200){
	model.text = response;
	var attachmentsJSON = eval("(" + attachments + ")");
	model.attachments = attachmentsJSON.data;
}
else
	model.text = "Your session has expired.  You must login to view this message";
