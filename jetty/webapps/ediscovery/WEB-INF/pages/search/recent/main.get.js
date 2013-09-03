var pos = context.properties["pos"];
var size = context.properties["size"];
var sortBy = context.properties["sortBy"];
var sortDir = context.properties["sortDir"];

// get a connector to the Enkive endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page
var searchlist = connector.get("/search/recentList" + "?pos=" + pos + "&size="
		+ size + "&sortBy=" + sortBy + "&sortDir=" + sortDir);

if (searchlist.status == 200) {
	var resultJSON = eval("(" + searchlist + ")");
	model.searchList = resultJSON.data;
	model.uri = "/ediscovery/search/recent" + "?";
	model.paging = resultJSON.paging;
}
status.code = searchlist.status;
