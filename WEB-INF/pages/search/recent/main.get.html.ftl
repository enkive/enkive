<#if status.code == 200>
	<div class="scrollable">
		<table id="saved_searches">
			<tr>
				<th>Execution Date</th>
				<th>Criteria</th>
				<th>Status</th>
			</tr>
			<#list searchList?sort_by("searchDate")?reverse as search>
				<#if (search_index % 2) == 0>
				   	<tr class="result_even 
				<#else>
					<tr class="result_odd 
				</#if>
				<#if search.status == "COMPLETE">
					search_result
				</#if>
				" id="${search.searchId}">
					<#assign searchDate = search.searchDate?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
				  	<td>${searchDate}</td>
				  	<td>
					    <#list search.criteria as criteria>
						  	<b>${criteria.parameter}:</b>${criteria.value}<br />
				        </#list>
				    </td>
				    <td>${search.status}</td>	
					<td width="50px" class="search_action">
					    <#if search.status == "COMPLETE" && !search.searchIsSaved>
						    <table>
						    	<tr>
					    			<td class="noscript">
					    				<a class="view_search" href="${url.context}/search/saved/view?searchid=${search.searchId}">VIEW</a>
					    			</td>
								    <td><input type="button" onClick='save_recent_search("${search.searchId}")' value="Save" /></td>
								    <td><input type="button" onClick='delete_recent_search("${search.searchId}")' value="Delete" /></td>
								</tr>
							</table>
						<#elseif search.status == "RUNNING" || search.status == "QUEUED">
						    <table>
						    	<tr>
								    <td><input type="button" onClick='stop_search("${search.searchId}")' value="Request Stop" /></td>
								</tr>
							</table>
						<#else>
							<table>
						    	<tr>
								    <td><input type="button" onClick='delete_recent_search("${search.searchId}")' value="Delete" /></td>
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
</#if>
