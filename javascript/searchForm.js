function get_results(){
	
	var content = $("#contentField").val();
	var sender = $("#senderField").val();
	var recipient = $("#recipientField").val();
	var subject = $("#subjectField").val();;
	var dateEarliest = $("#dateEarliestField").val();
	var dateLatest = $("#dateLatestField").val();
	var messageId = $("#messageIdField").val();
	
	var queryString = '?content=' + content +
						'&sender=' + sender +
						'&recipient=' + recipient +
						'&subject=' + subject +
						'&dateEarliest=' + dateEarliest +
						'&dateLatest=' + dateLatest +
						'&messageId=' + messageId;
	
	$('#results').load('/ediscovery/search/results' + queryString);
}

function clearForm(){
	$(".searchField").each(
		function(){
			$(this).val("");
		}
	);
}