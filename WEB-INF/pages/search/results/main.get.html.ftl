<#if result.errors??>
<p>
Errors:
<ul>
  <#list result.errors as error>
    <li>${error}</li>
  </#list>
</ul>
</p>
</#if>
<p>



<p>
Query:
<ul>
  <#list result.query?keys as key>
    <li>${key} : ${result.query[key]}</li>
  </#list>
</ul>
</p>

<#if result.results??>

<p>
Found ${result.results.count} messages matching the query.
</p>

<#if (result.results.count > 0)>

<table>
  <tr>
    <td width="25%">Date</td>
    <td width="75%">Subject</td>
  </tr>
  
<#list result.results.messages?sort_by("datenumber") as message>
  <tr>
    <td>
      <a href="${url.context}/message?messageid=${message.id}">
        ${message.date}
      </a>
    </td>
    <td>
      <a href="${url.context}/message?messageid=${message.id}">
        ${message.subject}
      </a>
    </td>
  </tr>
</#list>

</table>

</#if>

</#if>
