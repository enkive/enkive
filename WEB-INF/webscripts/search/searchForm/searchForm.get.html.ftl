<table>
	<tr>
		<td colspan="2" align="center" >
				<input type="image" src="${url.context}/resource/images/clear_search_btn.png" alt="Clear Search" onClick="clearForm()"/>
		</td>
	</tr>
	<tr>
		<td>
			Content:
		</td>
		<td>
			<input type="text" id="contentField" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td>
			Sender:
		</td>
		<td>
			<input type="text" id="senderField" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td>
			Recipient:
		</td>
		<td>
			<input type="text" id="recipientField" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td>
			Subject:
		</td>
		<td>
			<input type="text" id="subjectField" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td>
			Earliest Date:
		</td>
		<td>
			<input type="text" id="dateEarliestField" disabled="disabled" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td>
			Latest Date:
		</td>
		<td>
			<input type="text" id="dateLatestField" disabled="disabled" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td>
			Message ID:
		</td>
		<td>
			<input type="text" id="messageIdField" class="searchField"/>
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
			<input type="image" src="${url.context}/resource/images/search_btn.png" alt="Search" onClick="get_results()"/>
		</td>
	</tr>
</table>

