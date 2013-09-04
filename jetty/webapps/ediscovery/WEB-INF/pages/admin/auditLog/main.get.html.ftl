<#if result??>
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
	
	<#-- if there is a search on the audit log, include query info here? -->
	
	<#if result.results??>
		<#if (result.results.audit_entries?size > 0) >
			<div class="scrollable">
				<table>
				  <thead>
				    <th><b>ID</b></th>
				    <th><b>Timestamp</b></th>
				    <th><b>Event Code</b></th>
				    <th><b>User Name</b></th>
				    <th><b>Description</b></th>
				  </thead>
				  <tbody>
					<#-- <#list result.results.audit_entries?sort_by("datenumber") as message> -->
					<#list result.results.audit_entries as entry>
					  	<#if (entry_index % 2) == 0>
					    	<tr class="result_even">
					    <#else>
					    	<tr class="result_odd">
					    </#if>
					    <td style="text-align: right;">
					        ${entry.id}
					    </td>
					    <td style="white-space:nowrap; text-align: right;">
					        ${entry.timestamp}
					    </td>
					    <td>
					        ${entry.event_code}
					    </td>
					    <td>
					        ${entry.user_name}
					    </td>
					    <td>
					        ${entry.description}
					    </td>
					  </tr>
					</#list>
					</tbody>
				</table>
			</div>
			<#assign baseuri = baseuri>
			<#assign sorturi = sorturi>
			<#assign paging = paging>
			<#include "*/templates/paging.ftl">
		<#else>
			<p>No audit entries on this page.</p>
		</#if>
	</#if>
<#else>
	<p>
		<b>There was an error retrieving audit entries</b><br />
	</p>
</#if>
