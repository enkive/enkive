<script type="text/javascript">
    $('tr.search_result td').live('click', function() {
        var id = $(this).parent().attr("id");
        if(id && !$(this).hasClass('search_action')) {
            window.location = "${url.context}/search/saved/view?searchid=" + id;
        }
    });	
	$('.pagingLink').live('click', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	    $(this).removeAttr("href");
	});
</script>
