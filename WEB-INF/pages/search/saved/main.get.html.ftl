<table>
	<tr>
		<th>Name</th>
		<th>Criteria</th>
	</tr>
	<#list searchList as search>
		<tr>
		  	<td>${search.name}</td>
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
					    <td>Details</td>
					</tr>
					<tr>
					    <td><input type="button" onClick='delete_saved_search("${search.id}")' value="Delete" /></td>
					</tr>
				</table>
			</td>
		</tr>
	</#list>
</table>

