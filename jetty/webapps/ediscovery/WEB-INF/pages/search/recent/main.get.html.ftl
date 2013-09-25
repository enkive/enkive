<#if status.code == 200>
	<script type="text/javascript">
$(function() {
	$( "#imap-searches" ).button().click(function() {
		imap_searches()
	});
	$( "#save-searches" ).button().click(function() {
		save_recent_searches()
	});
	$( "#delete-searches" ).button().click(function() {
		delete_recent_searches()
	});
});
	</script>
	<div class="scrollable">
		<table id="saved_searches">
			<tr>
				<th><input type="checkbox" onclick="toggleChecked(this.checked)" /></th>
				<th>Saved</th>
				<th>IMAP</th>
				<th>
					<a class="sortable" href="${baseuri}&sortBy=sortByName&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Name</b>
					<a class="sortable" href="${baseuri}&sortBy=sortByName&sortDir=1">
						<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
					</a>
				</th>
				<th>
					<a class="sortable" href="${baseuri}&sortBy=sortByDate&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Search Date</b>
					<a class="sortable" href="${baseuri}&sortBy=sortByDate&sortDir=1">
						<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
					</a>
				</th>
				<th>Criteria</th>
				<th>
					<a class="sortable" href="${baseuri}&sortBy=sortByStatus&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Status</b>
					<a class="sortable" href="${baseuri}&sortBy=sortByStatus&sortDir=1">
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
					<td class="search_action"><input type="checkbox" class="idcheckbox"  value="${search.searchId}"/></td>
					<#if search.searchIsSaved>
						<td align="center"><img src="${url.context}/resource/images/checkmark-16px.png" alt="IsSaved" />
					<#else>
						<td> &nbsp; </td>
					</#if>
					<#if search.searchIsIMAP>
						<td align="center"><img src="${url.context}/resource/images/checkmark-16px.png" alt="IsIMAP" />
					<#else>
						<td> &nbsp; </td>
					</#if>
					<td>${search.searchName}</td>
				  	<td>${searchDate}</td>
				  	<td>
					    <#list search.criteria as criteria>
						  	<b>${criteria.parameter}:</b>${criteria.value}<br />
				        </#list>
				    </td>
				    <td>${search.status}</td>
				    <#if search.status == "RUNNING" || search.status == "QUEUED">
					<td width="50px" class="search_action">
						<table>
						    <tr>
							    <td><input type="button" onClick='stop_search("${search.searchId}")' value="Request Stop" /></td>
							</tr>
						</table>
					</td>
					</#if>
				</tr>
			</#list>
		</table>
	</div>
	<#assign baseuri = baseuri>
	<#assign sorturi = sorturi>
	<#assign paging = paging>
	<#include "*/templates/paging.ftl">
	<table>
		<tr>
			<td><button id="save-searches">Save Selected Searches</button></td>
			<td><button id="imap-searches">Make Selected Searches IMAP Folders</button></td>
			<td><button id="delete-searches">Delete Selected Searches</button></td>
		</tr>
	</table>
</#if>
