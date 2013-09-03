var gn = context.properties["gn"];
var statName = context.properties["stat"];
var tsMin = context.properties["ts.min"];
var tsMax = context.properties["ts.max"];
var grainType = context.properties["gTyp"];
var statType = context.properties["statType"];
var methods = context.properties["methods"];
var graphType = context.properties["graphType"];
// get a connector to the Enkive endpoint
var connector = remote.connect("enkive");
// retrieve the web script index page
var queryStr = "/stats/statistics?gn=" + gn + "&" + gn + "=" + statName
		+ "&ts.min=" + tsMin + "&ts.max=" + tsMax;
if (grainType != null && grainType != undefined) {
	queryStr = queryStr + "&gTyp=" + grainType;
}
var auditEntryListJSON = connector.get("/stats/statistics?gn=" + gn + "&" + gn
		+ "=" + statName + "&ts.min=" + tsMin + "&ts.max=" + tsMax + "&gTyp="
		+ grainType);
var auditEntryList = "'" + auditEntryListJSON + "'";
if (auditEntryList != null) {
	model.result = auditEntryList;
	model.gn = gn;
	model.statName = statName;
	model.queryStr = queryStr;
	model.statType = "'" + statType + "'";
	model.methods = "[" + methods + "]";
	model.startDate = tsMin;
	model.endDate = tsMax;
	model.grain = grainType;
	model.graph = "'" + graphType + "'";
}