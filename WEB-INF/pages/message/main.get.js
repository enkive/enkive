var messageid = context.properties["messageid"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
model.text = connector.get("/emar/messages/" + messageid);
//model.text = messageid;