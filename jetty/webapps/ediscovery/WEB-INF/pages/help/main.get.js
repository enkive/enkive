// get a connector to the Enkive endpoint
var connector = remote.connect("enkive");
// retrieve the version
var version = connector.get("/admin/version");

var resultJSON = eval("(" + version + ")");
model.versionLocal = resultJSON.versionLocal;
model.versionRemote = resultJSON.versionRemote;
model.versionUpgrade = resultJSON.versionUpgrade;

