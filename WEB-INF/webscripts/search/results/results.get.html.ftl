<#if status.code != 200>
	<#-- Do Nothing -->

<#elseif emptySearch??>
	<p>
		<b>You must fill in a search field</b>
	</p>

<#elseif !firstRun?? && result.data.status == "running">
	<p>
		<b>Your search has not yet returned results</b><br />
		When the search is complete, it will appear in the recent search list.
	</p>
<#elseif result?? && !firstRun?? && result.data??>
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
	  <h3>Found ${result.data.itemTotal} messages </h3> 
	  <#if result.data.query??>
	  <b>matching the query:</b>
		  <#list result.data.query?keys as key>
		    ${key} : ${result.data.query[key]} &nbsp;
		  </#list>
	  </#if>
	</p>
	
	<#if result.data.results??>
		<div class="scrollable">
			<#if (result.data.itemTotal > 0)>
				<table id="search_results">
				  <thead>
				    <th>
				    	<a class="sortable" href="${uri}&sortBy=sortByDate&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Date</b>
				    	<a class="sortable" href="${uri}&sortBy=sortByDate&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				    <th>
				    	<a class="sortable" href="${uri}&sortBy=sortBySender&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Sender</b>
				    	<a class="sortable" href="${uri}&sortBy=sortBySender&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				    <th>
				    	<a class="sortable" href="${uri}&sortBy=sortByReceiver&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Receiver</b>
				    	<a class="sortable" href="${uri}&sortBy=sortByReceiver&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				    <th>
				    	<a class="sortable" href="${uri}&sortBy=sortBySubject&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Subject</b>
				    	<a class="sortable" href="${uri}&sortBy=sortBySubject&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				  </thead>
				  <tbody>
					<#list result.data.results as message>
					  	<#if (message_index % 2) == 0>
					    	<tr class="result_even message" id="${message.messageId}">
					    <#else>
					    	<tr class="result_odd message" id="${message.messageId}">
					    </#if>
				    	<td class="noscript">
				    		<a href="${url.context}/message?messageid=${message.messageId}" target="_blank">VIEW</a>
				    	</td>
					    <td style="white-space: nowrap">
					        ${message.date}
					    </td>
					    <td style="white-space: nowrap">
					    	<#if message.sender??>
					    		<#list message.sender as sender>
					        		${sender}
					        	</#list>
					        <#else>
					        	&nbsp;
					        </#if>
					    </td>
					    <td style="white-space: nowrap">
					    	<#if message.recipients??>
					      		<#list message.recipients as recipient>
					        		${recipient}<br />
					        	</#list>
					        <#else>
					        	&nbsp;
					        </#if>
					    </td>
					    <td>
					    	<#if message.subject??>
					        	${message.subject}
					        <#else>
					        	&nbsp;
					        </#if>
					    </td>
					  </tr>
					</#list>
					</tbody>
				</table>
			</#if>
		</div>
		<#assign uri = uri>
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
</#if>

