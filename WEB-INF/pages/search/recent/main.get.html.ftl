<table>
	<tr>
		<th>Execution Date</th>
		<th>Criteria</th>
	</tr>
	<#list searchList as search>
		<tr>
			<#assign searchDate = search.date?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
		  	<td>${searchDate}</td>
		  	<td>
		  		<table>
			        <#list search.criteria as criteria>
				  		<tr>
						    <td><b>${criteria.parameter}:</b></td>
						    <td>${criteria.value}</td>
						</tr>
		        	</#list>
		        </table>
		    </td>
		    <td>
			    <table>
			    	<tr>
					    <td><a href="${url.context}/search/recent/view?searchid=${search.id}">Details</a></td>
					</tr>
					<tr>
					    <td><input type="button" onClick='save_recent_search("${search.id}")' value="Save" /></td>
					</tr>
					<tr>
					    <td><input type="button" onClick='delete_recent_search("${search.id}")' value="Delete" /></td>
					</tr>
				</table>
			</td>
		</tr>
	</#list>
</table>
