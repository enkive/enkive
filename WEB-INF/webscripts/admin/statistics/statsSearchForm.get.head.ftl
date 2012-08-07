<link rel="stylesheet" href="${url.context}/resource/css/jqueryui/jquery.ui.all.css">
<script src="${url.context}/resource/javascript/jquery.ui.core.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.widget.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.datepicker.js"></script>
<script src="${url.context}/resource/javascript/searchForm.js"></script>
<script src="${url.context}/resource/javascript/searchActions.js"></script>
<script>
	$(function() {
		$( "#dateEarliestField" ).datepicker({
			showOn: "button",
			buttonImage: "${url.context}/resource/images/cal.gif",
			buttonImageOnly: true,
			changeMonth: true,
			changeYear: true,
			dateFormat: 'yy-mm-dd'
		});
		$( "#dateLatestField" ).datepicker({
			showOn: "button",
			buttonImage: "${url.context}/resource/images/cal.gif",
			buttonImageOnly: true,
			changeMonth: true,
			changeYear: true,
			dateFormat: 'yy-mm-dd'
		});
	});
	
	function loadStatGraph() {
		var checkBoxes = $("input[name=methods]");
	    var statType = "";
	    $.each(checkBoxes, function() {
	    	if ($(this).attr('checked')){
	           statType = statType + "\"" + $(this).val() + "\"" + ",";
	        }
		});
		statType = statType.slice(0, -1);	
		
	    var gatherer = $("#gnField").val();
	    var stat = $("#statField").val();
	    var tsMin = $("#dateEarliestField").val();
	    var tsMax = $("#dateLatestField").val();
	    var grain = $("#grainField").val();
	    var queryString = '?gn=' + encodeURIComponent(gatherer) + '&stat='
	        + encodeURIComponent(stat)  + '&ts.min='
	        + encodeURIComponent(tsMin) + '&ts.max='
	        + encodeURIComponent(tsMax) + '&gTyp='
	        + encodeURIComponent(grain) + '&methods='
	        + encodeURIComponent(statType);
	    $('#graph').html('<center>' +
	    '<p><b>Search is in progress...</b></p><br />' +
	    '<img src=/ediscovery/resource/images/spinner.gif alt="Waiting for results" />' +
	    '</center>');
	    $('#graph')
	        .load(
	            '/ediscovery/admin/stats/graph' + queryString,
	            function(response, status, xhr) {
	                if (xhr.status == 403) {
	                   $("#main")
	                    .html("You are not authorized to graph, your session has likely expired. Redirecting to login...");
	                     location.reload();
	                 } else if (status != "success") {
	                     $("#main")
	                          .html(
	                               "There was an error retrieving search results.  Please contact your administrator.");
	                 }
	     });
	    return false;
	}
</script>
<style>
	input.searchField, select.searchField
	{ 
		width:210px;
	}
	#dateEarliestField, #dateLatestField
	{
		width:185px;
	}
</style>
