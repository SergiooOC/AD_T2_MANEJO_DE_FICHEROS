<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Errores</title>
</head>
<body>

	<%
	if (request.getAttribute("error") != null) {
	%>
	<p style="color: red;">
		<strong>Error:</strong>
		<%=request.getAttribute("mensajeError")%>
	</p>
	<%
	}
	%><br>
	<a href="Acceso.jsp"> <input type="button" value="Volver">
	</a>
</body>
</html>