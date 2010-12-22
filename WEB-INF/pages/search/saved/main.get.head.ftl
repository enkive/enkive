<script type="text/javascript">
	$(document).ready(function() {
	
	    $('.search_result').click(function() {
	        var id = $(this).attr("id");
	        if(id) {
	            window.location = "${url.context}/search/saved/view?searchid=" + id;
	        }
	    });
	
	});
</script>
