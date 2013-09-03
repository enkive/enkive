<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>${page.title}</title>
	<link type="text/css" rel="stylesheet" href="${url.context}/resource/css/enkive.css" />
	<script src="${url.context}/resource/javascript/jquery.js"></script>
	<script src="${url.context}/resource/javascript/searchActions.js"></script>
	<script>
		document.write('<style>.noscript{ display: none }</style>');
	</script>
	${head}
</head>
<body>
	<div id="header">
		<@region id="header" scope="template" />
	</div>	
	<div id="topnav">
		<@region id="topnav" scope="template" />
	</div>
	<div id="subnav">
		<@region id="subnav" scope="page" />
	</div>
	<div id="page">
		<div id="content">
			<div id="left">
				<div id="searchForm">
					<@region id="searchForm" scope="page" />
				</div>
			</div>
			<div id="main">
				<@region id="main" scope="page" />
			</div>
		</div>
	</div>
	<div id="footer">
		<@region id="footer" scope="template" />
	</div>
</body>
</html>