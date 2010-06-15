<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>${page.title}</title>
	<link type="text/css" rel="stylesheet" href="${url.context}/res/css/enkive.css" />
	${head}
</head>
<body>

	<div id="page">
	
		<div id="header" class="clearfix">
			<@region id="header" scope="template" />
		</div>	
		
		<div id="horznav" class="clearfix">
			<@region id="horznav" scope="template" />
		</div>
		
		<div id="content" class="clearfix">
		
			<table width="100%">			
				<tr>
					<td valign="top" width="75%">
						<div id="main">
							<@region id="main" scope="page" />
						</div>
					</td>
					<td valign="top" width="25%" nowrap>
						<div id="side">
							<@region id="side" scope="page" />
						</div>
					</td>
				</tr>
			</table>
			
		</div>
		
		<div id="footer" class="clearfix">
			<@region id="footer" scope="template" />
		</div>
		
	</div>

</body>
</html>
