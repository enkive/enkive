var searchids = context.properties["searchids"];

var connector = remote.connect("enkive");

connector.get("/search/update?searchids=" + searchids);
