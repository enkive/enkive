var searchid = context.properties["searchid"];

var connector = remote.connect("alfresco"); 

var content = connector.get("/enkive/mbox/export/" + searchid);
model.content = content;