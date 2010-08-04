<form accept-charset="UTF-8" method="post" action="${url.context}/dologin">
	<table>
		<tr>
			<td>User Name:</td>
			<td><input name="username" type="text" /></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input name="password" type="password" /></td>
		</tr>
	</table>
	
	<input type="submit" value="Log In" />
	<input name="success" type="hidden" value="${url.url}" />
	<input name="failure" type="hidden" value="${url.url}" />

</form>
