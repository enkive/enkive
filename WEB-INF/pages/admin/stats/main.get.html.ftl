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
	
	<#-- use $ to insert data here -->
	
		<div class="scrollable">
			<table>
			  <thead>
			  	<th><b>Gatherer</b></th>
			  </thead>
			  <tbody>
				<#-- <#list result.results?sort_by("ts") as message> -->
				<#list result.results as entry>
				  	<#if (entry_index % 2) == 0>
				    	<tr class="result_even">
				    <#else>
				    	<tr class="result_odd">
				    </#if>
				    <td>
				        ${entry.gn}
				    </td>
				  </tr>
				</#list>
				</tbody>
			</table>
		</div>
<#else>
	<p>
		<b>There was an error retrieving audit entries</b><br />
	</p>
</#if>
