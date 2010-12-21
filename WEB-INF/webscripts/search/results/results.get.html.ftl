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
	  <b>Found ${result.results.count} messages matching the query:</b>
	  <#if result.query??>
		  <#list result.query?keys as key>
		    ${key} : ${result.query[key]} &nbsp;
		  </#list>
	  </#if>
	</p>
	
	<#if result.results??>
		<div class="scrollable">
			<#if (result.results.count > 0)>
				<table>
				  <thead>
				    <th><b>Date</b></th>
				    <th><b>Sender</b></th>
				    <th><b>Recipients</b></th>
				    <th><b>Subject</b></th>
				  </thead>
				  <tbody>
					<#list result.results.messages?sort_by("datenumber") as message>
					  	<#if (message_index % 2) == 0>
					    	<tr class="result_even">
					    <#else>
					    	<tr class="result_odd">
					    </#if>
					    <td style="white-space: nowrap">
					      <a class="message" href="${url.context}/message?messageid=${message.id}">
					        ${message.date}
					      </a>
					    </td>
					    <td style="white-space: nowrap">
					      <a href="${url.context}/message?messageid=${message.id}">
					        ${message.sender}
					      </a>
					    </td>
					    <td style="white-space: nowrap">
					      <a href="${url.context}/message?messageid=${message.id}">
					      	<#list message.recipients as recipient>
					        	${recipient}<br />
					        </#list>
					      </a>
					    </td>
					    <td>
					      <a href="${url.context}/message?messageid=${message.id}">
					        ${message.subject}
					      </a>
					    </td>
					  </tr>
					</#list>
					</tbody>
				</table>
			</#if>
		</div>
		<div class="search-actions">
			<form action="${url.context}/search/saved" method="get" onSubmit='save_search("${result.searchId}")'>
				<input type="submit" value="Save Search" />
			</form>
			<form action="${url.context}/search/export/mbox" method="get">
				<input type="hidden" name="searchid" value="${result.searchId}" />
				<input type="submit" value="Export Search" />
			</form>
		</div>
	</#if>
<#elseif !firstRun??>
	<p>
		<b>There was an error retrieving search results</b><br />
		Search results may have just taken too long to return, you can check your recent searches for results later.
	</p>
</#if>
