<ul>
<#list pages as page>
	<#assign display = true>
	<#if page.properties["authentication"] == "admin" && !user.capabilities.isAdmin >
		<#assign display = false>
	</#if>
	<#if display>
	   <#if context.page.id == page.id>
	      <li><a class="current" href="${url.context}/${page.id}"><b>${page.title}</b></a></li>
	   <#else>
	      <li><a class="normal" href="${url.context}/${page.id}"><b>${page.title}</b></a></li>
	   </#if>
	</#if>
</#list>
</ul>
