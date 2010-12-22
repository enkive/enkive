<div class="scrollable">
	<table id="saved_searches">
		<tr>
			<th>Execution Date</th>
			<th>Criteria</th>
		</tr>
		<#list searchList as search>
			<#if (search_index % 2) == 0>
			   	<tr class="result_even" id="${search.id}">
			<#else>
				<tr class="result_odd" id="${search.id}">
			</#if>
				<#assign searchDate = search.date?datetime("yyyy-MM-dd_HH-mm-ss-SSS")>
			  	<td class="search_result" id="${search.id}">${searchDate}</td>
			  	<td class="search_result" id="${search.id}">
			  		<table id="criteria">
				        <#list search.criteria as criteria>
					  		<tr>
							    <td><b>${criteria.parameter}:</b></td>
							    <td>${criteria.value}</td>
							</tr>
			        	</#list>
			        </table>
			    </td>
			    <td width="50px">
				    <table>
				    	<tr>
						    <td><input type="button" onClick='save_recent_search("${search.id}")' value="Save" /></td>
						    <td><input type="button" onClick='delete_recent_search("${search.id}")' value="Delete" /></td>
						</tr>
					</table>
				</td>
			</tr>
		</#list>
	</table>
</div>
