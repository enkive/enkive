var content = context.properties["content"];
var sender = context.properties["sender"];
var receiver = context.properties["receiver"];
var subject = context.properties["subject"];
var dateEarliest = context.properties["dateEarliest"];
var dateLatest = context.properties["dateLatest"];
var messageId = context.properties["messageId"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco"); 

// TODO this will likely choke if the user enters anything with a reserved character
// (e.g., &). Needs escaping.
var messagelist = connector.get("/enkive/search" + 
	"?content=" + content +
	"&sender=" + sender +
	"&receiver=" + receiver +
	"&subject=" + subject +
	"&dateEarliest=" + dateEarliest +
	"&dateLatest=" + dateLatest +
	"&messageId=" + messageId
	);

// this is broken; cannot handle arrays as subelements
// var messageJSON = jsonUtils.toObject(messagelist);

var messageJSON = eval("(" + messagelist + ")");

model.result = messageJSON
