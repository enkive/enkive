var searchid = context.properties["searchid"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
var messagelist = connector.get("/enkive/search/saved/view/" + searchid);

var messageJSON = eval("(" + messagelist + ")");

model.result = messageJSON