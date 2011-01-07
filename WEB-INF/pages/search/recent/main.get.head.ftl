<script type="text/javascript">
    $('.search_result').live('click', function() {
        var id = $(this).attr("id");
        if(id) {
            window.location = "${url.context}/search/recent/view?searchid=" + id;
        }
    });	

	$('.pagingLink').live('click', function() {
	    var link = $(this).attr("href");
	    $('#main').load(link + " #main");
	    $(this).removeAttr("href");
	});
</script>
