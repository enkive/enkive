var messagelist = null;

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco"); 

var searchid = context.properties["searchid"];
var pos = context.properties["pos"];
var size = context.properties["size"];

var content = context.properties["content"];
var sender = context.properties["sender"];
var recipient = context.properties["recipient"];
var subject = context.properties["subject"];
var dateEarliest = context.properties["dateEarliest"];
var dateLatest = context.properties["dateLatest"];
var messageId = context.properties["messageId"];

var uri = url.uri;

if (searchid != null){
	// retrieve the web script index page 
	messagelist = connector.get("/enkive/search/saved/view/" + searchid + "?pos=" + pos + "&size=" + size);
	
} else if(
		(content != null && content != "")|| 
		(sender != null && sender != "")|| 
		(recipient != null && recipient != "")|| 
		(subject != null && subject != "")|| 
		(dateEarliest != null && dateEarliest != "")|| 
		(dateLatest != null && dateLatest != "")|| 
		(messageId != null && messageId != "")
	){
	
	messagelist = connector.get("/enkive/search" + 
		"?content=" + encodeURIComponent(content) +
		"&sender=" + encodeURIComponent(sender) +
		"&recipient=" + encodeURIComponent(recipient) +
		"&subject=" + encodeURIComponent(subject) +
		"&dateEarliest=" + encodeURIComponent(dateEarliest) +
		"&dateLatest=" + encodeURIComponent(dateLatest) +
		"&messageId=" + encodeURIComponent(messageId)
		);
	
} else {
	model.firstRun = true;
}


// this is broken; cannot handle arrays as subelements
// var messageJSON = jsonUtils.toObject(messagelist);

var messageJSON = eval("(" + messagelist + ")");

if(searchid == null && !model.firstRun && messageJSON != null){
	uri = url.context + "/search/recent/view";
	searchid = messageJSON.data.searchId;
}

model.uri = uri + "?searchid=" + searchid;
model.result = messageJSON
