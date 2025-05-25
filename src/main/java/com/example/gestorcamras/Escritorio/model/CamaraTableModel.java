package com.example.gestorcamras.Escritorio.model;

import javax.swing.table.AbstractTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

public class CamaraTableModel extends AbstractTableModel {
    private JSONArray camaras;
    private final String[] columnNames = {"ID", "Nombre", "IP", "Tipo", "Activa"};

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
                default: return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 4) {
            return String.class;
        } else if (columnIndex == 0) {
            return Integer.class;
        }
        return String.class;
    }
}
