var pos = context.properties["pos"];
var size = context.properties["size"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
var searchlist = connector.get("/enkive/search/recent" + "?pos=" + pos + "&size=" + size);
var searchJSON = jsonUtils.toObject(searchlist);

model.searchList = eval("(" + searchJSON.data + ")");