function get_results(){
	
	var keyword = $("#contentField").val();
	var from = $("#senderField").val();
	var to = $("#recipientField").val();
	var cc = "";
	var subject = $("#subjectField").val();;
	var dateFrom = $("#dateEarliestField").val();
	var dateTo = $("#dateLatestField").val();
	var messageId = $("#messageIdField").val();
	
	var queryString = '?keyword=' + keyword +
						'&from=' + from +
						'&to=' + to +
						'&cc=' + cc +
						'&subject=' + subject +
						'&dateFrom=' + dateFrom +
						'&dateTo=' + dateTo +
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