package com.example.gestorcamras.Escritorio.model;

import javax.swing.table.AbstractTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

public class CamaraTableModel extends AbstractTableModel {
    private JSONArray camaras;
    private final String[] columnNames = {"ID", "Nombre", "IP", "Tipo", "Activa", "Latitud", "Longitud", "Dirección"};

    public CamaraTableModel() {
        this.camaras = new JSONArray();
    }

    public void setCamaras(JSONArray camaras) {
        this.camaras = camaras != null ? camaras : new JSONArray();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return camaras.length();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            JSONObject camara = camaras.getJSONObject(rowIndex);
            switch (columnIndex) {
                case 0: return camara.optInt("idCamara", 0);
                case 1: return camara.optString("nombre", "");
                case 2: return camara.optString("ip", "");
                case 3: return camara.optString("tipo", "");
                case 4: return camara.optBoolean("activa", false) ? "Sí" : "No";
                case 5: return camara.has("latitud") ? camara.optDouble("latitud", 0.0) : "";
                case 6: return camara.has("longitud") ? camara.optDouble("longitud", 0.0) : "";
                case 7: return camara.optString("direccion", "");
                default: return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Integer.class; // ID
        } else if (columnIndex == 4) {
            return String.class; // Activa (Sí/No)
        } else if (columnIndex == 5 || columnIndex == 6) {
            // Para latitud y longitud, devolvemos Object.class para manejar tanto Double como String vacío
            return Object.class;
        }
        return String.class; // Nombre, IP, Tipo, Dirección
    }
}
