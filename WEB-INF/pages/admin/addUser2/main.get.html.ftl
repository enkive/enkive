<form accept-charset="UTF-8" method="post" action="/alfresco/service/enkive/permissions/add">
	<table id="create_enkive_access_table" align="center">
		<tr>
			<td>Is this user an Admin?:</td>
			<td><input name="admin" type="checkbox" /></td>
		</tr>
		<tr>
			<td>List of email addresses this user can access:</td>
			<td><input name="addresses" type="text" /></td>
		</tr>
		<tr>
			<td align="center" colspan="2">
				<input id="submit_btn" type="submit" alt="Submit" />
			</td>
		</tr>
	</table>
	<input type="hidden" name="userName" value="${userName}" />
</form>
