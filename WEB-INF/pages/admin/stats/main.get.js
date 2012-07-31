// get a connector to the Alfresco repository endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page

var keysJSON = connector.get("/stats/keys");
var keysList = "'" + keysJSON + "'";
model.result = keysList;