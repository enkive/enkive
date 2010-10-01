var keyword = context.properties["keyword"];
var from = context.properties["from"];
var to = context.properties["to"];
var cc = context.properties["cc"];
var subject = context.properties["subject"];
var dateFrom = context.properties["dateFrom"];
var dateTo = context.properties["dateTo"];
var messageId = context.properties["messageId"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco"); 

// TODO this will likely choke if the user enters anything with a reserved character
// (e.g., &). Needs escaping.
var messagelist = connector.get("/enkive/search" + 
	"?keyword=" + keyword +
	"&from=" + from +
	"&to=" + to +
	"&cc=" + cc +
	"&subject=" + subject +
	"&dateFrom=" + dateFrom +
	"&dateTo=" + dateTo +
	"&messageId=" + messageId
	);

// this is broken; cannot handle arrays as subelements
// var messageJSON = jsonUtils.toObject(messagelist);

var messageJSON = eval("(" + messagelist + ")");

model.result = messageJSON
