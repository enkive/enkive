<table>
	<tr>
		<th>Name</th>
		<th>Criteria</th>
	</tr>
	<#list searchList?sort_by("date")?reverse as search>
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
					    <td><a href="${url.context}/search/saved/view?searchid=${search.id}">Details</a></td>
					</tr>
					<tr>
					    <td><input type="button" onClick='delete_saved_search("${search.id}")' value="Delete" /></td>
					</tr>
				</table>
			</td>
		</tr>
	</#list>
</table>

