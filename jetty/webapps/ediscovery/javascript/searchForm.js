function get_results() {

	var content = $("#contentField").val();
	var sender = $("#senderField").val();
	var recipient = $("#recipientField").val();
	var subject = $("#subjectField").val();
	var dateEarliest = $("#dateEarliestField").val();
	var dateLatest = $("#dateLatestField").val();
	var messageId = $("#messageIdField").val();

	var queryString = '?content=' + encodeURIComponent(content) + '&sender='
			+ encodeURIComponent(sender) + '&recipient='
			+ encodeURIComponent(recipient) + '&subject='
			+ encodeURIComponent(subject) + '&dateEarliest='
			+ encodeURIComponent(dateEarliest) + '&dateLatest='
			+ encodeURIComponent(dateLatest) + '&messageId='
			+ encodeURIComponent(messageId) + '&search=true';

	$('#main').html('<center>' +
	'<p><b>Search is in progress...</b></p><br />' +
	'<img src=/ediscovery/resource/images/spinner.gif alt="Waiting for results" />' +
	'</center>');
	$('#main')
			.load(
					'/ediscovery/search/results' + queryString,
					function(response, status, xhr) {
						if (xhr.status == 403) {
							$("#main")
							.html("You are not authorized to search, your session has likely expired. Redirecting to login...");
							location.reload();
						} else if (status != "success") {
							$("#main")
									.html(
											"There was an error retrieving search results.  Please contact your administrator.");
						}
					});
	return false;
}

function clearForm() {
	$(".searchField").each(function() {
		$(this).val("");
	});
}