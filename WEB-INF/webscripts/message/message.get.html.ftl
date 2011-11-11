<#list attachments as attachment>
    <a href=${url.context}/attachment?attachmentid=${attachment.UUID}>${attachment.filename}</a><br />
</#list>
<div class="scrollable">
	<pre>
${text?html}
	</pre>
</div>
