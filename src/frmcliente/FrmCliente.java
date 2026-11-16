package frmcliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FrmCliente extends JFrame {
    private JTextField txtTipoDoc, txtNumDoc, txtNombre, txtTelefono, txtDireccion;
    private JButton btnGuardar;

    // Credenciales de tu contenedor SQL Server local
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=podeshop;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Podeshop2026*";

    public FrmCliente() {
        setTitle("Registro de Clientes - PODESHOP");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTipo = new JLabel("Tipo Doc (DNI/RUC):");
        lblTipo.setBounds(20, 20, 130, 25);
        add(lblTipo);

        txtTipoDoc = new JTextField();
        txtTipoDoc.setBounds(160, 20, 180, 25);
        add(txtTipoDoc);

        JLabel lblNum = new JLabel("N° Documento:");
        lblNum.setBounds(20, 60, 130, 25);
        add(lblNum);

        txtNumDoc = new JTextField();
        txtNumDoc.setBounds(160, 60, 180, 25);
        add(txtNumDoc);

        JLabel lblNombre = new JLabel("Nombre / Razón Social:");
        lblNombre.setBounds(20, 100, 130, 25);
        add(lblNombre);

        txtNombre = new JTextField();
        txtNombre.setBounds(160, 100, 180, 25);
        add(txtNombre);

        JLabel lblTel = new JLabel("Teléfono:");
        lblTel.setBounds(20, 140, 130, 25);
        add(lblTel);

        txtTelefono = new JTextField();
        txtTelefono.setBounds(160, 140, 180, 25);
        add(txtTelefono);

        JLabel lblDir = new JLabel("Dirección:");
        lblDir.setBounds(20, 180, 130, 25);
        add(lblDir);

        txtDireccion = new JTextField();
        txtDireccion.setBounds(160, 180, 180, 25);
        add(txtDireccion);

        btnGuardar = new JButton("Guardar Cliente");
        btnGuardar.setBounds(120, 230, 150, 35);
        add(btnGuardar);

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCliente();
            }
        });
    }

    private void guardarCliente() {
        String sql = "INSERT INTO dbo.CLIENTE (tipo_documento, num_documento, nombre_razon_social, telefono, direccion) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtTipoDoc.getText());
            pstmt.setString(2, txtNumDoc.getText());
            pstmt.setString(3, txtNombre.getText());
            pstmt.setString(4, txtTelefono.getText());
            pstmt.setString(5, txtDireccion.getText());

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "¡Cliente registrado con éxito en la base de datos!");
            
            txtTipoDoc.setText("");
            txtNumDoc.setText("");
            txtNombre.setText("");
            txtTelefono.setText("");
            txtDireccion.setText("");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FrmCliente().setVisible(true);
        });
    }
}