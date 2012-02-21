var pos = context.properties["pos"];
var size = context.properties["size"];

var connector = remote.connect("enkive");
// retrieve the web script index page
var searchlist = connector.get("/search/savedList?" + "?pos=" + pos + "&size="
		+ size);

if (searchlist.status == 200) {
	var resultJSON = eval("(" + searchlist + ")");
	model.searchList = resultJSON.data;
	model.uri = "/ediscovery/search/saved?";
	model.paging = resultJSON.paging;
}
status.code = searchlist.status;
