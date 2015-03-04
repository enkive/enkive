<link rel="stylesheet" href="${url.context}/resource/css/jqueryui/jquery.ui.all.css">
<script src="${url.context}/resource/javascript/jquery.ui.core.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.widget.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.button.js"></script>
<script type="text/javascript">
	function toggleChecked(status) {
		$(".idcheckbox").each( function() {
		$(this).attr("checked",status);
		})
	}
    $('tr.search_result td').on('click', function() {
        var id = $(this).parent().attr("id");
        if(id && !$(this).hasClass('search_action')) {
            window.location = "${url.context}/search/imaps/view?searchid=" + id;
        }
    });	
	$('.pagingLink').on('click', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	    $(this).removeAttr("href");
	});
	$('.sortable').on('click', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	});
</script>
