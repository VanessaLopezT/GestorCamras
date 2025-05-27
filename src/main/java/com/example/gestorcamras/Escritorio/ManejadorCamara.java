package com.example.gestorcamras.Escritorio;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.Arrays;

/**
 * Clase para manejar la cámara web y realizar capturas de fotos y videos.
 */
public class ManejadorCamara {
    private VideoCapture captura;
    private VideoWriter grabadorVideo;
    private boolean grabando = false;
    private final File carpetaFotos;
    private final File carpetaVideos;
    private final JLabel etiquetaVistaPrevia;
    private final AtomicBoolean detenerGrabacion = new AtomicBoolean(false);
    private static final int ANCHO_ESTANDAR = 640;
    private static final int ALTO_ESTANDAR = 480;
    private static final int FPS_ESTANDAR = 15; // Reducido a 15 FPS para mejor rendimiento
    private static final long TIEMPO_ENTRE_FRAMES_MS = 1000L / FPS_ESTANDAR;
    private long ultimoFrameTiempo = 0;
    private long tiempoSobrante = 0; // Para manejar el tiempo sobrante entre frames
    private static final long MAX_TAMANO_VIDEO_MB = 100; // 100MB máximo por video
    private static final long MAX_TAMANO_FOTO_MB = 10;   // 10MB máximo por foto
    private Consumer<String> onArchivoGuardadoListener;   // Listener para notificar cuando se guarda un archivo

    static {
        try {
            // Cargar la biblioteca desde la ruta de instalación
            String opencvPath = "C:\\opencv\\build\\java\\x64\\opencv_java455.dll";
            System.load(opencvPath);
            
            // OpenCV cargado correctamente
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error al cargar OpenCV. Verifica la instalación en C:\\opencv\\n" +
                "Asegúrate de que los siguientes archivos existen:\n" +
                "- C:\\opencv\\build\\java\\x64\\opencv_java455.dll\n" +
                "- C:\\opencv\\build\\java\\x64\\opencv_videoio_ffmpeg455_64.dll\n\n" +
                "Error: " + ex.getMessage(),
                "Error de OpenCV",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Constructor de la clase ManejadorCamara.
     * @param etiquetaVistaPrevia JLabel donde se mostrará la vista previa de la cámara.
     */
    /**
     * Constructor de la clase ManejadorCamara.
     * @param etiquetaVistaPrevia JLabel donde se mostrará la vista previa de la cámara.
     * @param onArchivoGuardadoListener Callback que se ejecutará cuando se guarde un archivo (foto o video).
     *                                 Recibe como parámetro la ruta del archivo guardado.
     */
    public ManejadorCamara(JLabel etiquetaVistaPrevia, Consumer<String> onArchivoGuardadoListener) {
        this.onArchivoGuardadoListener = onArchivoGuardadoListener;
        this.etiquetaVistaPrevia = etiquetaVistaPrevia;
        
        // Crear carpetas si no existen
        this.carpetaFotos = new File("capturas/fotos");
        this.carpetaVideos = new File("capturas/videos");
        
        if (!carpetaFotos.exists()) {
            if (!carpetaFotos.mkdirs()) {
                JOptionPane.showMessageDialog(null, 
                    "No se pudo crear la carpeta para fotos: " + carpetaFotos.getAbsolutePath(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        if (!carpetaVideos.exists()) {
            if (!carpetaVideos.mkdirs()) {
                JOptionPane.showMessageDialog(null, 
                    "No se pudo crear la carpeta para videos: " + carpetaVideos.getAbsolutePath(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Inicializar la cámara
        if (!iniciarCamara()) {
            throw new RuntimeException("No se pudo inicializar la cámara");
        }
    }
    
    /**
     * Inicializa la cámara web.
     * @return true si la cámara se inicializó correctamente, false en caso contrario.
     */
    public boolean iniciarCamara() {
        if (captura != null && captura.isOpened()) {
            return true;
        }
        
        // Intentar abrir la cámara predeterminada (0)
        captura = new VideoCapture(0);
        
        if (!captura.isOpened()) {
            mostrarError("No se pudo abrir la cámara. Asegúrate de que esté conectada y no esté siendo utilizada por otra aplicación.");
            return false;
        }
        
        // Configurar el tamaño del frame y los FPS
        captura.set(Videoio.CAP_PROP_FRAME_WIDTH, ANCHO_ESTANDAR);
        captura.set(Videoio.CAP_PROP_FRAME_HEIGHT, ALTO_ESTANDAR);
        captura.set(Videoio.CAP_PROP_FPS, FPS_ESTANDAR);
        
        // Forzar el modo MJPG que generalmente permite mejor control de FPS
        captura.set(Videoio.CAP_PROP_FOURCC, VideoWriter.fourcc('M', 'J', 'P', 'G'));
        
        // Verificar la configuración
        double fpsConfigurado = captura.get(Videoio.CAP_PROP_FPS);
        // FPS configurados
        
        // Si no se pudo configurar a 30 FPS, intentar con un valor más bajo
        if (fpsConfigurado <= 0 || fpsConfigurado > 60) {
            captura.set(Videoio.CAP_PROP_FPS, 15);
            fpsConfigurado = captura.get(Videoio.CAP_PROP_FPS);
            // FPS reconfigurados
        }
        captura.set(Videoio.CAP_PROP_FPS, FPS_ESTANDAR);
        
        return true;
    }
    
    /**
     * Actualiza la vista previa de la cámara.
     */
    void actualizarVistaPrevia() {
        long tiempoActual = System.currentTimeMillis();
        long tiempoTranscurrido = tiempoActual - ultimoFrameTiempo;
        
        // Solo actualizar si ha pasado el tiempo suficiente para el siguiente frame
        if (tiempoTranscurrido + tiempoSobrante < TIEMPO_ENTRE_FRAMES_MS) {
            // No es tiempo de actualizar aún
            try {
                // Dormir solo el tiempo restante para el siguiente frame
                long tiempoRestante = TIEMPO_ENTRE_FRAMES_MS - (tiempoTranscurrido + tiempoSobrante);
                if (tiempoRestante > 0) {
                    Thread.sleep(tiempoRestante);
                    // Acumular cualquier tiempo sobrante
                    tiempoSobrante = 0;
                } else {
                    // Si no dormimos, guardar el tiempo sobrante para la próxima iteración
                    tiempoSobrante = Math.abs(tiempoRestante);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            return; // Saltar este frame
        }
        
        // Actualizar el tiempo del último frame
        ultimoFrameTiempo = tiempoActual;
        // Ajustar el tiempo sobrante
        if (tiempoTranscurrido > TIEMPO_ENTRE_FRAMES_MS) {
            tiempoSobrante = tiempoTranscurrido % TIEMPO_ENTRE_FRAMES_MS;
        }
        
        if (captura == null || !captura.isOpened()) {
            return;
        }
        
        Mat frame = new Mat();
        Mat frameParaGrabar = null;
        
        try {
            if (captura.read(frame)) {
                // Crear una copia del frame para grabar (mantener en BGR)
                if (grabando && grabadorVideo != null && grabadorVideo.isOpened()) {
                    frameParaGrabar = frame.clone();
                    grabadorVideo.write(frameParaGrabar);
                }
                
                // Convertir a RGB solo para la vista previa
                Mat frameParaVista = new Mat();
                Imgproc.cvtColor(frame, frameParaVista, Imgproc.COLOR_BGR2RGB);
                
                // Mostrar la vista previa
                BufferedImage imagen = matToBufferedImage(frameParaVista);
                if (imagen != null) {
                    ImageIcon icono = new ImageIcon(imagen.getScaledInstance(
                        etiquetaVistaPrevia.getWidth(), 
                        etiquetaVistaPrevia.getHeight(), 
                        java.awt.Image.SCALE_SMOOTH));
                    etiquetaVistaPrevia.setIcon(icono);
                }
                
                // Liberar recursos
                frameParaVista.release();
            }
        } finally {
            // Asegurarse de liberar recursos
            if (frameParaGrabar != null) {
                frameParaGrabar.release();
            }
            frame.release();
        }
    }
    
    /**
     * Toma una foto y la guarda en la carpeta de fotos.
     * @return Ruta del archivo de la foto tomada, o null si hubo un error.
     */
    public String tomarFoto() {
        if (captura == null || !captura.isOpened()) {
            mostrarError("No se puede tomar la foto: la cámara no está disponible.");
            return null;
        }
        
        Mat frame = new Mat();
        if (!captura.read(frame)) {
            mostrarError("No se pudo capturar el fotograma de la cámara.");
            return null;
        }
        
        // Verificar el tamaño del frame
        if (frame.empty() || frame.cols() <= 0 || frame.rows() <= 0) {
            mostrarError("El fotograma capturado no es válido.");
            return null;
        }
        
        // Crear nombre de archivo con fecha y hora
        String nombreArchivo = "foto_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        File archivoFoto = new File(carpetaFotos, nombreArchivo);
        
        // Crear una copia del frame original
        Mat frameParaGuardar = frame.clone();
        
        // Convertir de BGR a RGB para la vista previa (si es necesario)
        // OpenCV usa BGR por defecto, pero para guardar la imagen está bien dejarlo en BGR
        // ya que la mayoría de los visores de imágenes manejan correctamente el espacio BGR
        
        // Guardar la imagen en formato JPG con la mejor calidad
        MatOfInt params = new MatOfInt(
            Imgcodecs.IMWRITE_JPEG_QUALITY, 95,
            Imgcodecs.IMWRITE_JPEG_PROGRESSIVE, 1
        );
        
        if (!Imgcodecs.imwrite(archivoFoto.getAbsolutePath(), frameParaGuardar, params)) {
            frameParaGuardar.release();
            mostrarError("No se pudo guardar la imagen en: " + archivoFoto.getAbsolutePath());
            return null;
        }
        
        // Liberar recursos
        frameParaGuardar.release();
        
        // Verificar el tamaño del archivo
        try {
            long tamanoBytes = Files.size(archivoFoto.toPath());
            long tamanoMB = tamanoBytes / (1024 * 1024);
            
            if (tamanoMB > MAX_TAMANO_FOTO_MB) {
                mostrarError("La foto es demasiado grande (" + tamanoMB + "MB). Tamaño máximo permitido: " + MAX_TAMANO_FOTO_MB + "MB");
                if (!archivoFoto.delete()) {
                    System.err.println("No se pudo eliminar la foto con tamaño excesivo: " + archivoFoto.getAbsolutePath());
                }
                return null;
            }
            
            // Notificar que se ha guardado un archivo
            if (onArchivoGuardadoListener != null) {
                onArchivoGuardadoListener.accept(archivoFoto.getAbsolutePath());
            }
        } catch (IOException e) {
            mostrarError("Error al verificar el tamaño de la foto: " + e.getMessage());
            return null;
        }
        
        return archivoFoto.getAbsolutePath();
    }
    
    /**
     * Inicia la grabación de video.
     * @return true si se inició la grabación correctamente, false en caso contrario.
     */
    public boolean iniciarGrabacion() {
        if (grabando) {
            mostrarError("Ya hay una grabación en curso.");
            return false;
        }
        
        // Crear nombre de archivo con fecha y hora
        String nombreArchivo = "video_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
        File archivoVideo = new File(carpetaVideos, nombreArchivo);
        
        // Configuración del códec de video
        // Probar primero con H.264 (requiere codecs adicionales en algunos sistemas)
        int[] codecs = {
            VideoWriter.fourcc('X', '2', '6', '4'),  // H.264
            VideoWriter.fourcc('a', 'v', 'c', '1'),   // H.264 alternativo
            VideoWriter.fourcc('M', 'P', '4', 'V'),   // MPEG-4
            VideoWriter.fourcc('M', 'J', 'P', 'G'),   // Motion-JPEG
            VideoWriter.fourcc('X', 'V', 'I', 'D')    // Xvid
        };
        
        boolean grabadorInicializado = false;
        
        // Probar diferentes códecs hasta encontrar uno que funcione
        for (int i = 0; i < codecs.length && !grabadorInicializado; i++) {
            int fourcc = codecs[i];
            
            // Liberar el grabador anterior si existe
            if (grabadorVideo != null) {
                grabadorVideo.release();
            }
            
            // Crear nuevo grabador
            grabadorVideo = new VideoWriter(
                archivoVideo.getAbsolutePath(),
                fourcc,
                FPS_ESTANDAR,
                new Size(ANCHO_ESTANDAR, ALTO_ESTANDAR),
                true  // Color
            );
            
            grabadorInicializado = grabadorVideo.isOpened();
            
            if (grabadorInicializado) {
                // Grabador de video inicializado
            }
        }
        
        if (!grabadorInicializado) {
            mostrarError("No se pudo inicializar el grabador de video con ningún códec disponible.\n" +
                       "Asegúrate de tener instalados los códecs de video necesarios.");
            return false;
        }
        
        grabando = true;
        detenerGrabacion.set(false);
        
        // Iniciar un hilo para monitorear el tamaño del archivo
        new Thread(() -> {
            try {
                while (grabando && !detenerGrabacion.get()) {
                    long tamanoBytes = Files.size(archivoVideo.toPath());
                    long tamanoMB = tamanoBytes / (1024 * 1024);
                    
                    if (tamanoMB > MAX_TAMANO_VIDEO_MB) {
                        SwingUtilities.invokeLater(() -> {
                            mostrarError("Se ha alcanzado el tamaño máximo de video (" + MAX_TAMANO_VIDEO_MB + "MB). La grabación se detendrá automáticamente.");
                            detenerGrabacion();
                        });
                        break;
                    }
                    
                    try {
                        Thread.sleep(1000); // Verificar cada segundo
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    mostrarError("Error al verificar el tamaño del video: " + e.getMessage());
                    detenerGrabacion();
                });
            }
        }).start();
        
        return true;
    }
    
    /**
     * Detiene la grabación de video.
     * @return Ruta del archivo de video grabado, o null si hubo un error.
     */
    public String detenerGrabacion() {
        if (!grabando) {
            return null;
        }
        
        grabando = false;
        detenerGrabacion.set(true);
        
        if (grabadorVideo != null) {
            grabadorVideo.release();
            grabadorVideo = null;
        }
        
        // Obtener el último archivo de video en la carpeta
        File[] archivos = carpetaVideos.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".mp4") || 
            name.toLowerCase().endsWith(".avi") ||
            name.toLowerCase().endsWith(".mov")
        );
        
        if (archivos != null && archivos.length > 0) {
            // Ordenar por fecha de modificación (más reciente primero)
            Arrays.sort(archivos, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            String rutaVideo = archivos[0].getAbsolutePath();
            
            // Notificar que se ha guardado un archivo de video
            if (onArchivoGuardadoListener != null) {
                onArchivoGuardadoListener.accept(rutaVideo);
            }
            
            return rutaVideo;
        }
        
        return null;
    }
    
    /**
     * Libera los recursos de la cámara.
     */
    public void liberarRecursos() {
        if (grabando) {
            detenerGrabacion();
        }
        
        if (captura != null) {
            captura.release();
        }
        
        if (grabadorVideo != null) {
            grabadorVideo.release();
        }
    }
    
    /**
     * Convierte un objeto Mat de OpenCV a BufferedImage de Java.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }
        
        try {
            int tipo = BufferedImage.TYPE_3BYTE_BGR;
            if (mat.channels() == 1) {
                tipo = BufferedImage.TYPE_BYTE_GRAY;
            } else if (mat.channels() == 3) {
                tipo = BufferedImage.TYPE_3BYTE_BGR;
                // Convertir de BGR a RGB
                Mat rgb = new Mat();
                Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);
                mat = rgb;
            }
            
            byte[] b = new byte[mat.channels() * mat.cols() * mat.rows()];
            mat.get(0, 0, b);
            
            BufferedImage imagen = new BufferedImage(mat.cols(), mat.rows(), tipo);
            byte[] targetPixels = ((DataBufferByte) imagen.getRaster().getDataBuffer()).getData();
            System.arraycopy(b, 0, targetPixels, 0, b.length);
            
            return imagen;
        } catch (Exception e) {
            System.err.println("Error al convertir Mat a BufferedImage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Muestra un mensaje de error en un cuadro de diálogo.
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            null, 
            mensaje, 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
}
