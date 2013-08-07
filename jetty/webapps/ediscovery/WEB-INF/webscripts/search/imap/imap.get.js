var searchids = context.properties["searchids"];
var enable = context.properties["enable"];

var connector = remote.connect("enkive");

connector.get("/search/imap?searchids=" + searchids + "&enable=" + enable);
