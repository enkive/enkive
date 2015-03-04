<link rel="stylesheet" href="${url.context}/resource/css/jqueryui/jquery.ui.all.css">
<link rel="stylesheet" type="text/css" href="${url.context}/resource/css/jquery.fancybox-1.3.4.css" media="screen" />
<script src="${url.context}/resource/javascript/jquery.fancybox-1.3.4.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.core.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.widget.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.button.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.draggable.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.mouse.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.position.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.resizable.js"></script>
<script src="${url.context}/resource/javascript/jquery.ui.dialog.js"></script>
<script type="text/javascript">
	function toggleChecked(status) {
		$(".idcheckbox").each( function() {
		$(this).attr("checked",status);
		})
	}
	$('tr.message td').on('click', function(){
		var currentId = $(this).parent().attr('id');
		if(currentId && !$(this).hasClass('search_action')) {
			$(this).fancybox({
				'width'				: '75%',
				'height'			: '75%',
				'autoScale'			: true,
				'transitionIn'		: 'none',
				'transitionOut'		: 'none',
				'type'				: 'iframe',
				'showCloseButton'	: 'true',
				'href'				: '${url.context}/message?messageid=' + currentId
			}).trigger("click");
		}
	});
	
	$('.pagingLink').on('click', function() {
	    var link = $(this).attr("href");
	    $('#result_list').load(link + " #result_list");
	    $(this).removeAttr("href");
	});
	
	$('.sortable').on('click', function() {
	    var link = $(this).attr("href");
	    $('#result_list').load(link + " #result_list");
	});
</script>
