<link rel="stylesheet" type="text/css" href="${url.context}/resource/css/jquery.fancybox-1.3.4.css" media="screen" />
<script src="${url.context}/resource/javascript/jquery.fancybox-1.3.4.js"></script>
<script type="text/javascript">
$(function() {
	
	$('#search_results tr').each(function(){
		var currentId = $(this).attr('id');
		$(this).fancybox({
				'width'				: '75%',
				'height'			: '75%',
				'autoScale'			: true,
				'transitionIn'		: 'none',
				'transitionOut'		: 'none',
				'type'				: 'iframe',
				'showCloseButton'	: 'true',
				'href'				: '${url.context}/message?messageid=' + currentId
		});
	});
});
</script>
