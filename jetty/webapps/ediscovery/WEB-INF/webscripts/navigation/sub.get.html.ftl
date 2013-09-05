<ul>
<#list pages as page>
   <#if context.page.id == page.id>
      <li><a class="current" href="${url.context}/${page.id}"><b>${page.title}</b></a></li>
   <#else>
      <li><a class="normal" href="${url.context}/${page.id}"><b>${page.title}</b></a></li>
   </#if>
</#list>
</ul>
