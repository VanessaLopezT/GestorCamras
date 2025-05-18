package com.example.gestorcamras;

import com.example.gestorcamras.model.Informe;
import com.example.gestorcamras.model.Usuario;
import java.util.List;

public class TablaConsolaHelper {
    public static void imprimirTablaInformes(List<Informe> informes) {
        System.out.println("\n================ TABLA INFORMES ================");
        System.out.printf("%-5s | %-20s | %-10s | %-10s | %-20s | %-10s\n", "ID", "Título", "Tamaño", "UsuarioId", "Fecha", "Resumen");
        System.out.println("------------------------------------------------------------------------------------------");
        for (Informe inf : informes) {
            String resumen = inf.getContenido() != null && inf.getContenido().length() > 15 ? inf.getContenido().substring(0, 15) + "..." : inf.getContenido();
            System.out.printf("%-5s | %-20s | %-10.2f | %-10s | %-20s | %-10s\n",
                    inf.getIdInfo(),
                    inf.getTitulo(),
                    inf.getTamaño(),
                    inf.getUsuario() != null ? inf.getUsuario().getIdUsuario() : "-",
                    inf.getFechaGeneracion() != null ? inf.getFechaGeneracion().toString() : "-",
                    resumen);
        }
        System.out.println("=================================================\n");
    }

    public static void imprimirTablaUsuarios(List<Usuario> usuarios) {
        System.out.println("\n================ TABLA USUARIOS ================");
        System.out.printf("%-5s | %-20s | %-25s\n", "ID", "Nombre", "Correo");
        System.out.println("----------------------------------------------------------");
        for (Usuario u : usuarios) {
            System.out.printf("%-5s | %-20s | %-25s\n", u.getIdUsuario(), u.getNombre(), u.getCorreo());
        }
        System.out.println("=================================================\n");
    }
}
