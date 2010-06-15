var keyword = context.properties["keyword"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");
// retrieve the web script index page 
var messagelist = connector.get("/enkive/search/simple/keyword" + "?keyword=" + keyword);
model.messages = eval("(" + messagelist + ")");
//model.messages = messagelist;
