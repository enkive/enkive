<script type="text/javascript">
	$(document).ready(function() {
	
	    $('.search_result').click(function() {
	        var id = $(this).attr("id");
	        if(id) {
	            window.location = "${url.context}/search/recent/view?searchid=" + id;
	        }
	    });
	
	});
</script>
