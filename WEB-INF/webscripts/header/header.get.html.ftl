<table cellpadding="5">
	<tr>
	    <td id="headerImage"><img src="${url.context}/resource/images/enkive_logo.png" alt="Enkive" /></td>
	    <td><p id="edition">${edition}</p></td>
	    <td><p id="slogan">${slogan}</p></td>
		<td id="headerUser" align="right" valign="top">
			<#if user.id != "guest">
				Hello, ${user.fullName} | <a href="${url.context}/dologout">Logout</a>
			</#if>
		</td>
	</tr>
</table>