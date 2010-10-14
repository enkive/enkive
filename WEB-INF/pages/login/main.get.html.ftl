<div id="emails_tile"></div>
<form accept-charset="UTF-8" method="post" action="${url.context}/dologin">
	<table id="login_table" align="center">
		<tr>
			<td align="center" colspan="2"><h1>Welcome To Enkive</h1></td>
		</tr>
		<tr>
			<td>User Name:</td>
			<td><input name="username" type="text" /></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input name="password" type="password" /></td>
		</tr>
		<tr>
			<td align="center" colspan="2">
				<input id="login_btn" type="image" src="${url.context}/resource/images/login_btn.png" alt="Log In" />
			</td>
		</tr>
	</table>	
	<input name="success" type="hidden" value="${page.url.url}" />
	<input name="failure" type="hidden" value="${page.url.url}" />
</form>
