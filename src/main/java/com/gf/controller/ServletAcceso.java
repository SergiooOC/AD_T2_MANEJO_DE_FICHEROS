package com.gf.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Servlet implementation class ServletAcceso
 */
@MultipartConfig
public class ServletAcceso extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServletAcceso() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String ruta = "";
		HttpSession sesion = request.getSession();
		String dondeViene = sesion.getAttribute("procedencia") != null ? "Acceso" : "DatosAbiertos";
		String tipoFichero = request.getParameter("tipoFichero");
		String opcion = request.getParameter("opcion");

		// Compruebo de que jsp viene
		if (dondeViene.equals("Acceso")) {
			if (tipoFichero != null && !tipoFichero.isEmpty() && opcion != null && !opcion.isEmpty()) {
				request.setAttribute("errorDatos", null);
				if ("lectura".equals(opcion)) {
					sesion.setAttribute("operacion", "lectura");
				} else if ("escritura".equals(opcion)) {
					sesion.setAttribute("operacion", "escritura");
				}
				ruta = "DatosAbiertos" + tipoFichero + ".jsp";
				sesion.setAttribute("tipoFichero", tipoFichero);
			} else {
				request.setAttribute("errorDatos", true);
				ruta = "Acceso.jsp";
			}
			request.getRequestDispatcher(ruta).forward(request, response);
		} else {
			if (sesion.getAttribute("operacion").equals("lectura")) {
				System.out.println((String) sesion.getAttribute("tipoFichero"));
				realizarLectura((String) sesion.getAttribute("tipoFichero"), request, response);
			} else {
				try {
					realizarEscritura((String) sesion.getAttribute("tipoFichero"), request, response);
				} catch (ServletException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void realizarLectura(String tipoFichero, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		switch (tipoFichero) {
		case "CSV":
			leerArchivoCSV(request, response);
			break;
		case "XML":
			leerArchivoXML(request, response);
			break;
		case "XLS":
			leerFicheroXLS(request, response);
			break;
		case "JSON":
			leerArchivoJSON(request, response);
			break;
		}
	}

	private void realizarEscritura(String tipoFichero, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		switch (tipoFichero) {
		case "CSV":
			escribirArchivoCSV(request, response);
			break;
		case "XML":
			try {
				escribirArchivoXML(request, response);
			} catch (ServletException | IOException | ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "XLS":
			escribirArchivoXLS(request, response);
			break;
		case "JSON":
			escribirArchivoJSON(request, response);
			break;
		}
	}

	private String getFileName(Part part) {
		// Obtener el encabezado "content-disposition" del Part
		String contentDispositionHeader = part.getHeader("content-disposition");

		// Dividir el encabezado para obtener los diferentes parámetros
		String[] elements = contentDispositionHeader.split(";");
		for (String element : elements) {
			// Buscar el parámetro que contiene el nombre del archivo
			if (element.trim().startsWith("filename")) {
				// Extraer y devolver el nombre del archivo
				return element.substring(element.indexOf('=') + 1).trim().replace("\"", "");
			}
		}

		// Devolver null si no se encuentra el nombre del archivo
		return null;
	}

	private char detectarSeparador(String primeraLinea) {
		// Proporciona si el separador es ; o ,
		if (primeraLinea.contains(";")) {
			return ';';
		} else {
			return ',';
		}
	}

	private void leerArchivoCSV(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("error", null);
		request.setAttribute("mensajeError", "");
		String pagina = "";
		Part part = request.getPart("ficheroCSV");

		if (getFileName(part) == null || getFileName(part).isBlank()) {
			request.setAttribute("error", true);
			request.setAttribute("mensajeError", "(*) No has seleccionado ningun archivo");
			pagina = "Error.jsp";
		} else {
			if (getFileName(part).toLowerCase().endsWith(".csv")) {
				try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(part.getInputStream(), "UTF-8"))
						.build()) {
					String[] primeraLinea = reader.readNext();
					if (primeraLinea != null) {
						char separador = detectarSeparador(primeraLinea[0]);
						CSVReader csvReader = new CSVReaderBuilder(
								new InputStreamReader(part.getInputStream(), "UTF-8"))
								.withCSVParser(new CSVParserBuilder().withSeparator(separador).build()).build();
						List<String[]> datos = new ArrayList<>();
						String[] line;
						while ((line = csvReader.readNext()) != null) {
							datos.add(line);
						}
						request.setAttribute("datos", datos);
						request.getSession().setAttribute("operacion", "lecturaExitosa");
						pagina = "DatosAbiertosCSV.jsp";
					}
				} catch (Exception e) {
					request.setAttribute("error", true);
					request.setAttribute("mensajeError", "Error al procesar el archivo CSV.");
					request.getRequestDispatcher("Error.jsp").forward(request, response);
				}
			} else {
				request.setAttribute("error", true);
				request.setAttribute("mensajeError", "(*) El archivo que selecciones debe ser .CSV");
				pagina = "Error.jsp";
			}
		}
		request.getRequestDispatcher(pagina).forward(request, response);
	}

	private void escribirArchivoCSV(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("error", null);
		request.setAttribute("mensajeError", "");
		request.setAttribute("errorDatos", null);
		String pagina = "";
		String dato1 = (String) request.getParameter("campo1");
		String dato2 = (String) request.getParameter("campo2");
		String dato3 = (String) request.getParameter("campo3");
		String dato4 = (String) request.getParameter("campo4");
		String dato5 = (String) request.getParameter("campo5");
		String dato6 = (String) request.getParameter("campo6");
		if (comprobarDatos(dato1, dato2, dato3, dato4, dato5, dato6)) {
			// Obtener la ruta al escritorio del usuario
			String escritorioUsuario = System.getProperty("user.home") + "/Desktop/";

			// Componer la ruta completa del archivo en el escritorio
			String rutaArchivo = escritorioUsuario + "miArchivo.csv";

			String[] datosFila1 = { dato1, dato2, dato3 };
			String[] datosFila2 = { dato4, dato5, dato6 };

			try (CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(rutaArchivo))
					.withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).withSeparator(';') // Configura sin comillas
					.build()) { // '\0' significa sin comillas
				writer.writeNext(datosFila1);
				writer.writeNext(datosFila2);
				request.getSession().setAttribute("operacion", "escrituraExitosa");
				pagina = "DatosAbiertosCSV.jsp";
			} catch (IOException e) {
				request.setAttribute("error", true);
				request.setAttribute("mensajeError", "Error al crear el archivo CSV.");
				pagina = "Error.jsp";
			}
		} else {
			request.setAttribute("errorDatos", true);
			pagina = "DatosAbiertosCSV.jsp";
		}
		request.getRequestDispatcher(pagina).forward(request, response);
	}

	private boolean comprobarDatos(String dato1, String dato2, String dato3, String dato4, String dato5, String dato6) {
		// TODO Auto-generated method stub
		if ((dato1 != null && !dato1.isBlank()) && (dato2 != null && !dato2.isBlank())
				&& (dato3 != null && !dato3.isBlank()) && (dato4 != null && !dato4.isBlank())
				&& (dato5 != null && !dato5.isBlank()) && (dato6 != null && !dato6.isBlank())) {
			return true;
		} else {
			return false;
		}

	}

	private void leerFicheroXLS(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("error", null);
		request.setAttribute("mensajeError", "");
		String pagina = "";
		Part part = request.getPart("ficheroXLS");

		// Comprobamos si se ha seleccionado o no el archivo
		if (getFileName(part) == null || getFileName(part).isBlank()) {
			request.setAttribute("error", true);
			request.setAttribute("mensajeError", "(*) No has seleccionado ningún archivo");
			pagina = "Error.jsp";
		} else {
			// Comprobamos si la extensión del archivo es .xls o xlsx
			if (getFileName(part).endsWith(".xls") || getFileName(part).endsWith(".xlsx")) {
				try {
					Workbook libro; // Inicializamos el objeto Workbook
					InputStream is = part.getInputStream();
					if (getFileName(part).endsWith(".xls")) {
						libro = new HSSFWorkbook(is); // Creamos el objeto para archivos antiguos .xls
					} else {
						libro = new XSSFWorkbook(is); // Creamos el objeto para archivos nuevos .xlsx
					}
					Sheet hoja = libro.getSheetAt(0); // Creamos la hoja del libro
					List<String[]> datos = new ArrayList<>(); // Lista que va a almacenar los datos recogidos

					for (int i = 0; i <= hoja.getLastRowNum(); i++) {
						Row fila = hoja.getRow(i);

						if (fila != null) {
							int numCeldas = Math.max(0, fila.getLastCellNum()); // Comprueba que el numero de celdas no
																				// sea negativo
							String[] datosFila = new String[numCeldas];

							for (int j = 0; j < numCeldas; j++) {
								Cell celda = fila.getCell(j);
								datosFila[j] = (celda != null) ? celda.toString() : "";
							}

							datos.add(datosFila);
						}
					}

					request.setAttribute("datos", datos);
					request.getSession().setAttribute("operacion", "lecturaExitosa");
					pagina = "DatosAbiertosXLS.jsp";
					libro.close();

				} catch (IOException e) {
					e.printStackTrace();
					request.setAttribute("error", true);
					request.setAttribute("mensajeError", "Error al procesar el archivo XLS.");
					pagina = "Error.jsp";
				}
			} else {
				request.setAttribute("error", true);
				request.setAttribute("mensajeError", "(*) El archivo que selecciones debe ser .XLS");
				pagina = "Error.jsp";
			}
		}

		request.getRequestDispatcher(pagina).forward(request, response);
	}

	private void escribirArchivoXLS(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("error", null);
		request.setAttribute("mensajeError", "");
		request.setAttribute("errorDatos", null);
		String pagina = "";
		String[] datos = new String[6];

		// Va a recoger los datos y los almacena en un Array
		for (int i = 1; i <= 6; i++) {
			datos[i - 1] = (String) request.getParameter("dato" + i);
		}

		// Comprueba si hay datos nulos o vacios antes de realizar la escritura del
		// archivo
		if (comprobarDatosXML(datos)) {

			// Ruta del escritorio del usuario
			String escritorioUsuario = System.getProperty("user.home") + "/Desktop/";
			// Crea la ruta donde queremos guardar el archivo
			String rutaArchivoXLS = escritorioUsuario + "LibroNuevo.xls";

			try (Workbook libro = new HSSFWorkbook()) {
				// Creamos la hoja y la cabecera del archivo
				Sheet hoja = libro.createSheet("Hoja1");
				Row cabecera = hoja.createRow(0);
				for (int i = 1; i <= 6; i++) {
					cabecera.createCell(i - 1).setCellValue("Dato" + i);
				}
				// Creamos la primera fila con los datos recogido
				Row fila1 = hoja.createRow(1);
				for (int i = 0; i < datos.length; i++) {
					fila1.createCell(i).setCellValue(datos[i]);
				}
				// Guarda el archivo en la ruta indicada anteriormente
				try (FileOutputStream fos = new FileOutputStream(rutaArchivoXLS)) {
					libro.write(fos);
				}

				request.getSession().setAttribute("operacion", "escrituraExitosa");
				pagina = "DatosAbiertosXLS.jsp";
			} catch (IOException e) {
				request.setAttribute("error", true);
				request.setAttribute("mensajeError", "Error al crear el archivo XLS.");
				pagina = "Error.jsp";
			}
		} else {
			request.setAttribute("errorDatos", true);
			pagina = "DatosAbiertosXLS.jsp";
		}
		request.getRequestDispatcher(pagina).forward(request, response);
	}

	private boolean comprobarDatosXML(String[] datos) {
		for (String dato : datos) {
			if (dato == null || dato.isBlank()) {
				return false;
			}
		}
		return true;
	}

	private void leerArchivoXML(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Lógica para leer archivo XML utilizando DOM
		Part part = request.getPart("ficheroXML");
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(part.getInputStream()));

			// Almacena el documento en la sesión para usarlo en la JSP
			request.getSession().setAttribute("operacion", "lecturaExitosa");
			request.setAttribute("documentoXML", document);

			// Redirige a la página correspondiente para mostrar los datos leídos
			request.getRequestDispatcher("DatosAbiertosXML.jsp").forward(request, response);
		} catch (ParserConfigurationException | org.xml.sax.SAXException e) {
			// Manejo de excepciones
			e.printStackTrace();
			request.setAttribute("error", true);
			request.setAttribute("mensajeError", "Error al procesar el archivo XML.");
			request.getRequestDispatcher("Error.jsp").forward(request, response);
		}
	}

	private void escribirArchivoXML(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, ParserConfigurationException {
		// Obtén los datos del formulario
		String campo1 = request.getParameter("campo1");
		String campo2 = request.getParameter("campo2");
		String campo3 = request.getParameter("campo3");
		String campo4 = request.getParameter("campo4");
		String campo5 = request.getParameter("campo5");
		String campo6 = request.getParameter("campo6");

		if (!comprobarDatos(campo1, campo2, campo3, campo4, campo5, campo6)) {
			request.setAttribute("errorDatos", true);
			request.getRequestDispatcher("DatosAbiertosXML.jsp").forward(request, response);
			return;
		}

		// Crear el documento XML
		Document document = crearDocumentoXML(campo1, campo2, campo3, campo4, campo5, campo6);

		// Obtener la ruta al escritorio del usuario
		String escritorioUsuario = System.getProperty("user.home") + "/Desktop/";

		// Componer la ruta completa del archivo en el escritorio
		String nombreArchivo = "miArchivo.xml";
		String rutaArchivo = escritorioUsuario + nombreArchivo;

		try {
			// Especifica la ruta y el nombre del archivo
			try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
				// Convertir el documento a una cadena XML
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(document);

				StreamResult result = new StreamResult(writer);
				transformer.transform(source, result);

				// Indica que la operación de escritura fue exitosa
				request.getSession().setAttribute("operacion", "escrituraExitosa");
				// Guardamos la ruta del archivo en la sesión para poder mostrarla después
				request.getSession().setAttribute("rutaArchivoXML", rutaArchivo);
				// Redirige a la página correspondiente
				request.getRequestDispatcher("DatosAbiertosXML.jsp").forward(request, response);
			}
		} catch (TransformerException | IOException e) {
			// Manejo de excepciones
			e.printStackTrace();
			request.setAttribute("error", true);
			request.setAttribute("mensajeError", "Error al crear o escribir el archivo XML.");
			request.getRequestDispatcher("Error.jsp").forward(request, response);
		}
	}

	private Document crearDocumentoXML(String campo1, String campo2, String campo3, String campo4, String campo5,
			String campo6) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		// Crear el elemento raíz (AccesoDatos)
		Element elementoRaiz = document.createElement("AccesoDatos");
		document.appendChild(elementoRaiz);

		// Crear el elemento XML
		Element elementoXML = document.createElement("XML");
		elementoRaiz.appendChild(elementoXML);

		// Crear los elementos Campo1, Campo2, ..., Campo6 y agregar los datos
		crearElemento(document, elementoXML, "Campo1", campo1);
		crearElemento(document, elementoXML, "Campo2", campo2);
		crearElemento(document, elementoXML, "Campo3", campo3);
		crearElemento(document, elementoXML, "Campo4", campo4);
		crearElemento(document, elementoXML, "Campo5", campo5);
		crearElemento(document, elementoXML, "Campo6", campo6);

		return document;
	}

	private void crearElemento(Document document, Element padre, String nombre, String valor) {
		Element elemento = document.createElement(nombre);
		elemento.appendChild(document.createTextNode(valor));
		padre.appendChild(elemento);
	}

	private void leerArchivoJSON(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("error", null);
		request.setAttribute("mensajeError", "");
		String pagina = "";
		JSONParser jParser = new JSONParser();
		Part part = request.getPart("ficheroJSON");// Obtenemos el archivo seleccionado a través de la request

		// Creamos un archivo temporal para utilizar FileReader con el Part obtenido de
		// la request
		File tempFile = File.createTempFile("tempFile", ".tmp");

		// Comprobamos que el Part no sea nulo
		if (getFileName(part) == null || getFileName(part).isBlank()) {
			request.setAttribute("error", true);
			request.setAttribute("mensajeError", "(*) No has seleccionado ningun archivo");
			pagina = "Error.jsp";
		} else {
			// Comprobamos que realmente el archivo sea un .json
			if (getFileName(part).toLowerCase().endsWith(".json")) {
				try (InputStream input = part.getInputStream();
						FileOutputStream output = new FileOutputStream(tempFile)) {
					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = input.read(buffer)) != -1) {
						output.write(buffer, 0, bytesRead);
					}
				}

				// Leemos el archivo JSON y creamos un ArrayList para pasarlo como atributo a la
				// jsp
				try (FileReader reader = new FileReader(tempFile)) {
					Object objeto = jParser.parse(reader);
					JSONArray jArray = (JSONArray) objeto;

					for (Object objetoDos : jArray) {
						JSONObject jObject = (JSONObject) objetoDos;

						ArrayList<String> datosJSON = new ArrayList<String>();

						for (Object key : jObject.keySet()) {
							System.out.println(key + ":" + jObject.get(key));
							datosJSON.add(key + ":" + jObject.get(key));
						}

						// Prueba de como se muestra en el jsp
						for (int i = 0; i < datosJSON.size(); i++) {
							System.out.println(datosJSON.get(i));
						}

						request.setAttribute("datos", datosJSON);
						request.getSession().setAttribute("operacion", "lecturaExitosa");
						pagina = "DatosAbiertosJSON.jsp";
					}

				} catch (IOException e) {
					request.setAttribute("error", true);
					request.setAttribute("mensajeError", "Error al procesar el archivo .json");
					request.getRequestDispatcher("Error.jsp").forward(request, response);
				} catch (org.json.simple.parser.ParseException e) {
					request.setAttribute("error", true);
					request.setAttribute("mensajeError", "Error en el parser del archivo .json");
					request.getRequestDispatcher("Error.jsp").forward(request, response);
				}
			} else {
				request.setAttribute("error", true);
				request.setAttribute("mensajeError", "(*) El archivo que selecciones debe ser .json");
				pagina = "Error.jsp";
			}
		}
		request.getRequestDispatcher(pagina).forward(request, response);
	}

	private void escribirArchivoJSON(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("error", null);
		request.setAttribute("mensajeError", "");
		request.setAttribute("errorDatos", null);
		String pagina = "";

		// Recogemos los datos escritos por el usuario
		String dato1 = (String) request.getParameter("campo1");
		String dato2 = (String) request.getParameter("campo2");
		String dato3 = (String) request.getParameter("campo3");
		String dato4 = (String) request.getParameter("campo4");
		String dato5 = (String) request.getParameter("campo5");
		String dato6 = (String) request.getParameter("campo6");

		// Comprobamos que ningún datos esté nulo o en blanco
		if (comprobarDatos(dato1, dato2, dato3, dato4, dato5, dato6)) {
			// Obtener la ruta al escritorio del usuario
			String escritorioUsuario = System.getProperty("user.home") + "/Desktop/";

			// Componer la ruta completa del archivo en el escritorio
			String rutaArchivo = escritorioUsuario + "miArchivo.json";

			// Creamos un array JSON
			JSONArray jsonArray = new JSONArray();

			// Añadimos todos los datos en el objetoJSON
			JSONObject jObject = new JSONObject();
			jObject.put("campo1", dato1);
			jObject.put("campo2", dato2);
			jObject.put("campo3", dato3);
			jObject.put("campo4", dato4);
			jObject.put("campo5", dato5);
			jObject.put("campo6", dato6);

			// Añadimos el objetoJSON al array JSON
			jsonArray.add(jObject);

			// Creamos el archivo y lo guardamos en el escritorio del usuario
			try (FileWriter file = new FileWriter(rutaArchivo)) {
				file.write(jsonArray.toJSONString());
				file.close();
				request.getSession().setAttribute("operacion", "escrituraExitosa");
				pagina = "DatosAbiertosJSON.jsp";
			} catch (IOException e) {
				request.setAttribute("error", true);
				request.setAttribute("mensajeError", "Error al crear el archivo .json");
				pagina = "Error.jsp";
			}
		} else {
			request.setAttribute("errorDatos", true);
			pagina = "DatosAbiertosJSON.jsp";
		}
		request.getRequestDispatcher(pagina).forward(request, response);
	}
}
