var messagelist = null;

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("enkive");

var searchid = context.properties["searchid"];
var pos = context.properties["pos"];
var size = context.properties["size"];
var sortBy = context.properties["sortBy"];
var sortDir = context.properties["sortDir"];

var content = context.properties["content"];
var sender = context.properties["sender"];
var recipient = context.properties["recipient"];
var subject = context.properties["subject"];
var dateEarliest = context.properties["dateEarliest"];
var dateLatest = context.properties["dateLatest"];
var messageId = context.properties["messageId"];
var search = context.properties["search"];

var uri = url.uri;

if (searchid != null) {
	// retrieve the web script index page
	messagelist = connector.get("/search/results?id=" + searchid + "&pos="
			+ pos + "&size=" + size + "&sortBy=" + sortBy + "&sortDir=" + sortDir);

} else if ((content != null && content != "")
		|| (sender != null && sender != "")
		|| (recipient != null && recipient != "")
		|| (subject != null && subject != "")
		|| (dateEarliest != null && dateEarliest != "")
		|| (dateLatest != null && dateLatest != "")
		|| (messageId != null && messageId != "")) {

	messagelist = connector.get("/message/search" + "?content="
			+ encodeURIComponent(content) + "&sender="
			+ encodeURIComponent(sender) + "&recipient="
			+ encodeURIComponent(recipient) + "&subject="
			+ encodeURIComponent(subject) + "&dateEarliest="
			+ encodeURIComponent(dateEarliest) + "&dateLatest="
			+ encodeURIComponent(dateLatest) + "&messageId="
			+ encodeURIComponent(messageId));

} else if ((content != null && content == "")
		|| (sender != null && sender == "")
		|| (recipient != null && recipient == "")
		|| (subject != null && subject == "")
		|| (dateEarliest != null && dateEarliest == "")
		|| (dateLatest != null && dateLatest == "")
		|| (messageId != null && messageId == "")) {
	
	model.emptySearch = true;
	
} else {
	model.firstRun = true;
}

// this is broken; cannot handle arrays as subelements
// var messageJSON = jsonUtils.toObject(messagelist);

if (!model.firstRun && !model.emptySearch && messagelist.status == 403) {
	status.code = 403;
} else {

	var messageJSON = eval("(" + messagelist + ")");

	if (searchid == null && !model.firstRun && messageJSON != null) {
		uri = url.context + "/search/recent/view";
		searchid = messageJSON.data.searchId;
	}

	model.uri = uri + "?searchid=" + searchid;
	model.result = messageJSON
}