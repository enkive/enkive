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
    $(document).on('click', 'tr.search_result td', function() {
        var id = $(this).parent().attr("id");
        if(id && !$(this).hasClass('search_action')) {
            window.location = "${url.context}/search/saved/view?searchid=" + id;
        }
    });	
	$(document).on('click', '.pagingLink', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	    $(this).removeAttr("href");
	});
	$(document).on('click', '.sortable', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	});
</script>
