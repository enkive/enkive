// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
var searchlist = connector.get("/enkive/search/saved");
var searchJSON = jsonUtils.toObject(searchlist);

model.searchList = eval("(" + searchJSON.searches + ")");