<title>Stats Graph</title>
<script type="text/javascript" src="/ediscovery/resource/javascript/d3.v2.min.js"></script>
	<script src="/ediscovery/resource/javascript/jquery.js"></script>
	<script src="/ediscovery/resource/javascript/searchActions.js"></script>
    <style type="text/css">
		.axis path,
		.axis line {
		    fill: none;
		    stroke: black;
		    shape-rendering: crispEdges;
		}
		
		.axis text {
		    font-family: helvetica;
		    font-size: 11px;
		}
		
	#StatGraphStyle {
	      background-color: #fff0da;
	      border: solid 1px #faa735;       
	      color: #636363; 
	} 
	
	#graphdisplay
	{
	float: right;
	}
	
	#inputform
	{
	float: left;
	}
	</style>
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
    var array = [];
    var strang = "";
    $.each(checkBoxes, function() {
    	if ($(this).attr('checked')){
           array.push($(this).value());
           strang = strang + " " + $(this).value(); 
        }
	});
	
	alert(strang);
	
    var gatherer = $("#gnField").val();
    var stat = $("#statField").val();
    var tsMin = $("#dateEarliestField").val();
    var tsMax = $("#dateLatestField").val();
    var grain = $("#grainField").val();
    var statType = $("#statTypeField").val();
    var queryString = '?gn=' + encodeURIComponent(gatherer) + '&stat='
            + encodeURIComponent(stat)  + '&ts.min='
            + encodeURIComponent(tsMin) + '&ts.max='
            + encodeURIComponent(tsMax) + '&gTyp='
            + encodeURIComponent(grain) + '&statType='
            + encodeURIComponent(statType);
    $('#graph').html('<center>' +
    '<p><b>Search is in progress...</b></p><br />' +
    '<img src=/ediscovery/resource/images/spinner.gif alt="Waiting for results" />' +
    '</center>');
    $('#graph')
            .load(
                    '/ediscovery/service/admin/stats/graph' + queryString,
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
	input.searchField
	{ 
		width:210px;
	}
	
	#dateEarliestField, #dateLatestField
	{
		width:185px;
	}
</style>
