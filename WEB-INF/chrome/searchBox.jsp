<%@ page import="org.springframework.extensions.surf.*" %>
<%@ page import="org.springframework.extensions.surf.types.*"%>
<%@ page import="org.springframework.extensions.surf.render.*" %>
<%@ page buffer="0kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="surf" uri="http://www.springframework.org/tags/surf" %>
<%
	RenderContext context = (RenderContext) request.getAttribute("renderContext");
	
	// get the component	
	String componentId = (String) context.getValue(WebFrameworkConstants.RENDER_DATA_COMPONENT_ID);
	Component component = context.getObjectService().getComponent(componentId);
	
	String servletPath = request.getContextPath();

	String htmlBindingId = (String) context.getValue(WebFrameworkConstants.RENDER_DATA_HTMLID);
	
%>
<style type="text/css">

#chrome-content-<%=htmlBindingId%>
{
	background-color: #fff0da;
	border: solid 1px #faa735;
	color: #636363;
}
</style>

<table width="100%" cellpadding="0" cellspacing="0"> 
	<tr>
		<td id="chrome-content-<%=htmlBindingId%>" style="padding: 5px;" align="left" valign="top">
			
			<surf:componentInclude/>
			
		</td>
	</tr>
</table>
