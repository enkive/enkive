<div class="navigation">
	<#if paging.firstPage??>
		<a href="${uri}&${paging.firstPage}">FIRST</a>
	</#if>
	<#if paging.previousPage??>
		<a href="${uri}&${paging.previousPage}">PREVIOUS</a>
	</#if>
	${paging.pagePos}
	<#if paging.nextPage??>
		<a href="${uri}&${paging.nextPage}">NEXT</a>
	</#if>
	<#if paging.lastPage??>
		<a href="${uri}&${paging.lastPage}">LAST</a>
	</#if>
</div>
