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
