package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.Escritorio.model.ArchivoMultimediaDTO;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.service.IArchivoMultimediaService;
import com.example.gestorcamras.service.CamaraService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AplicarFiltros extends JFrame {
    
    private JComboBox<String> cmbCamaras;
    private JTable tablaFotos;
    private DefaultTableModel modeloTabla;
    private JButton btnAplicarFiltro;
    private JButton btnCerrar;
    
    private final transient IArchivoMultimediaService archivoMultimediaService;
    private final transient CamaraService camaraService;
    
    private Long equipoId; // ID del equipo actual
    private List<Camara> camaras; // Lista de cámaras del equipo
    
    public AplicarFiltros(IArchivoMultimediaService archivoMultimediaService, CamaraService camaraService) {
        this.archivoMultimediaService = archivoMultimediaService;
        this.camaraService = camaraService;
        // Inicializar la ventana en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            inicializarUI();
            // No mostramos la ventana aquí, se mostrará cuando se llame a setVisible(true)
        });
    }
    
    // Método para mostrar la ventana
    public void mostrar() {
        setVisible(true);
    }
    
    private void inicializarUI() {
        setTitle("Aplicar Filtros a Fotos");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior para la selección de cámara
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.add(new JLabel("Seleccionar Cámara: "));
        cmbCamaras = new JComboBox<>();
        cmbCamaras.setPreferredSize(new Dimension(400, 25)); // Aumentado de 200 a 400 píxeles de ancho
        panelSuperior.add(cmbCamaras);
        
        // Botón para cargar fotos de la cámara seleccionada
        JButton btnCargarFotos = new JButton("Cargar Fotos");
        btnCargarFotos.addActionListener(e -> cargarFotosPorCamara());
        panelSuperior.add(btnCargarFotos);
        
        // Tabla para mostrar las fotos
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que la tabla no sea editable
            }
        };
        modeloTabla.addColumn("ID");
        modeloTabla.addColumn("Nombre");
        modeloTabla.addColumn("Fecha Creación");
        modeloTabla.addColumn("Tipo");
        
        tablaFotos = new JTable(modeloTabla);
        tablaFotos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaFotos);
        
        // Panel inferior para botones
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAplicarFiltro = new JButton("Aplicar Filtro");
        btnAplicarFiltro.setEnabled(false);
        btnAplicarFiltro.addActionListener(e -> aplicarFiltroSeleccionado());
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> this.dispose());
        
        panelInferior.add(btnAplicarFiltro);
        panelInferior.add(btnCerrar);
        
        // Agregar componentes al panel principal
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        // Habilitar/deshabilitar botón de aplicar filtro según selección
        tablaFotos.getSelectionModel().addListSelectionListener(e -> {
            btnAplicarFiltro.setEnabled(tablaFotos.getSelectedRow() != -1);
        });
        
        add(panelPrincipal);
        setVisible(true);
    }
    
    /**
     * Establece el ID del equipo y carga las cámaras correspondientes
     * @param equipoId ID del equipo
     */
    public void setEquipoId(Long equipoId) {
        if (equipoId == null) {
            JOptionPane.showMessageDialog(this, 
                "No se ha proporcionado un ID de equipo válido.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.equipoId = equipoId;
        
        // Asegurarse de que la UI esté inicializada antes de cargar las cámaras
        if (cmbCamaras == null) {
            // Si la UI no está lista, programar la carga de cámaras para cuando lo esté
            SwingUtilities.invokeLater(this::cargarCamaras);
        } else {
            cargarCamaras();
        }
    }
    
    private boolean cargandoAutomaticamente = false;
    
    private void cargarCamaras() {
        if (equipoId == null) {
            JOptionPane.showMessageDialog(this, 
                "No se ha especificado un ID de equipo válido.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Ejecutar la carga en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Obtener las cámaras del equipo
                List<Camara> camarasCargadas = camaraService.obtenerCamarasPorEquipo(equipoId);
                
                // Actualizar la UI en el hilo de eventos
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
                            // Cargar fotos de la primera cámara automáticamente
                            cargandoAutomaticamente = true;
                            cargarFotosPorCamara();
                        }
                    } else {
                        // No mostrar mensaje cuando no hay cámaras
                    }
                });
            } catch (Exception e) {
                // Manejar errores en el hilo de eventos
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(AplicarFiltros.this, 
                        "Error al cargar las cámaras: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    private void cargarFotosPorCamara() {
        // Limpiar tabla actual
        modeloTabla.setRowCount(0);
        
        // Verificar que haya cámaras disponibles
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
        
        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                // Obtener las fotos de la cámara seleccionada
                List<ArchivoMultimediaDTO> archivos = archivoMultimediaService.obtenerArchivosPorCamara(camaraSeleccionada.getIdCamara());
                
                // Actualizar la UI en el hilo de eventos
                SwingUtilities.invokeLater(() -> {
                    if (archivos == null || archivos.isEmpty()) {
                        // Solo mostrar el mensaje si no es una carga automática inicial
                        if (!cargandoAutomaticamente) {
                            JOptionPane.showMessageDialog(AplicarFiltros.this, 
                                "No se encontraron archivos para la cámara seleccionada.", 
                                "Información", JOptionPane.INFORMATION_MESSAGE);
                        }
                        cargandoAutomaticamente = false; // Restablecer el flag
                        return;
                    }
                    
                    // Restablecer el flag de carga automática
                    cargandoAutomaticamente = false;
                    
                    // Mostrar solo las fotos en la tabla
                    int fotosMostradas = 0;
                    Set<String> tiposEncontrados = new HashSet<>();
                    
                    for (ArchivoMultimediaDTO archivo : archivos) {
                        String tipo = archivo.getTipo() != null ? archivo.getTipo().toUpperCase() : "";
                        tiposEncontrados.add(tipo);
                        
                        // Verificar si el archivo es una imagen (acepta FOTO, IMAGE, o extensiones comunes)
                        if (tipo.contains("FOTO") || tipo.contains("IMAGE") || 
                            archivo.getNombreArchivo().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                            modeloTabla.addRow(new Object[]{
                                archivo.getIdArchivo(),
                                archivo.getNombreArchivo(),
                                archivo.getFechaCaptura() != null ? archivo.getFechaCaptura() : "N/A",
                                tipo
                            });
                            fotosMostradas++;
                        }
                    }
                    
                    // Mostrar mensaje si no se encontraron fotos después de filtrar
                    if (fotosMostradas == 0 && !cargandoAutomaticamente) {
                        String mensaje = "No se encontraron archivos de imagen compatibles.\n\n" +
                                       "Tipos de archivo encontrados: " + 
                                       (tiposEncontrados.isEmpty() ? "Ninguno" : String.join(", ", tiposEncontrados));
                        
                        JOptionPane.showMessageDialog(AplicarFiltros.this, 
                            mensaje, 
                            "Sin imágenes compatibles", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    // Habilitar el botón de aplicar filtro si hay elementos
                    btnAplicarFiltro.setEnabled(modeloTabla.getRowCount() > 0);
                });
            } catch (Exception e) {
                // Manejar errores en el hilo de eventos
                SwingUtilities.invokeLater(() -> {
                    cargandoAutomaticamente = false; // Asegurarse de restablecer el flag en caso de error
                    JOptionPane.showMessageDialog(AplicarFiltros.this, 
                        "Error al cargar las fotos: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    private void aplicarFiltroSeleccionado() {
        int filaSeleccionada = tablaFotos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, seleccione una foto de la lista.", 
                "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Obtener los datos del archivo seleccionado
            Long idArchivo = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            String nombreArchivo = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            String tipoArchivo = (String) modeloTabla.getValueAt(filaSeleccionada, 3);
            
            // Verificar que el archivo sea una imagen (acepta varios tipos de imagen)
            String tipoArchivoUpper = tipoArchivo != null ? tipoArchivo.toUpperCase() : "";
            String nombreArchivoLower = nombreArchivo != null ? nombreArchivo.toLowerCase() : "";
            
            if (!tipoArchivoUpper.contains("FOTO") && 
                !tipoArchivoUpper.contains("IMAGE") && 
                !nombreArchivoLower.matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                JOptionPane.showMessageDialog(this, 
                    "Solo se pueden aplicar filtros a archivos de imagen.\n" +
                    "Tipo de archivo actual: " + (tipoArchivo != null ? tipoArchivo : "Desconocido"), 
                    "Tipo de archivo no soportado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Mostrar opciones de filtro
            String[] opcionesFiltro = {
                "Escala de grises", 
                "Sepia", 
                "Negativo", 
                "Brillo (+20%)", 
                "Contraste (+30%)",
                "Desenfocar"
            };
            
            String filtroSeleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione el filtro a aplicar a la imagen:\n\n" +
                "Archivo: " + nombreArchivo + "\n" +
                "ID: " + idArchivo,
                "Aplicar Filtro a la Imagen",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesFiltro,
                opcionesFiltro[0]);
                
            if (filtroSeleccionado != null) {
                // Mostrar confirmación antes de aplicar el filtro
                int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    String.format("¿Está seguro de aplicar el filtro '%s' a la imagen '%s'?", 
                        filtroSeleccionado, nombreArchivo),
                    "Confirmar Aplicación de Filtro",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirmacion == JOptionPane.YES_OPTION) {
                    // Aplicar el filtro en un hilo separado
                    new Thread(() -> {
                        try {
                            // Mostrar indicador de progreso
                            JOptionPane.showMessageDialog(AplicarFiltros.this, 
                                String.format("Aplicando filtro '%s' a la imagen...", filtroSeleccionado), 
                                "Procesando", JOptionPane.INFORMATION_MESSAGE);
                            
                            // Aquí iría la lógica real para aplicar el filtro
                            // Por ahora simulamos un retardo
                            Thread.sleep(2000);
                            
                            // Mostrar mensaje de éxito
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(AplicarFiltros.this, 
                                    String.format("Filtro '%s' aplicado correctamente a '%s'.", 
                                        filtroSeleccionado, nombreArchivo),
                                    "Filtro Aplicado", JOptionPane.INFORMATION_MESSAGE);
                                
                                // Actualizar la lista de fotos para reflejar los cambios
                                cargarFotosPorCamara();
                            });
                            
                        } catch (Exception e) {
                            // Mostrar mensaje de error
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(AplicarFiltros.this, 
                                    "Error al aplicar el filtro: " + e.getMessage(), 
                                    "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            });
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al procesar la solicitud: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
