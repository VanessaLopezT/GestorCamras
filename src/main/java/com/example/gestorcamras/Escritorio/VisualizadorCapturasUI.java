package com.example.gestorcamras.Escritorio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class VisualizadorCapturasUI extends JFrame {
    private static final String CARPETA_CAPTURAS = "capturas";
    private JLabel lblMedia;
    private JLabel lblInfo;
    private JButton btnAbrirVideo;
    private File[] archivosImagenes;
    private int indiceActual = 0;

    public VisualizadorCapturasUI(String nombreCamara) {
        setTitle("Capturas de la cámara: " + nombreCamara);
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de controles
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton btnAnterior = new JButton("Anterior");
        JButton btnSiguiente = new JButton("Siguiente");
        btnAbrirVideo = new JButton("Abrir video");
        JButton btnAbrirCarpeta = new JButton("Abrir carpeta");
        
        // Configurar botones
        btnAnterior.addActionListener(e -> mostrarImagenAnterior());
        btnSiguiente.addActionListener(e -> mostrarSiguienteImagen());
        btnAbrirVideo.addActionListener(e -> abrirVideoEnReproductor());
        btnAbrirCarpeta.addActionListener(e -> abrirCarpetaContenedora());
        
        // Inicialmente deshabilitar el botón de abrir video
        btnAbrirVideo.setEnabled(false);
        
        panelControles.add(btnAnterior);
        panelControles.add(btnSiguiente);
        panelControles.add(btnAbrirVideo);
        panelControles.add(btnAbrirCarpeta);
        
        // Panel de medios
        JPanel panelMedia = new JPanel(new BorderLayout());
        panelMedia.setBackground(Color.BLACK);
        panelMedia.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        lblMedia = new JLabel("No hay archivos multimedia disponibles", SwingConstants.CENTER);
        lblMedia.setForeground(Color.WHITE);
        lblMedia.setHorizontalAlignment(SwingConstants.CENTER);
        lblMedia.setVerticalAlignment(SwingConstants.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(lblMedia);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        panelMedia.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblInfo = new JLabel("0/0");
        panelInfo.add(lblInfo);
        
        // Agregar componentes al panel principal
        panelPrincipal.add(panelControles, BorderLayout.NORTH);
        panelPrincipal.add(panelMedia, BorderLayout.CENTER);
        panelPrincipal.add(panelInfo, BorderLayout.SOUTH);
        
        add(panelPrincipal);
        
        // Cargar imágenes
        cargarImagenes();
    }
    
    private void cargarImagenes() {
        File carpeta = new File(CARPETA_CAPTURAS);
        if (!carpeta.exists() || !carpeta.isDirectory()) {
            JOptionPane.showMessageDialog(this, 
                "La carpeta de capturas no existe o no es accesible.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Obtener todas las subcarpetas (fotos y videos)
        List<File> archivos = new ArrayList<>();
        File[] subcarpetas = carpeta.listFiles(File::isDirectory);
        
        if (subcarpetas != null) {
            for (File subcarpeta : subcarpetas) {
                // Buscar archivos en cada subcarpeta
                File[] archivosEnSubcarpeta = subcarpeta.listFiles((dir, name) -> 
                    name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|mp4|avi|mov|wmv)$")
                );
                
                if (archivosEnSubcarpeta != null) {
                    archivos.addAll(Arrays.asList(archivosEnSubcarpeta));
                }
            }
        }
        
        // También buscar archivos directamente en la carpeta raíz
        File[] archivosEnRaiz = carpeta.listFiles((dir, name) -> 
            name.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|mp4|avi|mov|wmv)$")
        );
        
        if (archivosEnRaiz != null) {
            archivos.addAll(Arrays.asList(archivosEnRaiz));
        }
        
        // Convertir la lista a array y ordenar por fecha de modificación (más reciente primero)
        archivosImagenes = archivos.toArray(new File[0]);
        Arrays.sort(archivosImagenes, Comparator.comparingLong(File::lastModified).reversed());
        
        if (archivosImagenes.length == 0) {
            lblMedia.setText("No se encontraron archivos multimedia en la carpeta de capturas.");
            lblInfo.setText("0/0");
            return;
        }
        
        indiceActual = 0;
        mostrarImagenActual();
    }
    
    private void mostrarImagenActual() {
        if (archivosImagenes == null || archivosImagenes.length == 0) {
            return;
        }
        
        try {
            File archivoActual = archivosImagenes[indiceActual];
            String nombreArchivo = archivoActual.getName().toLowerCase();
            
            // Limpiar el panel de medios
            lblMedia.setIcon(null);
            lblMedia.setText("");
            
            // Verificar si es un video
            if (nombreArchivo.matches(".*\\.(mp4|avi|mov|wmv)$")) {
                // Mostrar mensaje para videos con opción de abrir en reproductor
                lblMedia.setText("<html><div style='color:black;'>Vídeo: " + archivoActual.getName() + "<br><br>" +
                              "Haz clic en 'Abrir video' para reproducir con el reproductor del sistema</div></html>");
                btnAbrirVideo.setEnabled(true);
            } else {
                btnAbrirVideo.setEnabled(false);
                // Es una imagen
                try {
                    // Cargar la imagen
                    ImageIcon icono = new ImageIcon(archivoActual.getAbsolutePath());
                    Image imagen = icono.getImage();
                    
                    // Escalar la imagen para que quepa en el panel manteniendo la relación de aspecto
                    int anchoPanel = lblMedia.getWidth() > 0 ? lblMedia.getWidth() : 700;
                    int altoPanel = lblMedia.getHeight() > 0 ? lblMedia.getHeight() : 500;
                    
                    double relacionAncho = (double) anchoPanel / imagen.getWidth(null);
                    double relacionAlto = (double) altoPanel / imagen.getHeight(null);
                    double escala = Math.min(relacionAncho, relacionAlto);
                    
                    // Asegurarse de que la escala no sea mayor a 1 (no hacer zoom)
                    escala = Math.min(escala, 1.0);
                    
                    int nuevoAncho = (int) (imagen.getWidth(null) * escala);
                    int nuevoAlto = (int) (imagen.getHeight(null) * escala);
                    
                    Image imagenEscalada = imagen.getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
                    lblMedia.setIcon(new ImageIcon(imagenEscalada));
                } catch (Exception e) {
                    lblMedia.setText("Error al cargar la imagen: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Actualizar información
            lblInfo.setText(String.format("%d/%d - %s", 
                indiceActual + 1, 
                archivosImagenes.length,
                archivoActual.getName()));
            
        } catch (Exception e) {
            lblMedia.setIcon(null);
            lblMedia.setText("Error al cargar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void mostrarSiguienteImagen() {
        if (archivosImagenes == null || archivosImagenes.length == 0) {
            return;
        }
        
        indiceActual++;
        if (indiceActual >= archivosImagenes.length) {
            indiceActual = 0;
        }
        mostrarImagenActual();
    }
    
    private void mostrarImagenAnterior() {
        if (archivosImagenes == null || archivosImagenes.length == 0) {
            return;
        }
        
        indiceActual--;
        if (indiceActual < 0) {
            indiceActual = archivosImagenes.length - 1;
        }
        mostrarImagenActual();
    }
    
    // Método eliminado: eliminarImagenActual()
    
    private void abrirVideoEnReproductor() {
        if (archivosImagenes == null || archivosImagenes.length == 0) {
            return;
        }
        
        File archivoActual = archivosImagenes[indiceActual];
        String nombreArchivo = archivoActual.getName().toLowerCase();
        
        // Verificar si es un video
        if (!nombreArchivo.matches(".*\\.(mp4|avi|mov|wmv)$")) {
            return;
        }
        
        try {
            // Verificar si el escritorio es soportado
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this,
                    "La apertura de archivos no es compatible con este sistema operativo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Desktop desktop = Desktop.getDesktop();
            
            // Verificar si la operación es soportada
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(archivoActual);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se puede abrir el video. La operación no está soportada en este sistema.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error al abrir el video: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error inesperado al abrir el video: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirCarpetaContenedora() {
        // Verificar si el escritorio es soportado
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this,
                "La apertura de carpetas no es compatible con este sistema operativo.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Desktop desktop = Desktop.getDesktop();
        File carpeta = new File(CARPETA_CAPTURAS);
        
        try {
            // Verificar si la carpeta existe
            if (!carpeta.exists()) {
                // Intentar crear la carpeta si no existe
                boolean creada = carpeta.mkdirs();
                if (!creada) {
                    throw new IOException("No se pudo crear la carpeta de capturas.");
                }
            }
            
            // Verificar si es un directorio
            if (!carpeta.isDirectory()) {
                throw new IOException("La ruta especificada no es un directorio: " + CARPETA_CAPTURAS);
            }
            
            // Verificar si la operación es soportada
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                throw new UnsupportedOperationException("La acción de abrir carpeta no está soportada");
            }
            
            // Abrir la carpeta
            desktop.open(carpeta);
            
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this,
                "No tiene permisos para acceder a la carpeta: " + e.getMessage(),
                "Error de permisos", JOptionPane.ERROR_MESSAGE);
                
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(this,
                "Operación no soportada: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error al acceder a la carpeta: " + e.getMessage() + 
                "\nRuta: " + carpeta.getAbsolutePath(),
                "Error de E/S", JOptionPane.ERROR_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error inesperado: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Asegurarse de que la imagen se muestre correctamente
            SwingUtilities.invokeLater(this::mostrarImagenActual);
        }
    }
}