var searchid = context.properties["searchid"];

var connector = remote.connect("enkive"); 

connector.get("/search/delete?searchid=" + searchid);
