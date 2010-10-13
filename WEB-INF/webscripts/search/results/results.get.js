var content = context.properties["content"];
var sender = context.properties["sender"];
var recipient = context.properties["recipient"];
var subject = context.properties["subject"];
var dateEarliest = context.properties["dateEarliest"];
var dateLatest = context.properties["dateLatest"];
var messageId = context.properties["messageId"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco"); 

var messagelist = connector.get("/enkive/search" + 
	"?content=" + encodeURIComponent(content) +
	"&sender=" + encodeURIComponent(sender) +
	"&recipient=" + encodeURIComponent(recipient) +
	"&subject=" + encodeURIComponent(subject) +
	"&dateEarliest=" + encodeURIComponent(dateEarliest) +
	"&dateLatest=" + encodeURIComponent(dateLatest) +
	"&messageId=" + encodeURIComponent(messageId)
	);

// this is broken; cannot handle arrays as subelements
// var messageJSON = jsonUtils.toObject(messagelist);

var messageJSON = eval("(" + messagelist + ")");

model.result = messageJSON
