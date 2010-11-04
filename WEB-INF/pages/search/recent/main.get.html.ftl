<div class="scrollable">
	<table>
		<tr>
			<th>Execution Date</th>
			<th>Criteria</th>
		</tr>
		<#list searchList as search>
			<#if (search_index % 2) == 0>
			   	<tr class="result_even">
			<#else>
				<tr class="result_odd">
			</#if>
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
				    		<td>
						    	<form method="get" action="${url.context}/search/recent/view">
						    		<input type="hidden" name="searchid" value="${search.id}">
									<input type="submit" value="Details">
								</form>
							</td>
						    <td><input type="button" onClick='save_recent_search("${search.id}")' value="Save" /></td>
						    <td><input type="button" onClick='delete_recent_search("${search.id}")' value="Delete" /></td>
						</tr>
					</table>
				</td>
			</tr>
		</#list>
	</table>
</div>
