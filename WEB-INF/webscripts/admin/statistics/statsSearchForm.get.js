var connector = remote.connect("enkive");
var keysJSON = connector.get("/stats/keys");
var keysList = "'" + keysJSON + "'";
model.result = keysList;
