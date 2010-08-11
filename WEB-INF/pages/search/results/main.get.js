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
// retrieve the web script index page 
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
var rawdata = messagelist;
var messageJSON = jsonUtils.toObject(messagelist);

model.messageList = eval("(" + messageJSON.messages + ")");
