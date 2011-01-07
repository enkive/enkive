<table width="100%">
	<tr>
		<td colspan="2" align="center">
				<input type="image" src="${url.context}/resource/images/clear_search_btn.png" alt="Clear Search" onClick="clearForm()"/>
		</td>
	</tr>
	<form name="searchInput" action="${url.context}/search" method="GET" onSubmit="return get_results()">
		<tr>
			<td>
				Content:
			</td>
			<td>
				<input type="text" name="content" id="contentField" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Sender:
			</td>
			<td>
				<input type="text" name="sender" id="senderField" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Recipient:
			</td>
			<td>
				<input type="text" name="recipient" id="recipientField" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Subject:
			</td>
			<td>
				<input type="text" name="subject" id="subjectField" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Earliest Date:
			</td>
			<td>
				<input type="text" name="dateEarliest" id="dateEarliestField" readonly="readonly" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Latest Date:
			</td>
			<td>
				<input type="text" name="dateLatest" id="dateLatestField" readonly="readonly" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td>
				Message ID:
			</td>
			<td>
				<input type="text" name="messageId" id="messageIdField" class="searchField"/>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center">
				<input type="image" src="${url.context}/resource/images/search_btn.png" alt="Search"/>
			</td>
		</tr>
	</form>
</table>


