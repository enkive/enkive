var gn = context.properties["gn"];
var statName = context.properties["stat"];
var tsMin = context.properties["ts.min"];
var tsMax = context.properties["ts.max"];
// get a connector to the Alfresco repository endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page
var auditEntryListJSON = connector.get("/stats/statistics?gn="+gn+"&"+gn+"="+statName+"&ts.min="+tsMin+"&ts.max="+tsMax);
var auditEntryList = "'" + auditEntryListJSON + "'";
model.result = auditEntryList;
model.gn = gn;
model.statName = statName;