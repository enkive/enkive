<table>
  <tr>
    <th>Criteria</th>
  </tr>
  
<#list searchList as search>
  <tr>
    <td>
    	<table>
        <#list search.criteria as criteria>
			  <tr>
			  	<td>Name:</td>
			  	<td>${search.name}:</td>
			    <td>${criteria.parameter}:</td>
			    <td>${criteria.value}</td>
			    <td>ID:</td>
			    <td>${search.id}</td>
			    <td><a href="${url.context}/results/${search.lastQueryId}">Last Resultset</a></td>
			  </tr>
        </#list>
        </table>
    </td>
  </tr>
</#list>

</table>
