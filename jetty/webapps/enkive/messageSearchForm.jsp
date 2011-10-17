<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Message Search</title>
</head>
<body>
	<form action="messageSearch" method="get">
		Subject: <input type="text" name="search_subject" size="30"/><br />
		Sender: <input type="text" name="search_sender" size="30"/><br />
		Recipient: <input type="text" name="search_recipient" size="30"/><br />
		Earliest Date: <input type="text" name="search_dateEarliest" size="30"/><br />
		Latest Date: <input type="text" name="search_dateLatest" size="30"/><br />
		Content: <input type="text" name="search_content" size="30"/><br /> 
		<input
			type="submit" title="Search" />
	</form>
</body>
</html>