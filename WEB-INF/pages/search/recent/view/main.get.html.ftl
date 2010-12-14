<#if result??>
	<#if result.results??>
		<div class="scrollable">
			<#if (result.results.count > 0)>
				<table >
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
					      <a href="${url.context}/message?messageid=${message.id}">
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
	</#if>
<#else>
	<p>
		<b>There was an error retrieving search results</b>
	</p>
</#if>