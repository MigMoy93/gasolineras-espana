package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class VentanaLanzar extends JFrame {
    private JLabel lblEstado;
    private JButton btnLanzar;
    private JPanel panelCentro;
    private JPanel panel1;


    public VentanaLanzar() {
        configurarVentana();
        crearInterfaz();
    }

    private void configurarVentana() {
        setTitle("Automatizador Web");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void crearInterfaz() {

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        add(panelPrincipal);

        // ------ CABECERA ------
        JPanel panelCabecera = new JPanel(new GridLayout(2, 1));
        panelCabecera.setBackground(new Color(60, 63, 65));
        panelCabecera.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel lblTitulo = new JLabel("Automatizador Web", SwingConstants.CENTER);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel lblSubtitulo = new JLabel("Descarga automatizada de documentos", SwingConstants.CENTER);
        lblSubtitulo.setForeground(Color.LIGHT_GRAY);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        panelCabecera.add(lblTitulo);
        panelCabecera.add(lblSubtitulo);
        panelPrincipal.add(panelCabecera, BorderLayout.NORTH);

        // ------ CENTRO ------

        panelCentro = new JPanel();
        panelCentro.setBackground(new Color(245, 245, 245));

        btnLanzar = new JButton("Lanzar tarea");
        btnLanzar.setPreferredSize(new Dimension(160, 40));
        btnLanzar.setBackground(new Color(70, 130, 180));
        btnLanzar.setForeground(Color.WHITE);
        btnLanzar.setFocusPainted(false);
        btnLanzar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnLanzar.addActionListener(e -> lanzarProceso());

        panelCentro.add(btnLanzar);
        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        // ------ ESTADO ------
        JPanel panelEstado = new JPanel(new BorderLayout());
        panelEstado.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        lblEstado = new JLabel("Estado: esperando...");
        panelEstado.add(lblEstado, BorderLayout.WEST);

        panelPrincipal.add(panelEstado, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void lanzarProceso() {
        lblEstado.setText("Estado: ejecutando tarea...");
        btnLanzar.setEnabled(false);

        new Thread(() -> {
            try {
                System.out.println(">> Hilo iniciado");

                org.example.selenium.SeleniumGasolinaLauncher.ejecutar();

                System.out.println(">> Selenium terminado OK");

                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Estado: descarga completada");
                    btnLanzar.setEnabled(true);
                });

            } catch (Exception e) {

                e.printStackTrace(); // 🔥 ESTO ES CLAVE

                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Estado: ERROR (ver consola)");
                    btnLanzar.setEnabled(true);
                });
            }
        }).start();
    }


}
