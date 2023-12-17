<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Datos Abiertos <%=session.getAttribute("tipoFichero")%></title>
</head>
<body>
	<%
	session.setAttribute("procedencia", null);
	%>
	<h2>
		Datos Abiertos en formato
		<%=session.getAttribute("tipoFichero")%>:
	</h2>

	<%
	String operacion = (String) session.getAttribute("operacion");

	if ("lectura".equals(operacion)) {
	%>
	<form action="ServletAcceso" method="post"
		enctype="multipart/form-data">
		<input type="file" name="ficheroXLS" accept=".xls, .xlsx" /><br>
		<input type="submit" value="Enviar">
	</form>
	<%
	} else if ("escritura".equals(operacion)) {
	%>
	<form action="ServletAcceso" method="post">
		<label for="dato1">Dato 1:</label> <input type="text" name="dato1" /><br>
		<label for="dato2">Dato 2:</label> <input type="text" name="dato2" /><br>
		<label for="dato3">Dato 3:</label> <input type="text" name="dato3" /><br>
		<label for="dato4">Dato 4:</label> <input type="text" name="dato4" /><br>
		<label for="dato5">Dato 5:</label> <input type="text" name="dato5" /><br>
		<label for="dato6">Dato 6:</label> <input type="text" name="dato6" />
		<%
		if (request.getAttribute("errorDatos") != null && (Boolean) request.getAttribute("errorDatos") == true) {
		%>
		<p style="color: red;">
			<strong>(*) Los campos no pueden estar vacíos</strong>
		</p>
		<%
		}
		%>
		<br>
		<input type="submit" value="Enviar">
	</form>
	<%
	} else if ("lecturaExitosa".equals(session.getAttribute("operacion"))) {
	%>
	<h2>
		Datos del Archivo
		<%=session.getAttribute("tipoFichero")%></h2>
	<table border="1">
		<%
		List<String[]> datos = (List<String[]>) request.getAttribute("datos");
		for (int i = 0; i < datos.size(); i++) {
		%>
		<tr>
			<%
			for (String valor : datos.get(i)) {
				if (i == 0) {
			%>
			<th><%=valor%></th>
			<%
			} else {
			%>
			<td><%=valor%></td>
			<%
			}
			}
			%>
		</tr>
		<%
		}
		%>
	</table>

	<%
	} else if ("escrituraExitosa".equals(session.getAttribute("operacion"))) {
	%>
	<h2>
		El archivo <%=session.getAttribute("tipoFichero")%> se ha creado correctamente
	</h2>
	<a href="Acceso.jsp"> <input type="button" value="Volver">
	</a>
	<%
	}
	%>
</body>
</html>
