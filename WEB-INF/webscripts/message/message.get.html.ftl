<#if attachments??>
	<#list attachments as attachment>
	    <a href=${url.context}/attachment?attachmentid=${attachment.UUID?url}&fname=${attachment.filename?url}&mtype=${attachment.mimeType?url}>${attachment.filename}</a><br />
	</#list>
</#if>
<div class="scrollable">
	<pre>
${text?html}
	</pre>
</div>
