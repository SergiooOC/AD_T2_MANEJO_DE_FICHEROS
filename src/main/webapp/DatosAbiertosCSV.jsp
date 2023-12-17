<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Datos Abiertos <%=session.getAttribute("tipoFichero") %></title>
<style>
body {
	font-family: Arial, sans-serif;
}

table {
	border-collapse: collapse;
	width: 100%;
	margin-bottom: 20px;
	box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
	overflow: hidden;
}

th, td {
	padding: 15px;
	text-align: left;
	border-bottom: 1px solid #ddd;
}

th {
	background-color: #f2f2f2;
}

tr:hover {
	background-color: #f5f5f5;
}
</style>
</head>
<body>
	<%
	session.setAttribute("procedencia", null);
	%>
	<h2>Datos Abiertos en formato <%=session.getAttribute("tipoFichero") %>:</h2>

	<%
	String operacion = (String) session.getAttribute("operacion");

	if ("lectura".equals(operacion)) {
	%>
	<!-- Formulario para lectura de archivo CSV -->
	<form action="ServletAcceso" method="post" enctype="multipart/form-data">
		<input type="file" name="ficheroCSV" accept=".csv" /><br>
		 <input type="submit" value="Enviar">
	</form>
	<%
	} else if ("escritura".equals(operacion)) {
	%>
	<!-- Formulario para escritura de datos en formato CSV -->
	<form action="ServletAcceso" method="post">
		<label for="input1">Campo 1:</label> <input type="text" name="campo1"/><br>
		<label for="input2">Campo 2:</label> <input type="text" name="campo2"/><br>
		<label for="input3">Campo 3:</label> <input type="text" name="campo3"/><br>
		<label for="input4">Campo 4:</label> <input type="text" name="campo4"/><br> 
		<label for="input5">Campo 5:</label> <input type="text" name="campo5"/><br> 
		<label for="input6">Campo 6:</label> <input type="text" name="campo6"/>
		<%
		if (request.getAttribute("errorDatos") != null && (Boolean) request.getAttribute("errorDatos") == true) {
		%>
		<div style="color: red;">
			<strong>(*) Debe introducir correctamente los datos</strong>
		</div>

		<%
		}
		%>
		<br><input type="submit" value="Enviar">
	</form>
	<%
	} else if ("lecturaExitosa".equals(session.getAttribute("operacion"))) {
	%>
	<!-- Mostrar datos del archivo CSV leído -->
	<h2>Datos del Archivo <%=session.getAttribute("tipoFichero") %></h2>
	<table>
		<thead>
			<tr>
				<%
				for (String header : ((List<String[]>) request.getAttribute("datos")).get(0)) {
				%>
				<th><%=header%></th>
				<%
				}
				%>
			</tr>
		</thead>
		<tbody>
			<%
			for (int i = 1; i < ((List<String[]>) request.getAttribute("datos")).size(); i++) {
			%>
			<tr>
				<%
				for (String cell : ((List<String[]>) request.getAttribute("datos")).get(i)) {
				%>
				<td><%=cell%></td>
				<%
				}
				%>
			</tr>
			<%
			}
			%>
		</tbody>
	</table>
	<%
	} else if ("escrituraExitosa".equals(session.getAttribute("operacion"))) {
	%>
	<h2>El archivo <%=session.getAttribute("tipoFichero") %> se ha creado correctamente</h2>
	<a href="Acceso.jsp">
    <input type="button" value="Volver">
	</a>
	<% } %>
</body>
</html>
