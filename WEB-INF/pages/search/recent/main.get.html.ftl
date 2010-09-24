<table>
  <tr>
    <th>Date</th>
    <th>Criteria</th>
    <th>Search Saved</th>
  </tr>
  
<#list searchList?sort_by("datenumber") as search>
  <tr>
    <td valign="top">
        ${search.date}
    </td>
    <td>
    	<table>
        <#list search.criteria as criteria>
			  <tr>
			    <td>${criteria.parameter}:</td>
			    <#list criteria.values as value>
			    <td>${value}</td>
			    </#list>
			  </tr>
        </#list>
        </table>
    </td>
    <td valign="top">
      <#if search.saved>
      	saved
      </#if>
    </td>
  </tr>
</#list>

</table>
