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
	
	String title = null;
	if (component != null && component.getTitle() != null)
	{
		title = component.getTitle();
	}
	else
	{
		title = "Untitled Component";
	}
%>
<style type="text/css">

#chrome-header-<%=htmlBindingId%>
{
	background-color: #a9b4d5;
	color: white;
	font-family: Helvetica;
	font-size: 22px;
	padding: 5px;
	padding-left: 15px;
}

</style>

<table width="100%" cellpadding="0" cellspacing="0"> 
	<tr>
		<td id="chrome-header-<%=htmlBindingId%>" align="left" valign="top">
			<b><%=title%></b>
		</td>
	</tr>
	<tr>
		<td id="chrome-content-<%=htmlBindingId%>" align="left" valign="top">
			<surf:componentInclude/>
		</td>
	</tr>
</table>
