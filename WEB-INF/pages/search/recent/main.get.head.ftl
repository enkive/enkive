<script type="text/javascript">
	$(document).ready(function() {
	
	    $('#saved_searches tr').click(function() {
	        var id = $(this).attr("id");
	        if(id) {
	            window.location = "${url.context}/search/recent/view?searchid=" + id;
	        }
	    });
	
	});
</script>
