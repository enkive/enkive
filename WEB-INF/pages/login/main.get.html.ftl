<form accept-charset="UTF-8" method="post" action="${url.context}/dologin">
	<table align="center">
		<tr>
			<td>User Name:</td>
			<td><input name="username" type="text" /></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input name="password" type="password" /></td>
		</tr>
		<tr>
			<td><input type="submit" value="Log In" /></td>
		</tr>
	</table>	
	<input name="success" type="hidden" value="${page.url.url}" />
	<input name="failure" type="hidden" value="${page.url.url}" />
</form>
