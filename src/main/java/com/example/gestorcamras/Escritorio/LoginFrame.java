package com.example.gestorcamras.Escritorio;

import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginFrame extends JFrame{

    private JTextField tfUsuario;
    private JPasswordField pfClave;
    private JButton btnLogin;
    private JLabel lbEstado;

    public LoginFrame() {
        setTitle("Login - Solo Operadores");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsuario = new JLabel("Usuario:");
        gbc.gridx = 0; gbc.gridy = 0;
        add(lbUsuario, gbc);

        tfUsuario = new JTextField(20);
        gbc.gridx = 1; gbc.gridy =0;
        add(tfUsuario, gbc);
        JLabel lbClave = new JLabel("Contraseña:");
        gbc.gridx = 0; gbc.gridy = 1;
        add(lbClave, gbc);

        pfClave = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(pfClave, gbc);

        btnLogin = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(btnLogin, gbc);

        lbEstado = new JLabel("");
        lbEstado.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(lbEstado, gbc);

        btnLogin.addActionListener(e -> loginUsuario());

        // Enter para login
        pfClave.addActionListener(e -> loginUsuario());
    }

    private void loginUsuario() {
        String usuario = tfUsuario.getText().trim();
        String password = new String(pfClave.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            lbEstado.setText("Debe ingresar usuario y contraseña.");
            return;
        }

        // Intentamos login con backend
        try {
            URL url = new URL("http://localhost:8080/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);

            String params = "username=" + URLEncoder.encode(usuario, "UTF-8") +
                    "&password=" + URLEncoder.encode(password, "UTF-8");

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(params.length()));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();

            if (code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_OK) {
                // Capturamos cookie de sesión
                String headerCookies = conn.getHeaderField("Set-Cookie");
                System.out.println("Set-Cookie recibido: " + headerCookies); // útil para debug

                final String[] cookieSesion = new String[1];
                cookieSesion[0] = null;

                if (headerCookies != null) {
                    String[] cookies = headerCookies.split(";");
                    for (String part : cookies) {
                        part = part.trim();
                        if (part.startsWith("JSESSIONID=")) {
                            cookieSesion[0] = part;
                            break;
                        }
                    }
                }


                if (cookieSesion[0] == null) {
                    lbEstado.setText("Error al obtener cookie de sesión.");
                    return;
                }


                // Ahora consultamos rol con cookie para validar OPERADOR
                URL urlUser = new URL("http://localhost:8080/api/usuario");
                HttpURLConnection connUser = (HttpURLConnection) urlUser.openConnection();
                connUser.setRequestMethod("GET");
                connUser.setRequestProperty("Cookie", cookieSesion[0]);

                int respUser = connUser.getResponseCode();
                if (respUser == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connUser.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    String rol = obj.optString("rol", "");
                    if ("OPERADOR".equalsIgnoreCase(rol)) {
                        SwingUtilities.invokeLater(() -> {
                            ClienteSwing cliente = new ClienteSwing(usuario, cookieSesion[0]);
                            cliente.setVisible(true);
                        });
                        dispose();
                        return;
                    } else {
                        lbEstado.setText("No autorizado: usuario no es OPERADOR.");
                    }
                } else {
                    lbEstado.setText("Error al obtener información del usuario.");
                }

            } else {
                lbEstado.setText("Credenciales inválidas.");
            }

        } catch (Exception ex) {
            lbEstado.setText("Error de conexión: " + ex.getMessage());
        }
    }
}