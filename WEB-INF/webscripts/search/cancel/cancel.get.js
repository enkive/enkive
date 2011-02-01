var searchid = context.properties["searchid"];

var connector = remote.connect("alfresco"); 

connector.get("/enkive/search/cancel/" + searchid);
