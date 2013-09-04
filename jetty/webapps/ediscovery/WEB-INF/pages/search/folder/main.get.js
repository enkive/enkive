var id = context.properties["id"];
var pos = context.properties["pos"];
var size = context.properties["size"];
var sortBy = context.properties["sortBy"];
var sortDir = context.properties["sortDir"];

// get a connector to the Enkive endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page
var searchlist = connector.get("/search/searchFolder" + "?pos=" + pos + "&size="
		+ size + "&action=view&id=" + id);

if (searchlist.status == 200) {
	var resultJSON = eval("(" + searchlist + ")");
	model.result = resultJSON;
	model.searchList = resultJSON.data;
	model.baseuri = "/ediscovery/search/folder" + "?id=" + id + "&action=view";

	// Set up uri for paging
	uri = model.baseuri;
	if (sortBy != null) {
		uri = uri + "&sortBy=" + sortBy;
	}
	if (sortDir != null) {
		uri = uri + "&sortDir=" + sortDir;
	}
	model.sorturi = uri;

	model.paging = resultJSON.paging;
}
status.code = searchlist.status;
