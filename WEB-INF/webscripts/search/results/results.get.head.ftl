<link rel="stylesheet" type="text/css" href="${url.context}/resource/css/jquery.fancybox-1.3.4.css" media="screen" />
<script src="${url.context}/resource/javascript/jquery.fancybox-1.3.4.js"></script>
<script type="text/javascript">
$(function() {
	$('a.message').fancybox({
				'width'				: '75%',
				'height'			: '75%',
				'autoScale'			: false,
				'transitionIn'		: 'none',
				'transitionOut'		: 'none',
				'type'				: 'iframe'
		});
});
</script>
