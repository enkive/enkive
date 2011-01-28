<div class="scrollable">
	<table id="saved_searches">
		<tr>
			<th>Execution Date</th>
			<th>Criteria</th>
			<th>Status</th>
		</tr>
		<#list searchList as search>
			<#if (search_index % 2) == 0>
			   	<tr class="result_even" id="${search.searchId}">
			<#else>
				<tr class="result_odd" id="${search.searchId}">
			</#if>
				<#assign searchDate = search.searchDate?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
			  	<td class="search_result" id="${search.searchId}">${searchDate}</td>
			  	<td class="search_result" id="${search.searchId}">
				    <#list search.criteria as criteria>
					  	<b>${criteria.parameter}:</b>${criteria.value}<br />
			        </#list>
			    </td>
			    <td class="search_status" id="${search.searchId}">${search.status}</td>	
				<td width="50px">
				    <#if search.status == "COMPLETE">
					    <table>
					    	<tr>
				    			<td class="noscript">
				    				<a class="view_search" href="${url.context}/search/saved/view?searchid=${search.searchId}">VIEW</a>
				    			</td>
							    <td><input type="button" onClick='save_recent_search("${search.searchId}")' value="Save" /></td>
							    <td><input type="button" onClick='delete_recent_search("${search.searchId}")' value="Delete" /></td>
							</tr>
						</table>
					</#if>
					<#if search.status == "RUNNING">
					    <table>
					    	<tr>
				    			<td class="noscript">
				    				<a class="stop_search" href="">STOP</a>
				    			</td>
							    <td><input type="button" onClick='stop_search("${search.searchId}")' value="Stop" /></td>
							</tr>
						</table>
					</#if>
				</td>
			</tr>
		</#list>
	</table>
</div>
<#assign uri = uri>
<#assign paging = paging>
<#include "*/templates/paging.ftl">
