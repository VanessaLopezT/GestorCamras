package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.Escritorio.model.ArchivoMultimediaDTO;
import com.example.gestorcamras.filtros.FiltroImagen;
import com.example.gestorcamras.filtros.PoolFiltros;
import com.example.gestorcamras.filtros.impl.FiltroEscalaGrises;
import com.example.gestorcamras.filtros.impl.FiltroSepia;
import com.example.gestorcamras.filtros.impl.FiltroBrillo;
import com.example.gestorcamras.filtros.impl.FiltroReducirTamano;
import com.example.gestorcamras.filtros.impl.FiltroRotar;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.service.IArchivoMultimediaService;
import com.example.gestorcamras.service.CamaraService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.gestorcamras.dto.NotificacionFiltroDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AplicarFiltros extends JFrame {
    
    private JComboBox<String> cmbCamaras;
    private JTable tablaFotos;
    private DefaultTableModel modeloTabla;
    private JButton btnAplicarFiltro;
    private JButton btnCerrar;
    private JLabel lblVistaPrevia;
    private JPanel panelVistaPrevia;
    private List<ArchivoMultimediaDTO> archivosActuales;
    
    private final transient IArchivoMultimediaService archivoMultimediaService;
    private final transient CamaraService camaraService;
    private final String servidorUrl;
    
    private Long equipoId;
    private List<Camara> camaras;
    private boolean cargandoAutomaticamente = false;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AplicarFiltros(IArchivoMultimediaService archivoMultimediaService, CamaraService camaraService, String servidorUrl) {
        this.archivoMultimediaService = archivoMultimediaService;
        this.camaraService = camaraService;
        this.servidorUrl = servidorUrl.endsWith("/") ? 
                          servidorUrl.substring(0, servidorUrl.length() - 1) : 
                          servidorUrl;
        this.archivosActuales = new ArrayList<>();
        
        SwingUtilities.invokeLater(this::inicializarUI);
    }
    
    public void mostrar() {
        setVisible(true);
    }
    
    /**
     * Muestra una ventana con las imágenes filtradas guardadas en la carpeta fotos_filtradas
     */
    private void mostrarImagenesFiltradas() {
        String projectPath = System.getProperty("user.dir");
        String outputDir = projectPath + File.separator + "fotos_filtradas";
        File directorio = new File(outputDir);
        
        // Verificar si el directorio existe
        if (!directorio.exists() || !directorio.isDirectory()) {
            JOptionPane.showMessageDialog(this, 
                "No se encontró la carpeta de imágenes filtradas.",
                "Carpeta no encontrada", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Obtener archivos de imagen del directorio
        File[] archivos = directorio.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".jpeg") ||
            name.toLowerCase().endsWith(".png")
        );
        
        if (archivos == null || archivos.length == 0) {
            JOptionPane.showMessageDialog(this, 
                "No se encontraron imágenes filtradas en la carpeta.",
                "Sin imágenes", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Crear ventana para mostrar las imágenes
        JDialog dialog = new JDialog(this, "Imágenes Filtradas", false);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        // Panel con scroll para las imágenes
        JPanel panelImagenes = new JPanel(new GridLayout(0, 3, 5, 5)); // 3 columnas
        panelImagenes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Cargar y mostrar cada imagen
        for (File archivo : archivos) {
            try {
                ImageIcon icono = new ImageIcon(archivo.getAbsolutePath());
                // Escalar la imagen manteniendo la relación de aspecto
                Image img = icono.getImage();
                int ancho = 200; // Ancho fijo para cada miniatura
                int alto = (int) (ancho * ((double) icono.getIconHeight() / icono.getIconWidth()));
                Image imgEscalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                
                JLabel lblImagen = new JLabel(new ImageIcon(imgEscalada));
                lblImagen.setBorder(BorderFactory.createTitledBorder(archivo.getName()));
                panelImagenes.add(lblImagen);
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + archivo.getName());
                e.printStackTrace();
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(panelImagenes);
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
    
    private void inicializarUI() {
        setTitle("Aplicar Filtros a Fotos");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel para el contenido principal (tabla y vista previa)
        JPanel panelContenido = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Panel superior para la selección de cámara
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.add(new JLabel("Seleccionar Cámara: "));
        cmbCamaras = new JComboBox<>();
        cmbCamaras.setPreferredSize(new Dimension(400, 25));
        panelSuperior.add(cmbCamaras);
        
        // Botón para cargar fotos de la cámara seleccionada
        JButton btnCargarFotos = new JButton("Cargar Fotos");
        btnCargarFotos.addActionListener(e -> cargarFotosPorCamara());
        panelSuperior.add(btnCargarFotos);
        
        // Panel de vista previa
        panelVistaPrevia = new JPanel(new BorderLayout());
        panelVistaPrevia.setBorder(BorderFactory.createTitledBorder("Vista Previa"));
        panelVistaPrevia.setPreferredSize(new Dimension(300, 300));
        
        lblVistaPrevia = new JLabel("Seleccione una imagen para previsualizar", JLabel.CENTER);
        panelVistaPrevia.add(lblVistaPrevia, BorderLayout.CENTER);
        
        // Panel para la tabla con título
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Imágenes Disponibles"));
        
        // Configuración de la tabla
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        modeloTabla.addColumn("ID");
        modeloTabla.addColumn("Nombre");
        modeloTabla.addColumn("Fecha");
        modeloTabla.addColumn("Tipo");
        
        tablaFotos = new JTable(modeloTabla);
        tablaFotos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Agregar listener para la selección de filas
        tablaFotos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaFotos.getSelectedRow() >= 0) {
                int selectedRow = tablaFotos.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < archivosActuales.size()) {
                    mostrarVistaPrevia(archivosActuales.get(selectedRow));
                }
                btnAplicarFiltro.setEnabled(selectedRow != -1);
            }
        });
        
        JScrollPane scrollTabla = new JScrollPane(tablaFotos);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);
        
        // Configurar el diseño del panel de contenido
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6; // 60% del ancho para la tabla
        gbc.weighty = 1.0;
        panelContenido.add(panelTabla, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.4; // 40% del ancho para la vista previa
        panelContenido.add(panelVistaPrevia, gbc);
        
        // Panel inferior para botones
        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        
        // Botón para ver imágenes filtradas (izquierda)
        JButton btnVerFiltradas = new JButton("Ver Imágenes Filtradas");
        btnVerFiltradas.addActionListener(e -> mostrarImagenesFiltradas());
        panelInferior.add(btnVerFiltradas, BorderLayout.WEST);
        
        // Panel para los botones de la derecha
        JPanel panelBotonesDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnAplicarFiltro = new JButton("Aplicar Filtro");
        btnAplicarFiltro.setEnabled(false);
        btnAplicarFiltro.addActionListener(e -> aplicarFiltroSeleccionado());
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> this.dispose());
        
        panelBotonesDerecha.add(btnAplicarFiltro);
        panelBotonesDerecha.add(btnCerrar);
        
        panelInferior.add(panelBotonesDerecha, BorderLayout.EAST);
        
        // Agregar componentes al panel principal
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelContenido, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    /**
     * Establece el ID del equipo y carga las cámaras correspondientes
     */
    public void setEquipoId(Long equipoId) {
        this.equipoId = equipoId;
        cargarCamaras();
    }
    
    private void cargarCamaras() {
        if (equipoId == null) {
            JOptionPane.showMessageDialog(this, 
                "No se ha especificado un ID de equipo válido.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        new Thread(() -> {
            try {
                List<Camara> camarasCargadas = camaraService.obtenerCamarasPorEquipo(equipoId);
                
                SwingUtilities.invokeLater(() -> {
                    cmbCamaras.removeAllItems();
                    this.camaras = camarasCargadas;
                    
                    if (camarasCargadas != null && !camarasCargadas.isEmpty()) {
                        for (Camara camara : camarasCargadas) {
                            cmbCamaras.addItem(camara.getNombre() + " (ID: " + camara.getIdCamara() + ")");
                        }
                        
                        // Seleccionar la primera cámara automáticamente
                        if (cmbCamaras.getItemCount() > 0) {
                            cmbCamaras.setSelectedIndex(0);
                            cargandoAutomaticamente = true;
                            cargarFotosPorCamara();
                        }
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Error al cargar las cámaras: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    private void cargarFotosPorCamara() {
        // Limpiar tabla actual y vista previa
        modeloTabla.setRowCount(0);
        actualizarVistaPrevia(null);
        archivosActuales.clear();
        
        if (camaras == null || camaras.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No hay cámaras disponibles para mostrar.", 
                "Sin cámaras", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedIndex = cmbCamaras.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= camaras.size()) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione una cámara válida", 
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Camara camaraSeleccionada = camaras.get(selectedIndex);
        
        new Thread(() -> {
            try {
                // Obtener solo los archivos de esta cámara y este equipo
                List<ArchivoMultimediaDTO> archivos = archivoMultimediaService
                    .obtenerArchivosPorCamara(camaraSeleccionada.getIdCamara())
                    .stream()
                    .filter(archivo -> {
                        // Filtrar por equipoId para asegurar que solo se muestren los archivos de este equipo
                        return archivo.getEquipoId() != null && archivo.getEquipoId().equals(equipoId);
                    })
                    .collect(Collectors.toList());
                
                SwingUtilities.invokeLater(() -> {
                    if (archivos == null || archivos.isEmpty()) {
                        if (!cargandoAutomaticamente) {
                            JOptionPane.showMessageDialog(this, 
                                "No se encontraron archivos para la cámara seleccionada.", 
                                "Información", JOptionPane.INFORMATION_MESSAGE);
                        }
                        cargandoAutomaticamente = false;
                        return;
                    }
                    
                    cargandoAutomaticamente = false;
                    int fotosMostradas = 0;
                    Set<String> tiposEncontrados = new HashSet<>();
                    List<ArchivoMultimediaDTO> imagenesFiltradas = new ArrayList<>();
                    // Formateador de fecha
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    
                    for (ArchivoMultimediaDTO archivo : archivos) {
                        String tipo = archivo.getTipo() != null ? archivo.getTipo().toUpperCase() : "";
                        String nombreArchivo = archivo.getNombreArchivo() != null ? archivo.getNombreArchivo().toLowerCase() : "";
                        
                        tiposEncontrados.add(tipo);
                        
                        // Verificar si el archivo es una imagen
                        if (tipo.contains("FOTO") || tipo.contains("IMAGE") || 
                            nombreArchivo.matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                            
                            // Formatear la fecha de captura de manera segura
                            String fechaFormateada = "N/A";
                            try {
                                String fechaStr = archivo.getFechaCaptura();
                                if (fechaStr != null && !fechaStr.isEmpty()) {
                                    // Intentar formatear la fecha si está en un formato conocido
                                    try {
                                        fechaFormateada = sdf.format(sdf.parse(fechaStr));
                                    } catch (Exception e) {
                                        // Si no se puede formatear, usar el valor original
                                        fechaFormateada = fechaStr;
                                    }
                                }
                            } catch (Exception e) {
                                // Si hay algún error, mostramos N/A
                                fechaFormateada = "N/A";
                            }
                            
                            modeloTabla.addRow(new Object[]{
                                archivo.getIdArchivo(),
                                archivo.getNombreArchivo(),
                                fechaFormateada,
                                tipo
                            });
                            
                            imagenesFiltradas.add(archivo);
                            fotosMostradas++;
                        }
                    }
                    
                    archivosActuales = imagenesFiltradas;
                    
                    if (fotosMostradas == 0 && !cargandoAutomaticamente) {
                        String mensaje = "No se encontraron archivos de imagen compatibles.\n\n" +
                                       "Tipos de archivo encontrados: " + 
                                       (tiposEncontrados.isEmpty() ? "Ninguno" : 
                                       String.join(", ", tiposEncontrados));
                        
                        JOptionPane.showMessageDialog(this, 
                            mensaje, 
                            "Sin imágenes compatibles", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    // Habilitar el botón de aplicar filtro si hay resultados
                    btnAplicarFiltro.setEnabled(modeloTabla.getRowCount() > 0);
                    
                    // No seleccionar automáticamente la primera imagen
                    // para evitar que se aplique un filtro sin que el usuario lo solicite
                    if (modeloTabla.getRowCount() > 0 && cargandoAutomaticamente) {
                        // Limpiar cualquier selección si estamos cargando automáticamente
                        tablaFotos.clearSelection();
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    cargandoAutomaticamente = false;
                    JOptionPane.showMessageDialog(this, 
                        "Error al cargar las fotos: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    /**
     * Muestra la vista previa de la imagen seleccionada
     */
    private void mostrarVistaPrevia(ArchivoMultimediaDTO archivo) {
        if (archivo == null || archivo.getIdArchivo() == null) {
            actualizarVistaPrevia(null);
            return;
        }
        
        new Thread(() -> {
            try {
                // Construir la URL del endpoint para obtener la imagen
                String url = servidorUrl + "/api/archivos/" + archivo.getIdArchivo();
                
                // Descargar la imagen
                java.net.URL imageUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) imageUrl.openConnection();
                connection.setRequestMethod("GET");
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new IOException("Error al descargar la imagen. Código de respuesta: " + responseCode);
                }
                
                // Leer la imagen
                try (java.io.InputStream inputStream = connection.getInputStream()) {
                    // Leer la imagen en un array de bytes
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    byte[] data = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    buffer.flush();
                    
                    // Convertir a ImageIcon
                    byte[] imageData = buffer.toByteArray();
                    ImageIcon iconoOriginal = new ImageIcon(imageData);
                    
                    // Escalar la imagen para que se ajuste al panel manteniendo la relación de aspecto
                    int anchoPanel = panelVistaPrevia.getWidth() - 40; // Margen
                    int altoPanel = panelVistaPrevia.getHeight() - 60; // Margen + espacio para el título
                    
                    if (anchoPanel <= 0) anchoPanel = 300;
                    if (altoPanel <= 0) altoPanel = 300;
                    
                    // Calcular dimensiones manteniendo la relación de aspecto
                    int ancho = iconoOriginal.getIconWidth();
                    int alto = iconoOriginal.getIconHeight();
                    double relacion = (double) ancho / alto;
                    
                    if (ancho > anchoPanel || alto > altoPanel) {
                        if ((double) anchoPanel / ancho < (double) altoPanel / alto) {
                            ancho = anchoPanel;
                            alto = (int) (ancho / relacion);
                        } else {
                            alto = altoPanel;
                            ancho = (int) (alto * relacion);
                        }
                    }
                    
                    // Crear una copia escalada de la imagen
                    Image img = iconoOriginal.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                    ImageIcon iconoEscalado = new ImageIcon(img);
                    
                    // Actualizar la interfaz en el hilo de eventos
                    SwingUtilities.invokeLater(() -> {
                        lblVistaPrevia.setIcon(iconoEscalado);
                        lblVistaPrevia.setText("");
                        panelVistaPrevia.revalidate();
                        panelVistaPrevia.repaint();
                    });
                }
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    lblVistaPrevia.setIcon(null);
                    lblVistaPrevia.setText("<html><div style='text-align: center;'>Error al cargar la imagen<br>" + 
                                          "<small>" + e.getMessage() + "</small></div>");
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Actualiza la vista previa con un mensaje o una imagen
     */
    /**
     * Notifica al servidor que se ha aplicado un filtro a una imagen
     */
    private void notificarFiltroAplicado(ArchivoMultimediaDTO archivo, String nombreFiltro) {
        if (archivo == null || archivo.getIdArchivo() == null || equipoId == null) {
            System.err.println("No se puede notificar: datos de archivo o equipo no disponibles");
            return;
        }

        new Thread(() -> {
            try {
                // Obtener el ID de la cámara seleccionada
                int selectedCamIndex = cmbCamaras.getSelectedIndex();
                if (selectedCamIndex < 0 || selectedCamIndex >= camaras.size()) {
                    throw new IllegalStateException("No hay una cámara seleccionada válida");
                }
                
                Long idCamara = camaras.get(selectedCamIndex).getIdCamara();
                
                // Crear DTO de notificación
                NotificacionFiltroDTO notificacion = new NotificacionFiltroDTO();
                notificacion.setIdArchivo(archivo.getIdArchivo());
                notificacion.setIdEquipo(equipoId);
                notificacion.setIdCamara(idCamara);
                notificacion.setNombreFiltro(nombreFiltro);
                notificacion.setNombreArchivoOriginal(archivo.getNombreArchivo());
                
                // Enviar notificación
                String url = servidorUrl + "/api/archivos/notificar-filtro";
                java.net.URL obj = new java.net.URL(url);
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                
                // Convertir objeto a JSON
                String jsonInputString = objectMapper.writeValueAsString(notificacion);
                
                try (java.io.OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = con.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("Error al notificar filtro. Código: " + responseCode);
                }
                
            } catch (Exception e) {
                System.err.println("Error al notificar filtro aplicado: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Actualiza la vista previa con un mensaje o una imagen
     */
    private void actualizarVistaPrevia(Icon icono) {
        if (icono == null) {
            lblVistaPrevia.setIcon(null);
            lblVistaPrevia.setText("<html><div style='text-align: center;'>Seleccione una imagen<br>para previsualizar</div>");
            lblVistaPrevia.setHorizontalAlignment(JLabel.CENTER);
        } else {
            lblVistaPrevia.setIcon(icono);
            lblVistaPrevia.setText("");
        }
    }
    
    private void aplicarFiltroSeleccionado() {
        int filaSeleccionada = tablaFotos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione una imagen de la tabla primero.",
                "Ninguna imagen seleccionada", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ArchivoMultimediaDTO archivo = archivosActuales.get(filaSeleccionada);
        
        // Mostrar los filtros disponibles
        String[] opcionesFiltro = {"Escala de grises", "Sepia", "Aumentar brillo", "Reducir tamaño", "Rotar 90°"};
        
        String filtroSeleccionado = (String) JOptionPane.showInputDialog(
            this,
            "Seleccione el filtro a aplicar a la imagen:\n\n" +
            "Archivo: " + archivo.getNombreArchivo() + "\n" +
            "ID: " + archivo.getIdArchivo(),
            "Aplicar Filtro a la Imagen",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcionesFiltro,
            opcionesFiltro[0]);
            
        if (filtroSeleccionado != null) {
            // Mostrar confirmación antes de aplicar el filtro
            int confirmacion = JOptionPane.showConfirmDialog(
                this,
                String.format("¿Aplicar el filtro '%s' a la imagen '%s'?", 
                    filtroSeleccionado, archivo.getNombreArchivo()),
                "Confirmar Aplicación de Filtro",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                
            if (confirmacion == JOptionPane.YES_OPTION) {
                // Aplicar el filtro en un hilo separado para no bloquear la interfaz
                new Thread(() -> {
                    try {
                        // Obtener la imagen original
                        String imageUrl = servidorUrl + "/api/archivos/" + archivo.getIdArchivo();
                        java.net.URL url = new java.net.URL(imageUrl);
                        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        
                        int responseCode = connection.getResponseCode();
                        if (responseCode != 200) {
                            throw new IOException("Error al descargar la imagen. Código: " + responseCode);
                        }
                        
                        // Leer la imagen
                        java.io.InputStream inputStream = connection.getInputStream();
                        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                        byte[] data = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, bytesRead);
                        }
                        buffer.flush();
                        
                        // Convertir a BufferedImage
                        byte[] imageData = buffer.toByteArray();
                        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageData);
                        javax.imageio.ImageIO.setUseCache(false);
                        java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(bais);
                        
                        if (originalImage == null) {
                            throw new IOException("No se pudo decodificar la imagen");
                        }
                        
                        // Aplicar el filtro seleccionado usando el pool de filtros
                        java.awt.image.BufferedImage filteredImage = null;
                        FiltroImagen filtro = null;
                        
                        try {
                            switch (filtroSeleccionado) {
                                case "Escala de grises":
                                    filtro = PoolFiltros.obtenerFiltro(FiltroEscalaGrises.class);
                                    filteredImage = filtro.aplicar(originalImage);
                                    break;
                                case "Sepia":
                                    filtro = PoolFiltros.obtenerFiltro(FiltroSepia.class);
                                    filteredImage = filtro.aplicar(originalImage);
                                    break;
                                case "Aumentar brillo":
                                    filtro = PoolFiltros.obtenerFiltro(FiltroBrillo.class);
                                    filteredImage = filtro.aplicar(originalImage);
                                    break;
                                case "Reducir tamaño":
                                    filtro = PoolFiltros.obtenerFiltro(FiltroReducirTamano.class);
                                    filteredImage = filtro.aplicar(originalImage);
                                    break;
                                case "Rotar 90°":
                                    filtro = PoolFiltros.obtenerFiltro(FiltroRotar.class);
                                    filteredImage = filtro.aplicar(originalImage);
                                    break;
                                default:
                                    filteredImage = originalImage;
                            }
                        } finally {
                            if (filtro != null) {
                                PoolFiltros.liberarFiltro(filtro);
                            }
                        }
                        
                        // Crear directorio de salida si no existe
                        String projectPath = System.getProperty("user.dir");
                        String outputDir = projectPath + File.separator + "fotos_filtradas";
                        java.io.File dir = new java.io.File(outputDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        
                        // Generar nombre de archivo único
                        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                        String fileName = "filtrado_" + timestamp + "_" + archivo.getNombreArchivo();
                        // Usar ruta relativa para el mensaje
                        String filePath = "fotos_filtradas" + File.separator + fileName;
                        
                        // Guardar la imagen con filtro
                        if (archivo.getNombreArchivo().toLowerCase().endsWith(".png")) {
                            javax.imageio.ImageIO.write(filteredImage, "PNG", new java.io.File(filePath));
                        } else {
                            // Convertir a RGB para asegurar compatibilidad con JPEG
                            java.awt.image.BufferedImage rgbImage = new java.awt.image.BufferedImage(
                                filteredImage.getWidth(), 
                                filteredImage.getHeight(),
                                java.awt.image.BufferedImage.TYPE_INT_RGB);
                            rgbImage.createGraphics().drawImage(filteredImage, 0, 0, java.awt.Color.WHITE, null);
                            javax.imageio.ImageIO.write(rgbImage, "JPEG", new java.io.File(filePath));
                        }
                        
                        // Notificar al servidor después de aplicar el filtro exitosamente
                        notificarFiltroAplicado(archivo, filtroSeleccionado);
                        
                        // Mostrar mensaje de éxito en el hilo de eventos
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                this,
                                String.format("Filtro '%s' aplicado correctamente.\n\n" +
                                            "Imagen guardada en:\n%s",
                                    filtroSeleccionado, filePath),
                                "Filtro Aplicado",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(
                                this,
                                "Error al aplicar el filtro: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            )
                        );
                    }
                }).start();
            }
        }
    }
}
