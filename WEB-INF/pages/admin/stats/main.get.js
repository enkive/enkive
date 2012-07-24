var pos = context.properties["pos"];
var size = context.properties["size"];
var gn = context.properties["gn"];
var statName = context.properties[gn];
var tsMin = context.properties["ts.min"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("enkive");

// retrieve the web script index page
var auditEntryListJSON = connector.get("/stats/statistics?gn="+gn+"&"+gn+"="+statName+"&ts.min="+tsMin);

var auditEntryList = "'" + auditEntryListJSON + "'";
model.result = auditEntryList;