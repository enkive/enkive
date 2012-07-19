<#if status.code == 200>
	<div class="scrollable">
		<table id="saved_searches">
			<tr>
				<th><input type="checkbox" onclick="toggleChecked(this.checked)" /></th>
				<th>
					<a class="sortable" href="${uri}&sortBy=sortByDate&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Execution Date</b>
					<a class="sortable" href="${uri}&sortBy=sortByDate&sortDir=1">
						<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
					</a>
				</th>
				<th>Criteria</th>
				<th>
					<a class="sortable" href="${uri}&sortBy=sortByStatus&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Status</b>
					<a class="sortable" href="${uri}&sortBy=sortByStatus&sortDir=1">
						<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
					</a>
				</th>
			</tr>
			<#list searchList as search>
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
					<td class="search_action"><input type="checkbox" class="checkbox"/></td>
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
