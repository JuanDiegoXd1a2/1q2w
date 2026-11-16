package frmcliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FrmPodeshopPOS extends JFrame {
    private JPanel panelProductos, panelCategorias;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JLabel lblTotal;
    
    // Credenciales de tu base de datos SQL Server
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=podeshop;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "Podeshop2026*";

    private double subtotalGeneral = 0.0;

    public FrmPodeshopPOS() {
        setTitle("PODESHOP - Sistema de Ventas y Caja");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ==========================================
        // 1. PANEL SUPERIOR (CABECERA Y CATEGORÍAS)
        // ==========================================
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBackground(new Color(20, 30, 55));
        
        JLabel lblTitulo = new JLabel("  PODESHOP - POS Principal", JLabel.LEFT);
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panelTop.add(lblTitulo, BorderLayout.WEST);

        panelCategorias = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelCategorias.setOpaque(false);
        cargarBotonesCategorias(panelTop);
        panelTop.add(panelCategorias, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL CENTRAL (CATÁLOGO DE PRODUCTOS)
        // ==========================================
        panelProductos = new JPanel(new GridLayout(0, 3, 15, 15));
        panelProductos.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollProductos = new JScrollPane(panelProductos);
        add(scrollProductos, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL DERECHO (CARRITO / PEDIDO ACTUAL)
        // ==========================================
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setPreferredSize(new Dimension(380, 0));
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Pedido de la Mesa / Venta"));

        modeloCarrito = new DefaultTableModel(new Object[]{"ID", "Producto", "Cant", "P.Unit", "Subtotal"}, 0);
        tablaCarrito = new JTable(modeloCarrito);
        panelDerecho.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        // Panel inferior del carrito (Totales y Botón Procesar Venta)
        JPanel panelTotales = new JPanel(new GridLayout(3, 1, 5, 5));
        panelTotales.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblTotal = new JLabel("TOTAL: S/ 0.00", JLabel.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelTotales.add(lblTotal);

        JButton btnProcesar = new JButton("Procesar Venta y Pagar");
        btnProcesar.setBackground(new Color(40, 167, 69));
        btnProcesar.setForeground(Color.WHITE);
        btnProcesar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelTotales.add(btnProcesar);

        JButton btnLimpiar = new JButton("Anular / Limpiar Carrito");
        btnLimpiar.setBackground(new Color(220, 53, 69));
        btnLimpiar.setForeground(Color.WHITE);
        panelTotales.add(btnLimpiar);

        panelDerecho.add(panelTotales, BorderLayout.SOUTH);
        add(panelDerecho, BorderLayout.EAST);

        // Cargar productos iniciales de la BD
        cargarProductosBD(0);

        // Eventos corregidos de los botones
        btnLimpiar.addActionListener(e -> limpiarCarrito());
        btnProcesar.addActionListener(e -> procesarVentaBD());
    }

    // Cargar productos desde la base de datos SQL Server
    private void cargarProductosBD(int idCategoriaFiltro) {
        panelProductos.removeAll();
        String sql = "SELECT id_producto, nombre, descripcion, precio_venta FROM dbo.PRODUCTO";
        if (idCategoriaFiltro > 0) {
            sql += " WHERE id_categoria = " + idCategoriaFiltro;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String desc = rs.getString("descripcion");
                double precio = rs.getDouble("precio_venta");

                // Crear tarjeta visual para cada producto
                JPanel card = new JPanel(new BorderLayout());
                card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
                card.setBackground(Color.WHITE);

                JTextArea txtInfo = new JTextArea(nombre + "\n\nS/ " + String.format("%.2f", precio));
                txtInfo.setEditable(false);
                txtInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
                txtInfo.setMargin(new Insets(10, 10, 10, 10));
                card.add(txtInfo, BorderLayout.CENTER);

                JButton btnAgregar = new JButton("+ Agregar");
                btnAgregar.setBackground(new Color(0, 123, 255));
                btnAgregar.setForeground(Color.WHITE);
                btnAgregar.addActionListener(e -> agregarAlCarrito(id, nombre, precio));
                card.add(btnAgregar, BorderLayout.SOUTH);

                panelProductos.add(card);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + ex.getMessage());
        }

        panelProductos.revalidate();
        panelProductos.repaint();
    }

    // Cargar botones de categorías dinámicamente
    private void cargarBotonesCategorias(JPanel panelTop) {
        JButton btnTodos = new JButton("Todos");
        btnTodos.addActionListener(e -> cargarProductosBD(0));
        panelCategorias.add(btnTodos);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_categoria, nombre_categoria FROM dbo.CATEGORIA")) {

            while (rs.next()) {
                int idCat = rs.getInt("id_categoria");
                String nombreCat = rs.getString("nombre_categoria");
                JButton btnCat = new JButton(nombreCat);
                btnCat.addActionListener(e -> cargarProductosBD(idCat));
                panelCategorias.add(btnCat);
            }
        } catch (SQLException ex) {
            // Manejo silencioso
        }
    }

    private void agregarAlCarrito(int idProducto, String nombre, double precio) {
        modeloCarrito.addRow(new Object[]{idProducto, nombre, 1, precio, precio});
        calcularTotal();
    }

    private void calcularTotal() {
        subtotalGeneral = 0.0;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            subtotalGeneral += (double) modeloCarrito.getValueAt(i, 4);
        }
        double igv = subtotalGeneral * 0.18;
        double total = subtotalGeneral + igv;
        lblTotal.setText(String.format("TOTAL: S/ %.2f", total));
    }

    private void limpiarCarrito() {
        modeloCarrito.setRowCount(0);
        lblTotal.setText("TOTAL: S/ 0.00");
    }

    // Registrar la venta en las tablas VENTA, DETALLE_VENTA y actualizar INVENTARIO
    private void procesarVentaBD() {
        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sqlVenta = "INSERT INTO dbo.VENTA (id_cliente, id_empleado, fecha_venta, canal_venta, monto_total, estado) VALUES (?, ?, GETDATE(), ?, ?, ?)";
        String sqlDetalle = "INSERT INTO dbo.DETALLE_VENTA (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE dbo.INVENTARIO SET stock_actual = stock_actual - ? WHERE id_producto = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Transacción segura

            double igv = subtotalGeneral * 0.18;
            double totalFinal = subtotalGeneral + igv;

            PreparedStatement pstVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            pstVenta.setInt(1, 1);
            pstVenta.setInt(2, 1);
            pstVenta.setString(3, "Presencial");
            pstVenta.setDouble(4, totalFinal);
            pstVenta.setString(5, "Pagada");
            pstVenta.executeUpdate();

            ResultSet rsKeys = pstVenta.getGeneratedKeys();
            int idVentaGenerada = 0;
            if (rsKeys.next()) {
                idVentaGenerada = rsKeys.getInt(1);
            }

            PreparedStatement pstDetalle = conn.prepareStatement(sqlDetalle);
            PreparedStatement pstStock = conn.prepareStatement(sqlStock);

            for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                int idProd = (int) modeloCarrito.getValueAt(i, 0);
                int cant = (int) modeloCarrito.getValueAt(i, 2);
                double pUnit = (double) modeloCarrito.getValueAt(i, 3);
                double sub = (double) modeloCarrito.getValueAt(i, 4);

                pstDetalle.setInt(1, idVentaGenerada);
                pstDetalle.setInt(2, idProd);
                pstDetalle.setInt(3, cant);
                pstDetalle.setDouble(4, pUnit);
                pstDetalle.setDouble(5, sub);
                pstDetalle.executeUpdate();

                pstStock.setInt(1, cant);
                pstStock.setInt(2, idProd);
                pstStock.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "¡Venta procesada con éxito!\nRegistrada en la base de datos con ID: " + idVentaGenerada);
            limpiarCarrito();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar la venta: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FrmPodeshopPOS().setVisible(true);
        });
    }
}