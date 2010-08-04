<form action="${url.context}/search/results" method="get">
	<table>
		<tr>
			<td>
				Keyword:
			</td>
			<td>
				<input type="text" name="keyword" />
			</td>
		</tr>
		<tr>
			<td>
				From:
			</td>
			<td>
				<input type="text" name="from" />
			</td>
			<td>
				To:
			</td>
			<td>
				<input type="text" name="to" />
			</td>
			<td>
				CC:
			</td>
			<td>
				<input type="text" name="cc" />
			</td>
		</tr>
		<tr>
			<td>
				Subject:
			</td>
			<td>
				<input type="text" name="subject" />
			</td>
		</tr>
		<tr>
			<td>
				Date:
			</td>
			<td>
				<input type="text" name="dateFrom" />
			</td>
			<td>
				to <input type="text" name="dateTo" />
			</td>
		</tr>
		<tr>
			<td>
				Message ID:
			</td>
			<td>
				<input type="text" name="messageId" />
			</td>
		</tr>
		<tr>
			<td>
				&nbsp;
			</td>
			<td>
				<input type="submit" value="Search" />
			</td>
		</tr>
	</table>
</form>
