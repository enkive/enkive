<#if status.code == 200>
	<div class="scrollable">
		<table id="saved_searches">
			<tr>
				<th><input type="checkbox" onclick="toggleChecked(this.checked)" /></th>
				<th>
					<a class="sortable" href="${uri}&sortBy=sortByName&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Name</b>
					<a class="sortable" href="${uri}&sortBy=sortByName&sortDir=1">
						<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
					</a>
				</th>
				<th>
					<a class="sortable" href="${uri}&sortBy=sortByDate&sortDir=-1">
						<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
					</a>
					<b>Search Date</b>
					<a class="sortable" href="${uri}&sortBy=sortByDate&sortDir=1">
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
			    					<a class="view_search" href="${url.context}/search/saved/view?searchid=${search.searchId}">VIEW</a>
			    				</td>
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
	<table>
		<tr>
	    	<td><input type="button" onClick='delete_saved_searches()' value="Delete Selected Searches" /></td>
		</tr>
	</table>
</#if>