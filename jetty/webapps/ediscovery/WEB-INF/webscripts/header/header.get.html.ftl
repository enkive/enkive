<div id="headerLogo" class="column">
	<table cellpadding="5">
		<tr>
		    <td id="headerImage">
			<a href="http://www.enkive.org">
				<img src="${url.context}/resource/images/enkive_logo.png" alt="Enkive" />
			</a>
		    </td>
		    <td><p id="edition">${edition}</p></td>
		    <td><p id="slogan">${slogan}</p></td>
		 </tr>
	</table>
</div>
<div id="headerUser" class="column">
	<#if user.id != "guest">
		Hello, ${user.fullName} | <a class="logout" href="${url.context}/dologout">Logout</a>
	</#if>
</div>