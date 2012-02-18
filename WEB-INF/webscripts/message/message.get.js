var messageid = context.properties["messageid"];
var connector = remote.connect("enkive");

// retrieve the message
var response = connector.get("/message/retrieve?message_id=" + messageid);

if (response.status == 200) {
	var attachments = connector.get("/message/attachmentDetail?message_id="
			+ messageid);
	model.text = response;
	var attachmentsJSON = eval("(" + attachments + ")");
	model.attachments = attachmentsJSON.data;
} else
	model.text = "Your session has expired.  You must login to view this message";
