<div class="scrollable">
	<table id="saved_searches">
		<tr>
			<th>Name</th>
			<th>Saved Date</th>
			<th>Criteria</th>
		</tr>
		<#list searchList as search>
			<#if (search_index % 2) == 0>
			    	<tr class="result_even"  id="${search.searchId}">
			    <#else>
			    	<tr class="result_odd"  id="${search.searchId}">
			</#if>
			  	<td class="search_result" id="${search.searchId}">${search.searchName}</td>
			  	<#assign searchDate = search.searchDate?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
			  	<td class="search_result" id="${search.searchId}">${searchDate}</td>
			  	<td class="search_result" id="${search.searchId}">
				    <#list search.criteria as criteria>
					  	<b>${criteria.parameter}:</b>${criteria.value}<br />
			        </#list>
			    </td>
			    <td width="25px">
				    <table>
				    	<tr>
			    			<td class="noscript">
			    				<a class="view_search" href="${url.context}/search/saved/view?searchid=${search.searchId}">VIEW</a>
			    			</td>
						    <td><input type="button" onClick='delete_saved_search("${search.searchId}")' value="Delete" /></td>
						</tr>
					</table>
				</td>
			</tr>
		</#list>
	</table>
</div>
<#assign uri = uri>
<#assign paging = paging>
<#include "*/templates/paging.ftl">
