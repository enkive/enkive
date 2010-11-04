<div class="search-actions">
	<form action="${url.context}/search/saved" method="get" onSubmit='save_search("${searchId}")'>
		<input type="submit" value="Save Search" />
	</form>
	<a href="${url.context}/search/export/mbox?searchid=${searchId}">
		Export Search
	</a>
</div>