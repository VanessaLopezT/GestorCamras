package com.example.gestorcamras.Escritorio;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;

public class ServidorUI extends JFrame {

    private static final String SERVER_URL = "http://localhost:8080/api";
    private static boolean isServerInstance = false;

    private JList<String> listaEquipos;
    private DefaultListModel<String> modeloListaEquipos;

    private JTable tablaCamaras;
    private DefaultTableModel modeloTablaCamaras;

    private JTextArea areaLogs;

    // Cache de equipos con sus datos para usar al seleccionar
    private List<EquipoDTO> equiposCache = new ArrayList<>();

    public ServidorUI() {
        if (!isServerInstance) {
            isServerInstance = true;
        } else {
            JOptionPane.showMessageDialog(null, "Solo puede haber una instancia del servidor", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        setTitle("Servidor Gestor de Cámaras");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTitle("Servidor Gestor de Cámaras");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        modeloListaEquipos = new DefaultListModel<>();
        listaEquipos = new JList<>(modeloListaEquipos);
        listaEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollEquipos = new JScrollPane(listaEquipos);
        scrollEquipos.setPreferredSize(new Dimension(200, 0));
        add(scrollEquipos, BorderLayout.WEST);

        modeloTablaCamaras = new DefaultTableModel(new String[]{"ID", "Nombre", "IP", "Activa"}, 0);
        tablaCamaras = new JTable(modeloTablaCamaras);
        JScrollPane scrollCamaras = new JScrollPane(tablaCamaras);
        add(scrollCamaras, BorderLayout.CENTER);

        areaLogs = new JTextArea(5, 20);
        areaLogs.setEditable(false);
        JScrollPane scrollLogs = new JScrollPane(areaLogs);
        add(scrollLogs, BorderLayout.SOUTH);

        JButton btnRefrescar = new JButton("Refrescar Equipos");
        btnRefrescar.addActionListener(e -> cargarEquipos());
        add(btnRefrescar, BorderLayout.NORTH);

        // Listener para selección de equipo y cargar sus cámaras
        listaEquipos.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int index = listaEquipos.getSelectedIndex();
                if (index >= 0 && index < equiposCache.size()) {
                    EquipoDTO equipo = equiposCache.get(index);
                    cargarCamaras(equipo.getIdEquipo());
                }
            }
        });


        SwingUtilities.invokeLater(() -> {
            cargarEquipos();
        });

    }
    private void cargarEquipos() {
        try {
            log("Intentando conectar al servidor...");
            equiposCache.clear();
            modeloListaEquipos.clear();
            log("Cargando equipos...");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/equipos"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                log("Respuesta recibida: " + responseBody);

                try {
                    JSONArray jsonEquipos = new JSONArray(responseBody);
                    for (int i = 0; i < jsonEquipos.length(); i++) {
                        JSONObject obj = jsonEquipos.getJSONObject(i);
                        Long idEquipo = obj.getLong("idEquipo");
                        String nombre = obj.getString("nombre");
                        EquipoDTO equipo = new EquipoDTO(idEquipo);
                        equiposCache.add(equipo);
                        modeloListaEquipos.addElement(nombre);
                    }
                    log("Equipos cargados: " + jsonEquipos.length());
                } catch (org.json.JSONException jsonEx) {
                    log("La respuesta no es un JSON válido. Probablemente no autenticado.");
                    log("Respuesta completa: " + responseBody);
                    // Aquí podrías mostrar un diálogo para pedir login o avisar al usuario
                }
            } else {
                log("Error cargando equipos: HTTP " + response.statusCode());
            }
        } catch (Exception ex) {
            log("Error cargando equipos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void cargarCamaras(Long equipoId) {
        try {
            log("Cargando cámaras para equipo " + equipoId);
            modeloTablaCamaras.setRowCount(0);
            log("Cargando cámaras para equipo ID " + equipoId);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/equipos/" + equipoId + "/camaras"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JSONArray jsonCamaras = new JSONArray(responseBody);

                for (int i = 0; i < jsonCamaras.length(); i++) {
                    JSONObject obj = jsonCamaras.getJSONObject(i);
                    Long id = obj.getLong("idCamara");
                    String nombre = obj.getString("nombre");
                    String ip = obj.optString("ip", "");
                    boolean activa = obj.optBoolean("activa", false);
                    modeloTablaCamaras.addRow(new Object[]{id, nombre, ip, activa});
                }
                log("Cámaras cargadas: " + jsonCamaras.length());
            } else {
                log("Error cargando cámaras: HTTP " + response.statusCode());
            }
        } catch (Exception ex) {
            log("Error cargando cámaras: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private void log(String mensaje) {
        areaLogs.append(mensaje + "\n");
        areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
    }



    // DTO simple para guardar id y nombre de equipo
    private static class EquipoDTO {
        private Long idEquipo;

        public EquipoDTO(Long idEquipo) {
            this.idEquipo = idEquipo;
        }

        public Long getIdEquipo() {
            return idEquipo;
        }
    }
}