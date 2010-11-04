<div class="right">
	<form action="${url.context}/search/saved" method="get" onSubmit='save_search("${searchId}")'>
		<input type="submit" value="Save Search" />
	</form>
	<form action="${url.context}/search/export/mbox" method="get">
		<input type="hidden" name="searchid" value="${searchId}" />
		<input type="submit" value="Export Search" />
	</form>
</div>