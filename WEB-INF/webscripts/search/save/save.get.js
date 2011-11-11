var searchid = context.properties["searchid"];

var connector = remote.connect("enkive"); 

connector.get("/search/save?searchid=" + searchid);
