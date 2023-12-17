<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Datos Abiertos <%=session.getAttribute("tipoFichero") %></title>
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
	<!-- Formulario para lectura de archivo JSON -->
	<form action="ServletAcceso" method="post" enctype="multipart/form-data">
		<input type="file" name="ficheroJSON" accept=".json" /><br>
		 <input type="submit" value="Enviar">
	</form>
	<%
	} else if ("escritura".equals(operacion)) {
	%>
	<!-- Formulario para escritura de datos en formato JSON -->
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
	<!-- Mostrar datos del archivo JSON leído -->
	<h2>Datos del Archivo <%=session.getAttribute("tipoFichero") %></h2>
	<%
	ArrayList<String> datosJSON=(ArrayList<String>)request.getAttribute("datos");
	for(int i=0;i<datosJSON.size();i++){
	%>
		<p><%=datosJSON.get(i)%></p><br>
	<%	
	}
	%>
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