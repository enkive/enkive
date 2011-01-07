<#assign uri = uri>
<#assign paging = paging>
<#include "*/templates/paging.ftl">
<div class="scrollable">
	<table id="saved_searches">
		<tr>
			<th>Name</th>
			<th>Saved Date</th>
			<th>Criteria</th>
		</tr>
		<#list searchList as search>
			<#if (search_index % 2) == 0>
			    	<tr class="result_even"  id="${search.id}">
			    <#else>
			    	<tr class="result_odd"  id="${search.id}">
			</#if>
			  	<td class="search_result" id="${search.id}">${search.name}</td>
			  	<#assign searchDate = search.date?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
			  	<td class="search_result" id="${search.id}">${searchDate}</td>
			  	<td class="search_result" id="${search.id}">
				    <#list search.criteria as criteria>
					  	<b>${criteria.parameter}:</b>${criteria.value}<br />
			        </#list>
			    </td>
			    <td width="25px">
				    <table>
				    	<tr>
				    		<noscript>
				    			<td><a class="view_search" href="${url.context}/search/saved/view?searchid=${search.id}">VIEW</a></td>
						    </noscript>
						    <td><input type="button" onClick='delete_saved_search("${search.id}")' value="Delete" /></td>
						</tr>
					</table>
				</td>
			</tr>
		</#list>
	</table>
</div>
