package com.example.gestorcamras.Escritorio;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServidorUI extends JFrame {

    private final String BASE_URL = "http://0.0.0.0:8080/api"; // Esto no funcionará en cliente, es para binding, no URL de llamada

    private JList<String> listaEquipos;
    private DefaultListModel<String> modeloListaEquipos;

    private JTable tablaCamaras;
    private DefaultTableModel modeloTablaCamaras;

    private JTextArea areaLogs;

    // Cache de equipos con sus datos para usar al seleccionar
    private List<EquipoDTO> equiposCache = new ArrayList<>();

    public ServidorUI() {
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
        listaEquipos.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int index = listaEquipos.getSelectedIndex();
                    if (index >= 0 && index < equiposCache.size()) {
                        EquipoDTO equipo = equiposCache.get(index);
                        cargarCamaras(equipo.getIdEquipo());
                    }
                }
            }
        });


        SwingUtilities.invokeLater(() -> {
            cargarEquipos();
        });

    }
    private void cargarEquipos() {
        try {
            equiposCache.clear();
            modeloListaEquipos.clear();
            log("Cargando equipos...");

            URL url = new URL(BASE_URL + "/equipos");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseSb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseSb.append(line);
                }
                in.close();

                String respuesta = responseSb.toString();
                log("Respuesta recibida: " + respuesta);

                try {
                    JSONArray jsonEquipos = new JSONArray(respuesta);
                    for (int i = 0; i < jsonEquipos.length(); i++) {
                        JSONObject obj = jsonEquipos.getJSONObject(i);
                        Long idEquipo = obj.getLong("idEquipo");
                        String nombre = obj.getString("nombre");
                        EquipoDTO equipo = new EquipoDTO(idEquipo, nombre);
                        equiposCache.add(equipo);
                        modeloListaEquipos.addElement(nombre);
                    }
                    log("Equipos cargados: " + jsonEquipos.length());
                } catch (org.json.JSONException jsonEx) {
                    log("La respuesta no es un JSON válido. Probablemente no autenticado.");
                    log("Respuesta completa: " + respuesta);
                    // Aquí podrías mostrar un diálogo para pedir login o avisar al usuario
                }

            } else {
                log("Error cargando equipos: HTTP " + code);
            }
        } catch (Exception ex) {
            log("Error cargando equipos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void cargarCamaras(Long equipoId) {
        try {
            modeloTablaCamaras.setRowCount(0);
            log("Cargando cámaras para equipo ID " + equipoId);

            URL url = new URL(BASE_URL + "/equipos/" + equipoId + "/camaras");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json"); // <-- buena práctica

            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseSb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseSb.append(line);
                }
                in.close();

                JSONArray jsonCamaras = new JSONArray(responseSb.toString());

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
                log("Error cargando cámaras: HTTP " + code);
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

    private static class Credentials {
        public final String usuario;
        public final String password;

        public Credentials(String usuario, String password) {
            this.usuario = usuario;
            this.password = password;
        }
    }

    // DTO simple para guardar id y nombre de equipo
    private static class EquipoDTO {
        private Long idEquipo;
        private String nombre;

        public EquipoDTO(Long idEquipo, String nombre) {
            this.idEquipo = idEquipo;
            this.nombre = nombre;
        }

        public Long getIdEquipo() {
            return idEquipo;
        }

        public String getNombre() {
            return nombre;
        }
    }
}