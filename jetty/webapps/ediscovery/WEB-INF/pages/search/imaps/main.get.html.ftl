<#if status.code == 200>
	<script type="text/javascript">
$(function() {
	$( "#unimap-searches" ).button().click(function() {
		unimap_searches()
	});
});
	</script>
	<div class="scrollable">
		<table id="imap_searches">
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
			</tr>
			<#list searchList as search>
				<#if (search_index % 2) == 0>
				    	<tr class="result_even search_result"  id="${search.searchId}">
			    	<#else>
			    		<tr class="result_odd search_result"  id="${search.searchId}">
				</#if>
					<td class="search_action"><input type="checkbox" class="idcheckbox" value="${search.searchId}"/></td>
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
			  		<#assign searchDate = search.searchDate?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
			  		<td>${searchDate}</td>
			  		<td>
				    	<#list search.criteria as criteria>
					  		<b>${criteria.parameter}:</b>${criteria.value}<br />
			        	</#list>
			    	</td>
			    	<td width="25px" class="noscript">
				    	<table>
				    		<tr>
			    				<td>
			    					<a class="view_search" href="${url.context}/search/imaps/view?searchid=${search.searchId}">VIEW</a>
			    				</td>
							</tr>
						</table>
					</td>
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
			<td><button id="unimap-searches">Remove Selected IMAP Folders</button></td>
		</tr>
	</table>
</#if>
