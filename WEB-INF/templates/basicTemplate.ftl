<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>${page.title}</title>
	<link type="text/css" rel="stylesheet" href="${url.context}/resource/css/enkive.css" />
	${head}
</head>
<body>
		<div id="header">
			<@region id="header" scope="template" />
		</div>	
		
		<div id="horznav">
			<@region id="horznav" scope="template" />
		</div>
		
		<div id="page">
		
			<table width="100%">			
				<tr>
					<td valign="top" nowrap>
						<table>
							<tr>
								<td>
									<div id="vertnav">
										<@region id="vertnav" scope="page" />
									</div>
								</td>
							</tr>
							<tr>
								<td>
									<div id="searchForm">
										<@region id="searchForm" scope="page" />
									</div>
								</td>
							</tr>
						</table>
					</td>
					<td valign="top" width="100%">
						<div id="content">
							<@region id="main" scope="page" />
						</div>
					</td>
				</tr>
			</table>
		</div>
	
		<div id="footer">
			<@region id="footer" scope="template" />
		</div>
</body>
</html>