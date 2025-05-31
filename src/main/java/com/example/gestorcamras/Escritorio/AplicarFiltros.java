package com.example.gestorcamras.Escritorio;

import com.example.gestorcamras.model.ArchivoMultimedia;
import com.example.gestorcamras.model.Camara;
import com.example.gestorcamras.service.ArchivoMultimediaService;
import com.example.gestorcamras.service.CamaraService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AplicarFiltros extends JFrame {
    
    private JComboBox<String> cmbCamaras;
    private JTable tablaFotos;
    private DefaultTableModel modeloTabla;
    private JButton btnAplicarFiltro;
    private JButton btnCerrar;
    
    private final transient ArchivoMultimediaService archivoMultimediaService;
    private final transient CamaraService camaraService;
    
    private Long equipoId; // ID del equipo actual
    private List<Camara> camaras; // Lista de cámaras del equipo
    
    public AplicarFiltros(ArchivoMultimediaService archivoMultimediaService, CamaraService camaraService) {
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
        cmbCamaras.setPreferredSize(new Dimension(200, 25));
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
    
    public void setEquipoId(Long equipoId) {
        this.equipoId = equipoId;
        if (equipoId != null) {
            cargarCamaras();
        }
    }
    
    private void cargarCamaras() {
        try {
            // Obtener las cámaras del equipo
            camaras = camaraService.obtenerCamarasPorEquipo(equipoId);
            cmbCamaras.removeAllItems();
            
            if (camaras != null && !camaras.isEmpty()) {
                for (Camara camara : camaras) {
                    cmbCamaras.addItem(camara.getNombre() + " (ID: " + camara.getIdCamara() + ")");
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se encontraron cámaras para este equipo", 
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar las cámaras: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarFotosPorCamara() {
        // Limpiar tabla actual
        modeloTabla.setRowCount(0);
        
        int selectedIndex = cmbCamaras.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= camaras.size()) {
            JOptionPane.showMessageDialog(this, "Seleccione una cámara válida", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Camara camaraSeleccionada = camaras.get(selectedIndex);
        
        try {
            // Obtener las fotos de la cámara seleccionada
            List<ArchivoMultimedia> archivos = archivoMultimediaService.obtenerArchivosPorCamara(camaraSeleccionada.getIdCamara());
            
            if (archivos == null || archivos.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No se encontraron archivos para la cámara seleccionada", 
                    "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Formateador de fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // Mostrar las fotos en la tabla
            for (ArchivoMultimedia archivo : archivos) {
                modeloTabla.addRow(new Object[]{
                    archivo.getIdArchivo(),
                    archivo.getNombreArchivo(),
                    archivo.getFechaCaptura() != null ? dateFormat.format(archivo.getFechaCaptura()) : "N/A",
                    archivo.getTipo()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar las fotos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void aplicarFiltroSeleccionado() {
        int filaSeleccionada = tablaFotos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una foto primero", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Obtener el ID del archivo seleccionado
            Long idArchivo = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            String nombreArchivo = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
            
            // Aquí iría la lógica para aplicar el filtro a la foto seleccionada
            // Por ahora mostramos un diálogo con las opciones de filtro
            String[] opcionesFiltro = {"Escala de grises", "Sepia", "Negativo", "Brillo", "Contraste"};
            String filtroSeleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione el filtro a aplicar:",
                "Aplicar Filtro",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesFiltro,
                opcionesFiltro[0]);
                
            if (filtroSeleccionado != null) {
                // Aquí iría la lógica para aplicar el filtro seleccionado
                JOptionPane.showMessageDialog(this, 
                    String.format("Aplicando filtro '%s' a: %s (ID: %d)", 
                        filtroSeleccionado, nombreArchivo, idArchivo),
                    "Aplicando Filtro", JOptionPane.INFORMATION_MESSAGE);
                
                // TODO: Llamar al servicio para aplicar el filtro
                // Ejemplo: procesadorImagenService.aplicarFiltro(idArchivo, filtroSeleccionado);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al aplicar el filtro: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
