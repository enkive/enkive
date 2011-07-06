<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isErrorPage="true"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Enkive: Error Reporting Page</title>
</head>
<body>
	<h1>Enkive: Error Report</h1>
	<h2>
		HTTP Error:
		<%=pageContext.getErrorData().getStatusCode()%></h2>
	<%
		if (exception != null) {
	%>
	<h2>
		Message:
		<%=exception.getMessage()%></h2>
	<h3>
		Exception:
		<%=exception.getClass().getName()%></h3>
	<%
		}
	%>
	<p>See server logs for details.</p>
</body>
</html>