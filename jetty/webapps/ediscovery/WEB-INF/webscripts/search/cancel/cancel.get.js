var searchid = context.properties["searchid"];

var connector = remote.connect("enkive"); 

connector.get("/search/cancel?searchid=" + searchid);
