<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Acceso</title>
</head>
<body>
	<%
	session.setAttribute("procedencia", true);
	%>
	<h2>TRATAMIENTO DE FICHEROS</h2>
	<form action="ServletAcceso" method="post"
		enctype="multipart/form-data">
		Formato del fichero: <select name="tipoFichero" id="fichero">
			<option value="XML">XML</option>
			<option value="CSV">CSV</option>
			<option value="XLS">XLS</option>
			<option value="JSON">JSON</option>
		</select><br>
		<hr>
		¿Qué quieres hacer con el fichero? <br> Lectura <input
			type="radio" value="lectura" name="opcion"><br>
		Escritura <input type="radio" value="escritura" name="opcion"><br>
		<%
		if (request.getAttribute("errorDatos") != null && (Boolean) request.getAttribute("errorDatos") == true) {
		%>
		<p style="color: red;">
			<strong>(*) Debe seleccionar lo que quiere hacer con los datos</strong>
		</p>
		<%
		}
		%>
		<input type="submit" value="Enviar">
	</form>
</body>
</html>