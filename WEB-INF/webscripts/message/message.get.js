var messageid = context.properties["messageid"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the message
var response = connector.get("/enkive/message/" + messageid);

if(response.status == 200)
	model.text = response;
else
	model.text = "Your session has expired.  You must login to view this message";
