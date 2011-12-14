var searchid = context.properties["searchid"];

var connector = remote.connect("enkive"); 

var content = connector.get("/search/export/mbox?searchid=" + searchid);
model.content = content;