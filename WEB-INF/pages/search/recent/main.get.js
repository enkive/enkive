var pos = context.properties["pos"];
var size = context.properties["size"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page 
var searchlist = connector.get("/recentSearchList" + "?pos=" + pos + "&size=" + size);

var resultJSON = eval("(" + searchlist + ")");
model.searchList = resultJSON.data;
model.uri = "/ediscovery/search/recent" + "?";
model.paging = resultJSON.paging;
