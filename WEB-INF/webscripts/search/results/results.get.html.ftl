<#if result?? && !firstRun??>
	<#if result.errors??>
	<p>
	Errors:
	<ul>
	  <#list result.errors as error>
	    <li>${error}</li>
	  </#list>
	</ul>
	</p>
	</#if>
	
	<p>
	  <h3>Found ${result.paging.total} messages </h3> <b>matching the query:</b>
	  <#if result.data.query??>
		  <#list result.data.query?keys as key>
		    ${key} : ${result.data.query[key]} &nbsp;
		  </#list>
	  </#if>
	</p>
	
	<#if result.data.results??>
		<div class="scrollable">
			<#if (result.paging.total > 0)>
				<table id="search_results">
				  <thead>
				    <th><b>Date</b></th>
				    <th><b>Sender</b></th>
				    <th><b>Recipients</b></th>
				    <th><b>Subject</b></th>
				  </thead>
				  <tbody>
					<#list result.data.results.messages as message>
					  	<#if (message_index % 2) == 0>
					    	<tr class="result_even" id="${message.id}">
					    <#else>
					    	<tr class="result_odd" id="${message.id}">
					    </#if>
					    <td style="white-space: nowrap">
					        ${message.date}
					    </td>
					    <td style="white-space: nowrap">
					        ${message.sender}
					    </td>
					    <td style="white-space: nowrap">
					      	<#list message.recipients as recipient>
					        	${recipient}<br />
					        </#list>
					    </td>
					    <td>
					        ${message.subject}
					    </td>
					  </tr>
					</#list>
					</tbody>
				</table>
			</#if>
		</div>
		<#assign paging = result.paging>
		<#include "*/templates/paging.ftl"> 
		<div class="search-actions">
			<input type="button" onClick='save_recent_search("${result.data.searchId}")' value="Save Search">
			<form action="${url.context}/search/export/mbox" method="get">
				<input type="hidden" name="searchid" value="${result.data.searchId}" />
				<input type="submit" value="Export Search" />
			</form>
		</div>
	</#if>
<#elseif !firstRun??>
	<p>
		<b>Your search has not yet returned results</b><br />
		When the search is complete, it will appear in the recent search list.
	</p>
</#if>
