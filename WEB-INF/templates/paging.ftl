<div class="paging">
	<#if paging.firstPage??>
		<a href="${uri}&${paging.firstPage}">FIRST</a>
	</#if>
	<#if paging.previousPage??>
		<a href="${uri}&${paging.previousPage}">PREVIOUS</a>
	</#if>
	<#list (paging.pagePos-4)..(paging.pagePos-1) as i>
		<#if (i >= 1)>
			<a href="${uri}&pos=${i}&size=${paging.pageSize}">${i}</a>
		</#if>
	</#list>
	${paging.pagePos}
	<#list (paging.pagePos+1)..(paging.pagePos+4) as i>
		<#if (i <= paging.total)>
			<a href="${uri}&pos=${i}&size=${paging.pageSize}">${i}</a>
		</#if>
	</#list>
	<#if paging.nextPage??>
		<a href="${uri}&${paging.nextPage}">NEXT</a>
	</#if>
	<#if paging.lastPage??>
		<a href="${uri}&${paging.lastPage}">LAST</a>
	</#if>
</div>
