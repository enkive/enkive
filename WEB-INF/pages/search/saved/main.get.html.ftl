<div class="scrollable">
	<table id="saved_searches">
		<tr>
			<th>Name</th>
			<th>Saved Date</th>
			<th>Criteria</th>
		</tr>
		<#list searchList?sort_by("date")?reverse as search>
			<#if (search_index % 2) == 0>
			    	<tr class="result_even"  id="${search.id}">
			    <#else>
			    	<tr class="result_odd"  id="${search.id}">
			</#if>
			  	<td>${search.name}</td>
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
						    <td><input type="button" onClick='delete_saved_search("${search.id}")' value="Delete" /></td>
						</tr>
					</table>
				</td>
			</tr>
		</#list>
	</table>
</div>

