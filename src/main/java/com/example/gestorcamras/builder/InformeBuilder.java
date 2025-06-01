package com.example.gestorcamras.builder;

import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.model.Equipo;
import com.example.gestorcamras.model.Ubicacion;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.gestorcamras.model.FiltroAplicado;
import com.example.gestorcamras.model.ArchivoMultimedia;

/**
 * Builder para crear objetos Informe de manera flexible y reutilizable.
 * Permite la construcción paso a paso de informes con información detallada de equipos y sus cámaras.
 */
public class InformeBuilder {
    // Información básica del informe
    private String titulo;
    private LocalDateTime fechaGeneracion;
    private String contenido;
    
    // Formateador de fechas
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Información del equipo
    private Equipo equipo;
    private List<Camara> camaras;
    private List<ArchivoMultimedia> archivosMultimedia;

    /**
     * Constructor por defecto que inicializa valores por defecto.
     */
    public InformeBuilder() {
        this.fechaGeneracion = LocalDateTime.now();
        this.camaras = new ArrayList<>();
        this.archivosMultimedia = new ArrayList<>();
        this.contenido = "";
    }

    // Métodos para información básica
    
    /**
     * Establece el título del informe.
     * @param titulo Título del informe
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder conTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    /**
     * Establece la fecha de generación del informe.
     * @param fechaGeneracion Fecha de generación
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder conFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion != null ? fechaGeneracion : LocalDateTime.now();
        return this;
    }
    
    /**
     * Establece el contenido personalizado del informe.
     * @param contenido Contenido del informe
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder conContenido(String contenido) {
        this.contenido = contenido != null ? contenido : "";
        return this;
    }
    
    // Métodos para información del equipo
    
    /**
     * Establece el equipo sobre el que se genera el informe.
     * @param equipo Instancia de Equipo
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder conEquipo(Equipo equipo) {
        this.equipo = equipo;
        return this;
    }
    
    /**
     * Establece la lista de cámaras asociadas al equipo.
     * @param camaras Lista de cámaras
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder conCamaras(List<Camara> camaras) {
        if (camaras != null) {
            this.camaras = new ArrayList<>(camaras);
        }
        return this;
    }
    
    /**
     * Agrega una cámara a la lista de cámaras del informe.
     * @param camara Cámara a agregar
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder agregarCamara(Camara camara) {
        if (camara != null) {
            this.camaras.add(camara);
        }
        return this;
    }
    
    /**
     * Agrega una sección de texto al contenido del informe.
     * @param tituloSeccion Título de la sección
     * @param contenidoSeccion Contenido de la sección
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder agregarSeccion(String tituloSeccion, String contenidoSeccion) {
        if (tituloSeccion != null && contenidoSeccion != null) {
            if (!this.contenido.isEmpty()) {
                this.contenido += "\n\n";
            }
            this.contenido += String.format("=== %s ===\n%s", tituloSeccion, contenidoSeccion);
        }
        return this;
    }
    
    // Getters para acceder a los campos del informe
    
    public String getTitulo() {
        return titulo != null ? titulo : "Informe de Equipo";
    }
    
    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public Equipo getEquipo() {
        return equipo;
    }
    
    public List<Camara> getCamaras() {
        return new ArrayList<>(camaras);
    }
    
    /**
     * Establece la lista de archivos multimedia para el informe.
     * @param archivosMultimedia Lista de archivos multimedia
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder conArchivosMultimedia(List<ArchivoMultimedia> archivosMultimedia) {
        if (archivosMultimedia != null) {
            this.archivosMultimedia = new ArrayList<>(archivosMultimedia);
        } else {
            this.archivosMultimedia = new ArrayList<>();
        }
        return this;
    }
    
    /**
     * Agrega un archivo multimedia a la lista de archivos del informe.
     * @param archivo Archivo multimedia a agregar
     * @return Esta instancia para encadenamiento de métodos
     */
    public InformeBuilder agregarArchivoMultimedia(ArchivoMultimedia archivo) {
        if (archivo != null && this.archivosMultimedia != null) {
            this.archivosMultimedia.add(archivo);
        }
        return this;
    }
    
    /**
     * Construye el informe con la información configurada.
     * @return Una cadena con el contenido HTML del informe
     */
    public String construir() {
        if (equipo == null) {
            throw new IllegalStateException("No se ha proporcionado un equipo para el informe");
        }
        
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        // Encabezado del informe
        sb.append("<div class='informe-container'>");
        sb.append("<h2 class='informe-titulo'>").append(getTitulo()).append("</h2>");
        sb.append("<p class='informe-fecha'>Generado el: ").append(fechaGeneracion.format(formatter)).append("</p>");
        
        // Sección de información del equipo
        sb.append("<div class='seccion-equipo'>");
        sb.append("<h3>Información del Equipo</h3>");
        sb.append("<ul class='lista-equipo'>");
        sb.append("<li><strong>Nombre:</strong> ").append(equipo.getNombre()).append("</li>");
        sb.append("<li><strong>Identificador:</strong> ").append(equipo.getIdentificador()).append("</li>");
        sb.append("<li><strong>IP:</strong> ").append(equipo.getIp()).append("</li>");
        sb.append("<li><strong>Estado:</strong> ").append(equipo.getActivo() ? "Activo" : "Inactivo").append("</li>");
        sb.append("<li><strong>Última conexión:</strong> ").append(
            equipo.getUltimaConexion() != null ? equipo.getUltimaConexion().format(formatter) : "Nunca"
        ).append("</li>");
        sb.append("</ul>");
        sb.append("</div>");
        
        // Sección de cámaras
        if (!camaras.isEmpty()) {
            sb.append("<div class='seccion-camaras'>");
            sb.append("<h3>Cámaras Asociadas</h3>");
            sb.append("<table class='tabla-camaras'>");
            sb.append("<thead><tr>");
            sb.append("<th>ID</th>");
            sb.append("<th>Nombre</th>");
            sb.append("<th>IP</th>");
            sb.append("<th>Tipo</th>");
            sb.append("<th>Estado</th>");
            sb.append("<th>Fecha de Registro</th>");
            sb.append("<th>Ubicación</th>");
            sb.append("</tr></thead><tbody>");

            for (Camara camara : camaras) {
                sb.append("<tr>");
                sb.append("<td>").append(camara.getIdCamara() != null ? camara.getIdCamara() : "N/A").append("</td>");
                sb.append("<td>").append(camara.getNombre() != null ? camara.getNombre() : "N/A").append("</td>");
                sb.append("<td>").append(camara.getIp() != null ? camara.getIp() : "N/A").append("</td>");
                sb.append("<td>").append(camara.getTipo() != null ? camara.getTipo() : "N/A").append("</td>");
                sb.append("<td class='").append(camara.isActiva() ? "activa" : "inactiva").append("'>")
                  .append(camara.isActiva() ? "Activa" : "Inactiva").append("</td>");
                sb.append("<td>");
                if (camara.getFechaRegistro() != null) {
                    sb.append(camara.getFechaRegistro().format(formatter));
                } else {
                    sb.append("N/A");
                }
                sb.append("</td>");
                // Mostrar información detallada de la ubicación
                sb.append("<td>");
                if (camara.getUbicacion() != null) {
                    Ubicacion ubicacion = camara.getUbicacion();
                    if (ubicacion.getDireccion() != null && !ubicacion.getDireccion().isEmpty()) {
                        sb.append("<div><strong>Dirección:</strong> ").append(ubicacion.getDireccion()).append("</div>");
                    }
                    sb.append("<div><strong>Coordenadas:</strong> ")
                      .append(ubicacion.getLatitud()).append(", ").append(ubicacion.getLongitud())
                      .append("</div>");
                } else {
                    sb.append("N/A");
                }
                sb.append("</td>");
                sb.append("</tr>");
            }
            
            sb.append("</tbody></table>");
            sb.append("<p class='total-camaras'>Total de cámaras: ").append(camaras.size()).append("</p>");
            sb.append("</div>");
        } else {
            sb.append("<div class='sin-camaras'><p>No hay cámaras asociadas a este equipo.</p></div>");
        }
        
        // Sección de imágenes filtradas agrupadas por cámara
        boolean hayImagenesFiltradas = archivosMultimedia != null && 
            archivosMultimedia.stream().anyMatch(a -> a.getFiltrosAplicados() != null && !a.getFiltrosAplicados().isEmpty());
            
        if (hayImagenesFiltradas) {
            sb.append("<div class='seccion-imagenes-filtradas'>");
            sb.append("<h3><i class='fas fa-filter'></i> Imágenes con Filtros Aplicados</h3>");
            
            // Agrupar imágenes filtradas por cámara
            Map<String, List<ArchivoMultimedia>> imagenesPorCamara = archivosMultimedia.stream()
                .filter(a -> a.getFiltrosAplicados() != null && !a.getFiltrosAplicados().isEmpty())
                .collect(Collectors.groupingBy(
                    a -> a.getCamara() != null ? a.getCamara().getNombre() : "Sin cámara",
                    LinkedHashMap::new,
                    Collectors.toList()
                ));
                
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            // Crear una sección por cada cámara
            imagenesPorCamara.forEach((nombreCamara, imagenes) -> {
                sb.append(String.format("<div class='carpeta-camara'><h4><i class='fas fa-video'></i> Cámara: %s <span class='badge'>%d imágenes</span></h4>", 
                    nombreCamara, imagenes.size()));
                
                sb.append("<div class='galeria-imagenes-filtradas'>");
                
                for (ArchivoMultimedia archivo : imagenes) {
                    sb.append("<div class='tarjeta-imagen-filtrada'>");
                    sb.append("<div class='info-imagen'>");
                    sb.append(String.format("<div class='nombre-archivo'>%s</div>", 
                        archivo.getNombreArchivo() != null ? archivo.getNombreArchivo() : "Sin nombre"));
                    
                    sb.append("<div class='fecha-captura'><i class='far fa-calendar-alt'></i> ");
                    sb.append(archivo.getFechaCaptura() != null ? 
                        archivo.getFechaCaptura().format(dateFormatter) : "Fecha desconocida");
                    sb.append("</div>");
                    
                    // Mostrar filtros aplicados
                    List<FiltroAplicado> filtros = archivo.getFiltrosAplicados();
                    if (filtros != null && !filtros.isEmpty()) {
                        sb.append("<div class='filtros-aplicados'><i class='fas fa-magic'></i> ");
                        sb.append(filtros.stream()
                            .map(FiltroAplicado::getNombreFiltro)
                            .distinct()
                            .collect(Collectors.joining(", ")));
                        sb.append("</div>");
                    }
                    
                    sb.append("</div>"); // Cierre de info-imagen
                    sb.append("</div>"); // Cierre de tarjeta-imagen-filtrada
                }
                
                sb.append("</div>"); // Cierre de galeria-imagenes-filtradas
                sb.append("</div>"); // Cierre de carpeta-camara
            });
            
            sb.append("</div>"); // Cierre de seccion-imagenes-filtradas
            
            // Agregar estilos para la sección de imágenes filtradas
            sb.append("<style>");
            sb.append(".seccion-imagenes-filtradas { margin: 2rem 0; padding: 1.5rem; background: rgba(42, 58, 92, 0.2); border-radius: 8px; }");
            sb.append(".seccion-imagenes-filtradas h3 { color: var(--accent); margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.5rem; }");
            sb.append(".carpeta-camara { margin-bottom: 2rem; }");
            sb.append(".carpeta-camara h4 { color: var(--text-primary); margin: 1rem 0; display: flex; align-items: center; gap: 0.5rem; }");
            sb.append(".carpeta-camara h4 .badge { background: var(--accent); color: var(--primary-dark); padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.8em; }");
            sb.append(".galeria-imagenes-filtradas { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1.5rem; }") ;
            sb.append(".tarjeta-imagen-filtrada { background: var(--primary-light); border-radius: 8px; overflow: hidden; transition: transform 0.2s; padding: 1rem; }") ;
            sb.append(".tarjeta-imagen-filtrada:hover { transform: translateY(-3px); }");
            sb.append(".info-imagen { padding: 0; }") ;
            sb.append(".info-imagen .nombre-archivo { font-weight: 600; margin-bottom: 0.5rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }");
            sb.append(".info-imagen .fecha-captura { font-size: 0.85em; color: var(--text-secondary); margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.3rem; }");
            sb.append(".info-imagen .filtros-aplicados { font-size: 0.8em; color: var(--accent); display: flex; align-items: center; gap: 0.3rem; margin-top: 0.5rem; }");
            sb.append("</style>");
        }
        
        // Sección de archivos multimedia (después de las cámaras)
        if (archivosMultimedia != null && !archivosMultimedia.isEmpty()) {
            /* Estilos para los filtros aplicados */
            sb.append("<style>");
            sb.append(".filtros-aplicados {");
            sb.append("max-width: 200px;");
            sb.append("}");
            sb.append(".lista-filtros {");
            sb.append("list-style: none;");
            sb.append("padding: 0;");
            sb.append("margin: 0;");
            sb.append("display: flex;");
            sb.append("flex-wrap: wrap;");
            sb.append("gap: 4px;");
            sb.append("}");
            sb.append(".filtro-badge {");
            sb.append("display: inline-block;");
            sb.append("background-color: var(--primary-light);");
            sb.append("color: var(--accent);");
            sb.append("padding: 2px 8px;");
            sb.append("border-radius: 12px;");
            sb.append("font-size: 0.8em;");
            sb.append("white-space: nowrap;");
            sb.append("}");
            sb.append("</style>");
            
            // Estilos para la tabla de archivos
            sb.append("<style>");
            sb.append(".tabla-archivos {");
            sb.append("}");
            sb.append("</style>");
            
            long totalFotos = archivosMultimedia.stream()
                .filter(a -> a.getTipo() == ArchivoMultimedia.TipoArchivo.FOTO)
                .count();
            long totalVideos = archivosMultimedia.size() - totalFotos;
            
            sb.append("<div class='seccion-archivos-multimedia'>");
            sb.append("<h3>Archivos Multimedia del Equipo</h3>");
            
            // Resumen de archivos
            sb.append("<div class='info-resumen'>");
            sb.append("<p><strong>Total de archivos:</strong> ").append(archivosMultimedia.size()).append("</p>");
            sb.append("<p><strong>Fotos:</strong> ").append(totalFotos).append("</p>");
            sb.append("<p><strong>Videos:</strong> ").append(totalVideos).append("</p>");
            sb.append("</div>");
            
            // Tabla con detalles de los archivos
            sb.append("<div class='tabla-contenedor'>");
            sb.append("<table class='tabla-archivos'>");
            sb.append("<thead><tr>");
            sb.append("<th>Nombre del Archivo</th>");
            sb.append("<th>Tipo</th>");
            sb.append("<th>Fecha de Captura</th>");
            sb.append("<th>Fecha de Subida</th>");
            sb.append("<th>Cámara</th>");
            sb.append("<th>Filtros Aplicados</th>");
            sb.append("</tr></thead><tbody>");
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (ArchivoMultimedia archivo : archivosMultimedia) {
                sb.append("<tr>");
                sb.append("<td>").append(archivo.getNombreArchivo() != null ? 
                    String.format("<span class='nombre-archivo'>%s</span>", archivo.getNombreArchivo()) : 
                    "<span class='texto-desconocido'>N/A</span>").append("</td>");
                    
                String tipoClase = archivo.getTipo() != null ? 
                    archivo.getTipo().toString().toLowerCase() : "desconocido";
                sb.append(String.format("<td><span class='tipo-archivo %s'>%s</span></td>", 
                    tipoClase, 
                    archivo.getTipo() != null ? archivo.getTipo().toString() : "N/A"));
                    
                sb.append("<td>").append(archivo.getFechaCaptura() != null ? 
                    archivo.getFechaCaptura().format(dateFormatter) : 
                    "<span class='texto-desconocido'>N/A</span>").append("</td>");
                    
                sb.append("<td>").append(archivo.getFechaSubida() != null ? 
                    archivo.getFechaSubida().format(dateFormatter) : 
                    "<span class='texto-desconocido'>N/A</span>").append("</td>");
                    
                sb.append("<td>").append(archivo.getCamara() != null && archivo.getCamara().getNombre() != null ? 
                    String.format("<span class='nombre-camara'>%s</span>", archivo.getCamara().getNombre()) : 
                    "<span class='texto-desconocido'>N/A</span>").append("</td>");
                
                // Celda para filtros aplicados
                sb.append("<td class='filtros-aplicados'>");
                List<String> filtros = archivo.getNombresFiltrosAplicados();
                if (filtros != null && !filtros.isEmpty()) {
                    sb.append("<ul class='lista-filtros'>");
                    for (String filtro : filtros) {
                        sb.append(String.format("<li><span class='badge filtro-badge'>%s</span></li>", filtro));
                    }
                    sb.append("</ul>");
                } else {
                    sb.append("<span class='texto-desconocido'>Ninguno</span>");
                }
                sb.append("</td>");
                
                sb.append("</tr>");
            }
            
            sb.append("</tbody></table>");
            sb.append("</div>"); // Cierre de tabla-contenedor
            sb.append("</div>"); // Cierre de seccion-archivos-multimedia
        }
        
        // Contenido adicional si existe
        if (contenido != null && !contenido.trim().isEmpty()) {
            sb.append("<div class='contenido-adicional'>");
            sb.append("<h3>Información Adicional</h3>");
            sb.append("<div class='contenido'>").append(contenido).append("</div>");
            sb.append("</div>");
        }
        
        sb.append("</div>"); // Cierre de informe-container
        
        return sb.toString();
    }
}
