<table>
  <tr>
    <td width="20%">Date</td>
    <td width="80%">Subject</td>
  </tr>
  
<#list messageList?sort_by("datenumber") as message>
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
