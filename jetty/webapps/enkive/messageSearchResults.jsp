<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" import="java.util.Set, java.util.Map"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Message Search Results</title>
</head>
<%
	Set<String> messageIdList = (Set<String>) request
			.getAttribute("message_id_list");
	Map<String, String> searchFields = (Map<String, String>) request.getAttribute("search_fields");
%>
<body>
	<p>
		Search Fields:
		<%for (String searchField : searchFields.keySet()) {%>
		Field: <%=searchField%>  -  <%=searchFields.get(searchField)%><br />
		<% } %>
	</p>
	<%
		if (messageIdList.isEmpty()) {
	%>
	<p>No documents found.</p>
	<%
		} else {
	%>
	<p>
		Found
		<%=messageIdList.size()%>
		message<%=messageIdList.size() == 1 ? "" : "s"%>:
	<ul>
		<%
			for (String id : messageIdList) {
		%>

		<li><a href="messageRetrieve?message_id=<%=id%>" target="_blank">Message</a>
		</li>
		<%
			}
		%>
	</ul>
	</p>
	<%
		}
	%>
</body>
</html>
