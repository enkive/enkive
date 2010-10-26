var page = Number(context.properties["page"])
if (page <= 0)
	page = 1

var perPage = Number(context.properties["perPage"])
if (perPage < 1)
	perPage = 25

	// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");

// retrieve the web script index page
var auditEntryListJSON = connector.get("/enkive/audit/recent?page=" + page
		+ "&perPage=" + perPage);
var auditEntryList = eval("(" + auditEntryListJSON + ")");
model.result = auditEntryList

var auditTrailSize = auditEntryList.results.audit_trail_size

model.pagination = {}
model.pagination.page = page
model.pagination.perPage = perPage
model.pagination.lastPage = Math.ceil(auditTrailSize / perPage)
