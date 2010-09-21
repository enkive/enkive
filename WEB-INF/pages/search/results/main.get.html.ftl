
<table>
  <tr>
    <th width="20%">Date</th>
    <th width="80%">Subject</th>
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
