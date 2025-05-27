package com.example.gestorcamras.Escritorio;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class MapaCamarasPanel extends JPanel {
    private JXMapViewer mapViewer;
    private JSONArray camaras;
    private Set<CameraWaypoint> waypoints;
    
    public MapaCamarasPanel(JSONArray camaras) {
        this.camaras = camaras != null ? camaras : new JSONArray();
        this.waypoints = new HashSet<>();
        setLayout(new BorderLayout());
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create the map viewer
        mapViewer = new JXMapViewer();
        
        // Configure the OpenStreetMap tile factory
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(2);
        mapViewer.setTileFactory(tileFactory);
        
        // Don't set any initial view - it will be set when cameras are loaded
        
        // Add camera markers
        actualizarCamaras(this.camaras);
        
        // Add the map viewer to the panel
        add(new JScrollPane(mapViewer), BorderLayout.CENTER);
    }
    
    public void actualizarCamaras(JSONArray nuevasCamaras) {
        this.camaras = nuevasCamaras != null ? nuevasCamaras : new JSONArray();
        this.waypoints.clear();
        
        // Hide the map by default - only show if we have cameras
        mapViewer.setVisible(false);
        
        // Create waypoints for cameras
        for (int i = 0; i < camaras.length(); i++) {
            try {
                JSONObject camara = camaras.getJSONObject(i);
                if (camara.has("latitud") && camara.has("longitud") && 
                    !camara.isNull("latitud") && !camara.isNull("longitud")) {
                    
                    double lat = camara.getDouble("latitud");
                    double lng = camara.getDouble("longitud");
                    String nombre = camara.optString("nombre", "Cámara " + (i + 1));
                    String ip = camara.optString("ip", "");
                    String direccion = camara.optString("direccion", "");
                    String tipo = camara.optString("tipo", "");
                    boolean activa = camara.optBoolean("activa", false);
                    
                    // Obtener el ID de la cámara
                    int id = -1;
                    try {
                        if (camara.has("idCamara") && !camara.isNull("idCamara")) {
                            id = camara.getInt("idCamara");
                        }
                    } catch (Exception e) {
                        // Usar el índice como respaldo
                        id = i + 1;
                    }
                    
                    // If no ID found, use index as fallback
                    if (id == -1) {
                        id = i + 1;

                    }
                    
                    // Create a custom waypoint with camera information
                    CameraWaypoint waypoint = new CameraWaypoint(id, lat, lng, nombre, ip, direccion, tipo, activa);
                    waypoints.add(waypoint);
                    

                } else {

                }
            } catch (Exception e) {
                System.err.println("Error al procesar cámara: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Only proceed if we have cameras to show
        if (waypoints.isEmpty()) {

            return;
        }
        
        // Show the map since we have cameras
        mapViewer.setVisible(true);
        
        // Create a custom painter for waypoints
        CameraWaypointRenderer waypointRenderer = new CameraWaypointRenderer(waypoints);
        
        // Create a compound painter (for future layers)
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(waypointRenderer);
        
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
        
        // Adjust the view to show all cameras
        try {
            Set<GeoPosition> positions = new HashSet<>();
            for (CameraWaypoint waypoint : waypoints) {
                positions.add(waypoint.getPosition());
            }
            
            if (!positions.isEmpty()) {
                // Calculate center point of all camera positions
                double centerLat = 0;
                double centerLon = 0;
                int count = 0;
                
                for (GeoPosition pos : positions) {
                    centerLat += pos.getLatitude();
                    centerLon += pos.getLongitude();
                    count++;
                }
                
                if (count > 0) {
                    // Set the center point
                    GeoPosition center = new GeoPosition(centerLat / count, centerLon / count);
                    mapViewer.setAddressLocation(center);
                    
                    // Always use zoom level 7
                    mapViewer.setZoom(6);
                }
            }
            

        } catch (Exception e) {

        }
        
        // Force map update
        mapViewer.repaint();
    }
    
    // No longer using default view - map will be hidden when no cameras are available
    
    // Inner class to represent a camera waypoint
    private static class CameraWaypoint extends DefaultWaypoint {
        private final int id;
        private final String nombre;
        private final String ip;
        private final String direccion;
        private final String tipo;
        private final boolean activa;
        private final Color color;
        private final GeoPosition position;
        
        public CameraWaypoint(int id, double lat, double lon, String nombre, String ip, 
                            String direccion, String tipo, boolean activa) {
            super(lat, lon);
            this.id = id;
            this.position = new GeoPosition(lat, lon);
            this.nombre = nombre != null ? nombre : "";
            this.ip = ip != null ? ip : "";
            this.direccion = direccion != null ? direccion : "";
            this.tipo = tipo != null ? tipo : "";
            this.activa = activa;
            this.color = activa ? new Color(0, 128, 0) : new Color(200, 0, 0); // Green for active, red for inactive
        }
        
        @Override
        public GeoPosition getPosition() {
            return position;
        }
        
        public String getTooltipText() {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<b>ID: ").append(id).append(" - ").append(htmlEscape(nombre)).append("</b>");
            if (!ip.isEmpty()) {
                sb.append("<br>IP: ").append(htmlEscape(ip));
            }
            if (!tipo.isEmpty()) {
                sb.append("<br>Tipo: ").append(htmlEscape(tipo));
            }
            if (!direccion.isEmpty()) {
                sb.append("<br>Dirección: ").append(htmlEscape(direccion));
            }
            sb.append("<br>Estado: ").append(activa ? "Activa" : "Inactiva").append("</html>");
            return sb.toString();
        }
        
        public Color getColor() {
            return color;
        }
        
        public boolean isActiva() {
            return activa;
        }
        
        private String htmlEscape(String input) {
            if (input == null) return "";
            return input.replace("&", "&amp;")
                      .replace("<", "&lt;")
                      .replace(">", "&gt;")
                      .replace("\"", "&quot;")
                      .replace("'", "&#39;");
        }
    }
    
    // Inner class to render camera waypoints
    private class CameraWaypointRenderer implements Painter<JXMapViewer> {
        private final Set<CameraWaypoint> waypoints;
        private CameraWaypoint lastHoveredWaypoint = null;
        private final ImageIcon cameraIcon;
        private final ImageIcon cameraIconInactive;
        private final int ICON_SIZE = 32;  // Increased size for better visibility
        
        public CameraWaypointRenderer(Set<CameraWaypoint> waypoints) {
            this.waypoints = waypoints;
            
            // Create custom camera icons
            cameraIcon = createCameraIcon(new Color(0, 128, 0)); // Green for active cameras
            cameraIconInactive = createCameraIcon(new Color(200, 0, 0)); // Red for inactive cameras
            
            // Configure tooltip and hover behavior
            mapViewer.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point2D point = new Point2D.Double(e.getX(), e.getY());
                    CameraWaypoint hovered = findWaypointAt(point);
                    
                    if (hovered != lastHoveredWaypoint) {
                        lastHoveredWaypoint = hovered;
                        if (hovered != null) {
                            mapViewer.setToolTipText(hovered.getTooltipText());
                        } else {
                            mapViewer.setToolTipText(null);
                        }
                        // Force repaint to update the hover state
                        mapViewer.repaint();
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (lastHoveredWaypoint != null) {
                        lastHoveredWaypoint = null;
                        mapViewer.setToolTipText(null);
                        mapViewer.repaint();
                    }
                }
            });
        }
        
        private ImageIcon createCameraIcon(Color color) {
            BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Make background transparent
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
            g2d.setComposite(AlphaComposite.Src);
            
            // Draw camera body (slightly larger)
            g2d.setColor(color);
            g2d.fillOval(2, 2, ICON_SIZE - 4, ICON_SIZE - 4);
            
            // Add white border
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(Color.WHITE);
            g2d.drawOval(2, 2, ICON_SIZE - 4, ICON_SIZE - 4);
            
            // Draw lens
            int lensSize = ICON_SIZE / 2;
            int lensX = (ICON_SIZE - lensSize) / 2;
            int lensY = (ICON_SIZE - lensSize) / 2;
            
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(lensX, lensY, lensSize, lensSize);
            
            // Add reflection effect
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillArc(lensX, lensY, lensSize, lensSize/2, 0, 180);
            
            // Draw lens center
            int centerSize = lensSize / 3;
            int centerX = (ICON_SIZE - centerSize) / 2;
            int centerY = (ICON_SIZE - centerSize) / 2;
            
            g2d.setColor(color.darker());
            g2d.fillOval(centerX, centerY, centerSize, centerSize);
            
            g2d.dispose();
            return new ImageIcon(image);
        }
        
        private CameraWaypoint findWaypointAt(Point2D point) {
            double minDist = Double.MAX_VALUE;
            CameraWaypoint closest = null;
            
            // Get the current viewport bounds
            Rectangle viewportBounds = mapViewer.getViewportBounds();
            
            for (CameraWaypoint waypoint : waypoints) {
                // Convert geographic coordinates to pixels
                Point2D waypointPoint = mapViewer.getTileFactory().geoToPixel(waypoint.getPosition(), mapViewer.getZoom());
                
                // Convert to view coordinates
                double x = waypointPoint.getX() - viewportBounds.getX();
                double y = waypointPoint.getY() - viewportBounds.getY();
                
                // Calculate distance from cursor to waypoint center
                double dist = Math.hypot(point.getX() - x, point.getY() - (y - ICON_SIZE/2));
                
                // Check if cursor is within the icon bounds (with some padding)
                if (dist < ICON_SIZE && dist < minDist) {
                    minDist = dist;
                    closest = waypoint;
                }
            }
            
            return closest;
        }
        
        @Override
        public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
            // Set rendering quality
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            
            // Convert the viewport to world coordinates
            Rectangle viewportBounds = map.getViewportBounds();
            
            for (CameraWaypoint waypoint : waypoints) {
                // Convert geographic coordinates to pixels
                Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());
                
                // Convert to view coordinates
                int x = (int)(point.getX() - viewportBounds.getX());
                int y = (int)(point.getY() - viewportBounds.getY());
                
                // Only draw if the waypoint is in the visible area
                if (x >= -ICON_SIZE && y >= -ICON_SIZE && 
                    x <= viewportBounds.getWidth() && y <= viewportBounds.getHeight()) {
                    
                    // Select icon based on camera status
                    ImageIcon icon = waypoint.isActiva() ? cameraIcon : cameraIconInactive;
                    
                    // Draw camera icon
                    g.drawImage(icon.getImage(), 
                               x - ICON_SIZE/2, 
                               y - ICON_SIZE,  // Adjust Y to have the point at the bottom of the icon
                               ICON_SIZE, 
                               ICON_SIZE, 
                               null);
                    
                    // Always show camera ID below the icon with better visibility
                    Font originalFont = g.getFont();
                    Font boldFont = originalFont.deriveFont(Font.BOLD, 12);
                    g.setFont(boldFont);
                    FontMetrics metrics = g.getFontMetrics();
                    
                    // Mostrar el ID solo cuando el mouse está sobre el ícono
                    if (waypoint == lastHoveredWaypoint) {
                        String idText = "ID: " + waypoint.id;
                        FontMetrics fm = g.getFontMetrics();
                        int idTextWidth = fm.stringWidth(idText);
                        
                        // Posición del texto (debajo del ícono)
                        int idX = x - idTextWidth / 2;
                        int idY = y + ICON_SIZE + 15;  // Espacio debajo del ícono
                        
                        // Fondo semitransparente para mejor legibilidad
                        int idPadding = 4;
                        int idArc = 4;
                        
                        // Dibujar fondo
                        g.setColor(new Color(255, 255, 255, 220));
                        g.fillRoundRect(
                            idX - idPadding,
                            idY - fm.getAscent() - idPadding/2,
                            idTextWidth + 2*idPadding,
                            fm.getHeight() + idPadding,
                            idArc, idArc
                        );
                        
                        // Borde
                        g.setColor(new Color(0, 0, 0, 150));
                        g.setStroke(new BasicStroke(1.0f));
                        g.drawRoundRect(
                            idX - idPadding,
                            idY - fm.getAscent() - idPadding/2,
                            idTextWidth + 2*idPadding,
                            fm.getHeight() + idPadding,
                            idArc, idArc
                        );
                        
                        // Dibujar el texto del ID
                        g.setColor(Color.BLACK);
                        g.drawString(idText, idX, idY);
                    }
                }
            }
        }
    }
}
