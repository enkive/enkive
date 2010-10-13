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
			    <td>${criteria.parameter}:</td>
			    <td>${criteria.value}</td>
			  </tr>
        </#list>
        </table>
    </td>
  </tr>
</#list>

</table>
