var searchid = context.properties["searchid"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
var searchlist = connector.get("/enkive/search/saved/view/" + searchid);

model.searchList = eval("(" + searchlist + ")");