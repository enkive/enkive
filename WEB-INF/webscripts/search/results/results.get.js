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

if (searchid != null){
	// retrieve the web script index page 
	messagelist = connector.get("/enkive/search/saved/view/" + searchid + "?pos=" + pos + "&size=" + size);
	model.uri = url.uri + "?searchid=" + searchid;
	
} else if(
		content != null || 
		sender != null || 
		recipient != null || 
		subject != null || 
		dateEarliest != null || 
		dateLatest != null || 
		messageId != null
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

model.result = messageJSON


