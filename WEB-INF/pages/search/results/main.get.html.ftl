<#list messageList as message>
	<a href="${url.context}/message?messageid=${message.id}">${message.id}</a>
	<br />
</#list>

