<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" import="java.util.List"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<%
	List<String> docIdList = (List<String>) request
			.getAttribute("doc_id_list");
	String searchTerm = (String) request.getAttribute("search_term");
%>
<body>
	<p>
		Search term:
		<%=searchTerm%></p>
	<%
		if (docIdList.isEmpty()) {
	%>
	<p>No documents found.</p>
	<%
		} else {
	%>
	<p>
		Found
		<%=docIdList.size()%>
		document<%=docIdList.size() == 1 ? "" : "s"%>:
	<ul>
		<%
			for (String id : docIdList) {
		%>

		<li><%=id%> <a href="docRetrieve?document_id=<%=id%>"
			target="_blank">binary</a> <a
			href="docRetrieveEncoded?document_id=<%=id%>" target="_blank">encoded</a>
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