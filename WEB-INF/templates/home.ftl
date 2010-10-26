<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>${page.title}</title>
	<link type="text/css" rel="stylesheet" href="${url.context}/resource/css/enkive.css" />
	${head}
</head>
<body>

	<div id="page">
		<div id="header">
			<@region id="header" scope="template" />
		</div>	
		
		<div id="horznav">
			<@region id="horznav" scope="template" />
		</div>
		
		<div id="content">
			<div id="center">
				<div id="picture" class="column">
					<@region id="picture" scope="page" />
				</div>
				<div id="side" class="column">
					<@region id="side" scope="page" />
				</div>
				<div id="main" class="column">
					<@region id="main" scope="page" />
				</div>
			</div>
		</div>
		<div id="footer">
			<@region id="footer" scope="template" />
		</div>
	</div>

</body>
</html>
