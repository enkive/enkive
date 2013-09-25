var searchids = context.properties["searchids"];
var enable = context.properties["enable"];
var name = context.properties["name"];

var connector = remote.connect("enkive"); 

connector.get("/search/save?searchids=" + searchids + "&enable=" + enable +
		"&name=" + encodeURIComponent(name));
