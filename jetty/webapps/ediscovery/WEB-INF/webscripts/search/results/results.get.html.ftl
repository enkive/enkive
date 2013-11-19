<#if status.code != 200>
	<#-- Do Nothing -->
<#elseif result?? && !firstRun?? && result.data??>
<div id="save_dialogs">
	<script type="text/javascript">
$(function() {

	function save_submit() {
		save_recent_search('${result.data.query.searchId}', $( "#saveName" ).val());
	};
	$( "#save-dialog-form" ).dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: {
			"Save": function() {
				save_submit();
			},
			Cancel: function() {
				$( this ).dialog( "close" );
			}
		},
		close: function() { }
	});
	$('#save-dialog-form').keypress(function(e) {
		var code = (e.keyCode ? e.keyCode : e.which);
		if (code == 13) { //Enter keycode
			e.preventDefault();
			save_submit();
		}
	});

	function imap_submit() {
		imap_search('${result.data.query.searchId}', $( "#imapName" ).val());
	};
	$( "#imap-dialog-form" ).dialog({
		autoOpen: false,
		height: 300,
		width: 350,
		modal: true,
		buttons: {
			"Mark as IMAP": function() {
				imap_submit();
			},
			Cancel: function() {
				$( this ).dialog( "close" );
			}
		},
		close: function() { }
	});
	$('#imap-dialog-form').keypress(function(e) {
		var code = (e.keyCode ? e.keyCode : e.which);
		if (code == 13) { //Enter keycode
			e.preventDefault();
			imap_submit();
		}
	});

	$( "#imap-search" ).button().click(function() {
		$( "#imap-dialog-form" ).dialog( "open" );
	});
	$( "#save-search" ).button().click(function() {
		$( "#save-dialog-form" ).dialog( "open" );
	});
	$( "#update-search" ).button().click(function() {
		update_search("${result.data.query.searchId}")
	});
	$( "#unimap-search" ).button().click(function() {
		unimap_search("${result.data.query.searchId}")
	});
	$("button, input:submit, input:button").button();
});
	</script>
	<div id="save-dialog-form" title="Name Saved Search">
		<form>
			<fieldset>
				<label for="saveName">Search Name</label>
				<input
					type="text"
					name="saveName"
					id="saveName"
					class="text ui-widget-content ui-corner-all"
					<#if result.data.query.searchName??>
					value="${result.data.query.searchName}"
					<#else>
					value="name"
					</#if>
				/>
			</fieldset>
		</form>
	</div>
	<div id="imap-dialog-form" title="Name IMAP Search">
		<form>
			<fieldset>
				<label for="imapName">Search Name</label>
				<input
					type="text"
					name="imapName"
					id="imapName"
					class="text ui-widget-content ui-corner-all"
					<#if result.data.query.searchName??>
					value="${result.data.query.searchName}"
					<#else>
					value="name"
					</#if>
				/>
			</fieldset>
		</form>
	</div>
</div>
</#if>
<div id="result_list">
<#if status.code != 200>
	<#-- Do Nothing -->

<#elseif emptySearch??>
	<p>
		<b>You must fill in a search field</b>
	</p>

<#elseif !firstRun?? && result.data.status == "running">
	<p>
		<b>Your search has not yet returned results</b><br />
		When the search is complete, it will appear in the recent search list.
	</p>
<#elseif result?? && !firstRun?? && result.data??>
	<#if result.errors??>
	<p>
	Errors:
	<ul>
	  <#list result.errors as error>
	    <li>${error}</li>
	  </#list>
	</ul>
	</p>
	</#if>

	<p>
	  <h3>Found ${result.data.itemTotal} messages </h3> 
	  <#if result.data.query.parameter??>
	  <b>matching the query:</b>
		  <#list result.data.query.parameter?keys as key>
		    ${key} : ${result.data.query.parameter[key]} &nbsp;
		  </#list>
	  </#if>
	</p>
	
	<#if result.data.results??>
		<div class="scrollable">
			<#if (result.data.itemTotal > 0)>
				<table id="search_results">
				  <thead>
				    <th>
					<a class="sortable" href="${baseuri}&sortBy=sortByDate&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Date</b>
					<a class="sortable" href="${baseuri}&sortBy=sortByDate&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				    <th>
					<a class="sortable" href="${baseuri}&sortBy=sortBySender&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Sender</b>
					<a class="sortable" href="${baseuri}&sortBy=sortBySender&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				    <th>
					<a class="sortable" href="${baseuri}&sortBy=sortByReceiver&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Receiver</b>
					<a class="sortable" href="${baseuri}&sortBy=sortByReceiver&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				    <th>
					<a class="sortable" href="${baseuri}&sortBy=sortBySubject&sortDir=-1">
				    		<img src="${url.context}/resource/images/sort_arrow_desc.png" alt="Sort DESC" />
				    	</a>
				    	<b>Subject</b>
					<a class="sortable" href="${baseuri}&sortBy=sortBySubject&sortDir=1">
				    		<img src="${url.context}/resource/images/sort_arrow_asc.png" alt="Sort ASC" />
				    	</a>
				    </th>
				  </thead>
				  <tbody>
					<#list result.data.results as message>
					  	<#if (message_index % 2) == 0>
					    	<tr class="result_even message" id="${message.messageId}">
					    <#else>
					    	<tr class="result_odd message" id="${message.messageId}">
					    </#if>
				    	<td class="noscript">
				    		<a href="${url.context}/message?messageid=${message.messageId}" target="_blank">VIEW</a>
				    	</td>
					    <td style="white-space: nowrap">
					        ${message.date}
					    </td>
					    <td style="white-space: nowrap">
					    	<#if message.sender??>
					    		<#list message.sender as sender>
					        		${sender}
					        	</#list>
					        <#else>
					        	&nbsp;
					        </#if>
					    </td>
					    <td style="white-space: nowrap">
					    	<#if message.recipients??>
					      		<#list message.recipients as recipient>
					        		${recipient}<br />
					        	</#list>
					        <#else>
					        	&nbsp;
					        </#if>
					    </td>
					    <td>
					    	<#if message.subject??>
					        	${message.subject}
					        <#else>
					        	&nbsp;
					        </#if>
					    </td>
					  </tr>
					</#list>
					</tbody>
				</table>
			</#if>
		</div>
		<#assign baseuri = baseuri>
		<#assign sorturi = sorturi>
		<#assign paging = result.paging>
		<#include "*/templates/paging.ftl"> 
</div>
		<div class="search-actions">
			<button id="save-search">Save Search</button>
			<button id="update-search">Update Search</button>
			<#if result.data.query.searchIsIMAP == "true">
			<button id="unimap-search">Remove IMAP Folder</button>
			<#else>
			<button id="imap-search">Make IMAP Folder</button>
			</#if>
			<form action="${url.context}/search/export/mbox" method="get">
				<input type="hidden" name="searchid" value="${result.data.query.searchId}" />
				<input type="submit" value="Export Search" />
			</form>
		</div>
	</#if>
</#if>

