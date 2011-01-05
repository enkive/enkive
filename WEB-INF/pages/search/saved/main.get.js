var pos = context.properties["pos"];
var size = context.properties["size"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
var searchlist = connector.get("/enkive/search/saved" + "?pos=" + pos + "&size=" + size);

var resultJSON = eval("(" + searchlist + ")");
model.searchList = resultJSON.data;
model.uri = url.uri + "?";
model.paging = resultJSON.paging;
