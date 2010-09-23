<table>
  <tr>
    <td width="20%">Date</td>
    <td width="80%">Node ID</td>
  </tr>
  
<#list searchList?sort_by("datenumber") as search>
  <tr>
    <td>
        ${search.date}
      </a>
    </td>
    <td>
        ${search.id}
      </a>
    </td>
  </tr>
</#list>

</table>
