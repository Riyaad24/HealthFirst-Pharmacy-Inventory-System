import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class PIMSApplication {
    private static final String URL = "jdbc:mysql://localhost:3306/pims";
    private static final String DB_USER = System.getenv().getOrDefault("PIMS_DB_USER", "root");
    private String dbPassword = System.getenv().getOrDefault("PIMS_DB_PASSWORD", "");

    private JFrame frame;
    private Connection connection;
    private int loggedInUserId;
    private String loggedInRole;
    private String loggedInName;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("Panel.background", new Color(244, 248, 247));
            UIManager.put("TabbedPane.selected", new Color(217, 238, 232));
            UIManager.put("Button.background", new Color(28, 112, 96));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Table.selectionBackground", new Color(190, 225, 216));
            UIManager.put("Table.selectionForeground", Color.BLACK);
            new PIMSApplication().showLogin();
        });
    }

    private void showLogin() {
        frame = new JFrame("HealthFirst Pharmacy Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 260);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton login = new JButton("Login");

        panel.add(new JLabel("HealthFirst Pharmacy", SwingConstants.CENTER));
        panel.add(new JLabel("Secure staff login", SwingConstants.CENTER));
        panel.add(new JLabel("Username"));
        panel.add(username);
        panel.add(new JLabel("Password"));
        panel.add(password);
        panel.add(new JLabel());
        panel.add(login);
        panel.add(new JLabel("Admin: admin / admin123"));
        panel.add(new JLabel("Cashier: cashier / cash123"));
        frame.add(panel);
        login.addActionListener(e -> login(username.getText(), new String(password.getPassword())));
        password.addActionListener(e -> login(username.getText(), new String(password.getPassword())));
        frame.setVisible(true);
    }

    private void login(String username, String password) {
        try {
            connect();
            String sql = "SELECT user_id, role, full_name FROM users WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username.trim());
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                loggedInUserId = result.getInt("user_id");
                loggedInRole = result.getString("role");
                JTextField nameField = new JTextField(result.getString("full_name"));
                int nameChoice = JOptionPane.showConfirmDialog(frame, nameField, "Enter your name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (nameChoice != JOptionPane.OK_OPTION || nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a name before continuing");
                    return;
                }
                loggedInName = nameField.getText().trim();
                PreparedStatement updateName = connection.prepareStatement("UPDATE users SET full_name = ? WHERE user_id = ?");
                updateName.setString(1, loggedInName);
                updateName.setInt(2, loggedInUserId);
                updateName.executeUpdate();
                frame.dispose();
                try {
                    showDashboard();
                } catch (RuntimeException error) {
                    showLogin();
                    JOptionPane.showMessageDialog(frame, "Could not open the workspace: " + error.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Username or password is incorrect");
            }
        } catch (SQLException error) {
            showDatabaseError(error);
        }
    }

    private void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (dbPassword.isEmpty()) {
                JPasswordField passwordField = new JPasswordField();
                int choice = JOptionPane.showConfirmDialog(frame, passwordField, "Enter MySQL password for " + DB_USER, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) {
                    throw new SQLException("MySQL password was not entered");
                }
                dbPassword = new String(passwordField.getPassword());
            }
            connection = DriverManager.getConnection(URL, DB_USER, dbPassword);
            ensureSalesStaffColumn();
        }
    }

    private void ensureSalesStaffColumn() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sales' AND column_name = 'staff_name'";
        try (PreparedStatement check = connection.prepareStatement(checkSql); ResultSet result = check.executeQuery()) {
            result.next();
            if (result.getInt(1) == 0) {
                connection.createStatement().executeUpdate("ALTER TABLE sales ADD COLUMN staff_name VARCHAR(100) NOT NULL DEFAULT 'Unknown'");
            }
        }
        addColumnIfMissing("sales", "customer_name", "VARCHAR(150) NOT NULL DEFAULT 'Walk in customer'");
        addColumnIfMissing("sales", "payment_method", "VARCHAR(30) NOT NULL DEFAULT 'Cash'");
        addColumnIfMissing("sales", "card_number", "VARCHAR(20) NULL");
        addColumnIfMissing("sales", "tendered_amount", "DECIMAL(10,2) NOT NULL DEFAULT 0");
        addColumnIfMissing("sales", "change_amount", "DECIMAL(10,2) NOT NULL DEFAULT 0");
        addColumnIfMissing("medicines", "medicine_category", "ENUM('Prescribed', 'Over the Counter') NOT NULL DEFAULT 'Over the Counter'");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (PreparedStatement check = connection.prepareStatement(checkSql)) {
            check.setString(1, tableName);
            check.setString(2, columnName);
            ResultSet result = check.executeQuery();
            result.next();
            if (result.getInt(1) == 0) connection.createStatement().executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void showDashboard() {
        frame = new JFrame("HealthFirst Pharmacy Inventory System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("  " + (loggedInRole.equals("Admin") ? "Administrator workspace" : "Cashier workspace") + " | " + loggedInName);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        JButton signOut = new JButton("Sign out");
        header.add(title, BorderLayout.WEST);
        header.add(signOut, BorderLayout.EAST);
        frame.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (loggedInRole.equals("Admin")) {
            tabs.addTab("Admin home", new AdminHomePanel());
            tabs.addTab("Point of Sale", new POSPanel());
            tabs.addTab("Medicines", new MedicinePanel());
            tabs.addTab("Suppliers", new SupplierPanel());
            tabs.addTab("Users", new UserPanel());
            tabs.addTab("Sales history", new SalesHistoryPanel(true));
            tabs.addTab("Reports", new ReportPanel());
        } else {
            tabs.addTab("Point of Sale", new POSPanel());
            tabs.addTab("Stock Check", new StockPanel());
            tabs.addTab("Sales history", new SalesHistoryPanel(false));
        }
        frame.add(tabs, BorderLayout.CENTER);
        signOut.addActionListener(e -> signOut());
        frame.setVisible(true);
    }

    private void signOut() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) { }
        }
        loggedInUserId = 0;
        loggedInRole = null;
        frame.dispose();
        showLogin();
    }

    private class AdminHomePanel extends JPanel {
        AdminHomePanel() {
            setLayout(new GridLayout(3, 1, 12, 12));
            setBorder(BorderFactory.createEmptyBorder(35, 80, 35, 80));
            JLabel welcome = new JLabel("Welcome to the administrator workspace", SwingConstants.CENTER);
            welcome.setFont(new Font("Arial", Font.BOLD, 22));
            JLabel details = new JLabel("Manage stock, suppliers, cashier accounts and business reports", SwingConstants.CENTER);
            details.setFont(new Font("Arial", Font.PLAIN, 16));
            add(welcome);
            add(details);
            add(new JLabel("Use the tabs above to open an administration task", SwingConstants.CENTER));
        }
    }

    private JTable makeTable(String[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(28, 112, 96));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private void showDatabaseError(SQLException error) {
        JOptionPane.showMessageDialog(frame, "Database error: " + error.getMessage());
    }

    private class POSPanel extends JPanel {
        private DefaultTableModel cartModel;
        private JTextField medicineId = new JTextField();
        private JTextField quantity = new JTextField("1");
        private JLabel totalLabel = new JLabel("Total: R0.00");
        private double subtotal;
        private double taxTotal;

        POSPanel() {
            setLayout(new BorderLayout(8, 8));
            JPanel top = new JPanel(new GridLayout(2, 4, 8, 8));
            JButton add = new JButton("Add to cart");
            JButton clear = new JButton("Clear cart");
            JButton checkout = new JButton("Checkout");
            top.add(new JLabel("Medicine ID"));
            top.add(medicineId);
            top.add(new JLabel("Quantity"));
            top.add(quantity);
            top.add(add);
            top.add(clear);
            top.add(checkout);
            top.add(totalLabel);
            add(top, BorderLayout.NORTH);

            cartModel = new DefaultTableModel(new String[]{"Medicine", "Type", "Category", "Price", "Quantity", "Tax", "Total"}, 0);
            add(new JScrollPane(new JTable(cartModel)), BorderLayout.CENTER);
            add.addActionListener(e -> addItem());
            clear.addActionListener(e -> clearCart());
            checkout.addActionListener(e -> checkout());
        }

        private void addItem() {
            try {
                connect();
                int id = Integer.parseInt(medicineId.getText());
                int amount = Integer.parseInt(quantity.getText());
                PreparedStatement statement = connection.prepareStatement("SELECT name, medicine_type, medicine_category, price, quantity_in_stock FROM medicines WHERE medicine_id = ?");
                statement.setInt(1, id);
                ResultSet result = statement.executeQuery();
                if (!result.next()) {
                    JOptionPane.showMessageDialog(frame, "Medicine was not found");
                    return;
                }
                if (amount < 1 || amount > result.getInt("quantity_in_stock")) {
                    JOptionPane.showMessageDialog(frame, "The requested quantity is not available");
                    return;
                }
                double price = result.getDouble("price");
                double lineTotal = price * amount;
                String category = result.getString("medicine_category");
                double lineTax = category.equals("Over the Counter") ? lineTotal * 0.15 : 0;
                cartModel.addRow(new Object[]{id + " " + result.getString("name"), result.getString("medicine_type"), category, price, amount, lineTax, lineTotal + lineTax});
                subtotal += lineTotal;
                taxTotal += lineTax;
                updateTotal();
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(frame, "Enter valid numbers");
            } catch (SQLException error) {
                showDatabaseError(error);
            }
        }

        private void clearCart() {
            cartModel.setRowCount(0);
            subtotal = 0;
            taxTotal = 0;
            totalLabel.setText("Total: R0.00");
        }

        private void updateTotal() {
            totalLabel.setText(String.format("Total: R%.2f", subtotal + taxTotal));
        }

        private void checkout() {
            if (cartModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame, "The cart is empty");
                return;
            }
            try {
                connect();
                JTextField customerField = new JTextField();
                int customerChoice = JOptionPane.showConfirmDialog(frame, customerField, "Enter customer name and surname", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (customerChoice != JOptionPane.OK_OPTION || customerField.getText().trim().isEmpty()) return;
                String customerName = customerField.getText().trim();
                double saleTotal = subtotal + taxTotal;
                String[] paymentOptions = {"Cash", "Card"};
                String paymentMethod = (String) JOptionPane.showInputDialog(frame, "Select payment method", "Payment", JOptionPane.PLAIN_MESSAGE, null, paymentOptions, paymentOptions[0]);
                if (paymentMethod == null) return;
                String cardNumber = null;
                double tendered = saleTotal;
                double change = 0;
                if (paymentMethod.equals("Cash")) {
                    JTextField cashField = new JTextField();
                    int cashChoice = JOptionPane.showConfirmDialog(frame, cashField, "Enter cash received for R" + String.format("%.2f", saleTotal), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (cashChoice != JOptionPane.OK_OPTION) return;
                    tendered = Double.parseDouble(cashField.getText().trim());
                    if (tendered < saleTotal) {
                        JOptionPane.showMessageDialog(frame, "The amount received is less than the sale total");
                        return;
                    }
                    change = tendered - saleTotal;
                } else {
                    String[] cardTypes = {"Visa", "Mastercard"};
                    String cardType = (String) JOptionPane.showInputDialog(frame, "Select card type", "Card payment", JOptionPane.PLAIN_MESSAGE, null, cardTypes, cardTypes[0]);
                    if (cardType == null) return;
                    cardNumber = cardType.equals("Visa") ? "xxxxxxxxxxxx3457" : "xxxxxxxxxxxx7890";
                }
                connection.setAutoCommit(false);
                PreparedStatement sale = connection.prepareStatement("INSERT INTO sales(total_amount, user_id, staff_name, customer_name, payment_method, card_number, tendered_amount, change_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                sale.setDouble(1, saleTotal);
                sale.setInt(2, loggedInUserId);
                sale.setString(3, loggedInName);
                sale.setString(4, customerName);
                sale.setString(5, paymentMethod);
                sale.setString(6, cardNumber);
                sale.setDouble(7, tendered);
                sale.setDouble(8, change);
                sale.executeUpdate();
                ResultSet keys = sale.getGeneratedKeys();
                keys.next();
                int saleId = keys.getInt(1);
                for (int row = 0; row < cartModel.getRowCount(); row++) {
                    String item = cartModel.getValueAt(row, 0).toString();
                    int id = Integer.parseInt(item.substring(0, item.indexOf(' ')));
                    int amount = (Integer) cartModel.getValueAt(row, 4);
                    double price = (Double) cartModel.getValueAt(row, 3);
                    PreparedStatement line = connection.prepareStatement("INSERT INTO sale_items(sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)");
                    line.setInt(1, saleId);
                    line.setInt(2, id);
                    line.setInt(3, amount);
                    line.setDouble(4, price);
                    line.executeUpdate();
                    PreparedStatement stock = connection.prepareStatement("UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?");
                    stock.setInt(1, amount);
                    stock.setInt(2, id);
                    stock.executeUpdate();
                }
                connection.commit();
                showBill(saleId, customerName, paymentMethod, cardNumber, tendered, change);
                clearCart();
                connection.setAutoCommit(true);
            } catch (SQLException error) {
                try { connection.rollback(); } catch (SQLException ignored) { }
                showDatabaseError(error);
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(frame, "Enter a valid cash amount");
            }
        }

        private void showBill(int saleId, String customerName, String paymentMethod, String cardNumber, double tendered, double change) {
            String saleTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new java.util.Date());
            String cardLine = cardNumber == null ? "" : "\nCard number: " + cardNumber;
            String billText = "========================================\n          HEALTHFIRST PHARMACY\n       12 Main Street, Johannesburg\n       Tel: 011 555 0100 | VAT registered\n========================================\nCustomer: " + customerName + "\nServed by: " + loggedInName + "\nSale number: " + saleId + "\nDate: " + saleTime + "\n----------------------------------------\nItems purchased\n----------------------------------------\n" + cartText() + "----------------------------------------\nSubtotal: R" + String.format("%.2f", subtotal) + "\nTax: R" + String.format("%.2f", taxTotal) + "\nTotal: R" + String.format("%.2f", subtotal + taxTotal) + "\nPayment method: " + paymentMethod + cardLine + "\nTendered: R" + String.format("%.2f", tendered) + "\nCash change: R" + String.format("%.2f", change) + "\n========================================\nThank you for shopping with HealthFirst.";
            JTextArea bill = new JTextArea(billText, 15, 35);
            bill.setEditable(false);
            JButton save = new JButton("Save to history");
            JButton print = new JButton("Print bill");
            JButton close = new JButton("Close");
            JPanel buttons = new JPanel();
            buttons.add(save);
            buttons.add(print);
            buttons.add(close);
            JDialog dialog = new JDialog(frame, "Customer Bill", true);
            dialog.setLayout(new BorderLayout(8, 8));
            dialog.add(new JScrollPane(bill), BorderLayout.CENTER);
            dialog.add(buttons, BorderLayout.SOUTH);
            save.addActionListener(e -> {
                JOptionPane.showMessageDialog(dialog, "Invoice saved to sales history");
                dialog.dispose();
            });
            print.addActionListener(e -> {
                try {
                    bill.print();
                    JOptionPane.showMessageDialog(dialog, "Invoice printed and saved to sales history");
                }
                catch (Exception error) { JOptionPane.showMessageDialog(dialog, "Could not print bill: " + error.getMessage()); }
            });
            close.addActionListener(e -> dialog.dispose());
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }

        private String cartText() {
            StringBuilder text = new StringBuilder();
            for (int row = 0; row < cartModel.getRowCount(); row++) {
                text.append(cartModel.getValueAt(row, 0)).append(" | ").append(cartModel.getValueAt(row, 2)).append(" | Qty: ").append(cartModel.getValueAt(row, 4)).append(" | Tax: R").append(String.format("%.2f", cartModel.getValueAt(row, 5))).append(" | Total: R").append(String.format("%.2f", cartModel.getValueAt(row, 6))).append("\n");
            }
            return text.toString();
        }
    }

    private class StockPanel extends JPanel {
        StockPanel() {
            setLayout(new BorderLayout());
            JTextField search = new JTextField();
            JButton find = new JButton("Check medicine");
            JTable table = makeTable(new String[]{"ID", "Name", "Type", "Category", "Price", "Available", "Expiry"});
            JPanel top = new JPanel(new BorderLayout(8, 8));
            top.add(search, BorderLayout.CENTER);
            JPanel actions = new JPanel();
            JButton changeCategory = new JButton("Change tax category");
            actions.add(find);
            actions.add(changeCategory);
            top.add(actions, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            find.addActionListener(e -> loadStock(table, search.getText()));
            changeCategory.addActionListener(e -> changeMedicineCategory(table));
        }
    }

    private class SalesHistoryPanel extends JPanel {
        SalesHistoryPanel(boolean showAllSales) {
            setLayout(new BorderLayout());
            JTable table = makeTable(new String[]{"Sale number", "Time", "Customer", "Products sold", "Total", "Payment", "Staff"});
            JButton refresh = new JButton("Refresh history");
            add(refresh, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            refresh.addActionListener(e -> loadSalesHistory(table, showAllSales));
            loadSalesHistory(table, showAllSales);
        }
    }

    private void loadSalesHistory(JTable table, boolean showAllSales) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        try {
            connect();
            String sql = "SELECT s.sale_id, s.sale_date, s.customer_name, GROUP_CONCAT(CONCAT(m.name, ' (', m.medicine_type, ') x ', si.quantity_sold) SEPARATOR ', '), s.total_amount, s.payment_method, s.staff_name FROM sales s JOIN sale_items si ON s.sale_id = si.sale_id JOIN medicines m ON si.medicine_id = m.medicine_id";
            if (!showAllSales) sql += " WHERE s.user_id = ?";
            sql += " GROUP BY s.sale_id, s.sale_date, s.customer_name, s.total_amount, s.payment_method, s.staff_name ORDER BY s.sale_date DESC";
            PreparedStatement statement = connection.prepareStatement(sql);
            if (!showAllSales) statement.setInt(1, loggedInUserId);
            ResultSet result = statement.executeQuery();
            SimpleDateFormat historyFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            while (result.next()) model.addRow(new Object[]{result.getInt(1), historyFormat.format(result.getTimestamp(2)), result.getString(3), result.getString(4), result.getDouble(5), result.getString(6), result.getString(7)});
        } catch (SQLException error) { showDatabaseError(error); }
    }

    private void loadStock(JTable table, String search) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        try {
            connect();
            PreparedStatement statement = connection.prepareStatement("SELECT medicine_id, name, medicine_type, medicine_category, price, quantity_in_stock, expiry_date FROM medicines WHERE name LIKE ?");
            statement.setString(1, "%" + search + "%");
            ResultSet result = statement.executeQuery();
            while (result.next()) model.addRow(new Object[]{result.getInt(1), result.getString(2), result.getString(3), result.getString(4), result.getDouble(5), result.getInt(6), result.getDate(7)});
        } catch (SQLException error) { showDatabaseError(error); }
    }

    private void changeMedicineCategory(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String[] options = {"Prescribed", "Over the Counter"};
        String category = (String) JOptionPane.showInputDialog(frame, "Select medicine category", "Tax category", JOptionPane.PLAIN_MESSAGE, null, options, table.getValueAt(row, 3));
        if (category == null) return;
        try {
            connect();
            PreparedStatement statement = connection.prepareStatement("UPDATE medicines SET medicine_category = ? WHERE medicine_id = ?");
            statement.setString(1, category);
            statement.setInt(2, (Integer) table.getValueAt(row, 0));
            statement.executeUpdate();
            loadStock(table, "");
        } catch (SQLException error) { showDatabaseError(error); }
    }

    private class MedicinePanel extends JPanel {
        MedicinePanel() {
            setLayout(new BorderLayout());
            JTable table = makeTable(new String[]{"ID", "Name", "Company", "Type", "Category", "Price", "Quantity", "Reorder", "Expiry", "Supplier"});
            JPanel buttons = new JPanel();
            JButton refresh = new JButton("Refresh");
            JButton add = new JButton("Add medicine");
            JButton edit = new JButton("Edit selected");
            JButton delete = new JButton("Delete selected");
            buttons.add(refresh); buttons.add(add); buttons.add(edit); buttons.add(delete);
            add(buttons, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);
            refresh.addActionListener(e -> loadMedicines(table));
            add.addActionListener(e -> medicineForm(table));
            edit.addActionListener(e -> editMedicine(table));
            delete.addActionListener(e -> deleteMedicine(table));
            loadMedicines(table);
        }
    }

    private void loadMedicines(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel(); model.setRowCount(0);
        try {
            connect(); ResultSet result = connection.createStatement().executeQuery("SELECT medicine_id, name, company, medicine_type, medicine_category, price, quantity_in_stock, reorder_level, expiry_date, supplier_id FROM medicines");
            while (result.next()) model.addRow(new Object[]{result.getInt(1), result.getString(2), result.getString(3), result.getString(4), result.getString(5), result.getDouble(6), result.getInt(7), result.getInt(8), result.getDate(9), result.getInt(10)});
        } catch (SQLException error) { showDatabaseError(error); }
    }

    private void medicineForm(JTable table) {
        JTextField name = new JTextField(), company = new JTextField(), type = new JTextField("Tablet"), category = new JTextField("Over the Counter"), price = new JTextField(), quantity = new JTextField(), reorder = new JTextField(), expiry = new JTextField("2027-12-31"), supplier = new JTextField();
        JTextField[] fields = {name, company, type, category, price, quantity, reorder, expiry, supplier};
        JPanel panel = new JPanel(new GridLayout(9, 2)); String[] labels = {"Name", "Company", "Type", "Category", "Price", "Quantity", "Reorder level", "Expiry yyyy mm dd", "Supplier ID"};
        for (int i = 0; i < fields.length; i++) { panel.add(new JLabel(labels[i])); panel.add(fields[i]); }
        if (JOptionPane.showConfirmDialog(frame, panel, "Add medicine", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                connect(); PreparedStatement statement = connection.prepareStatement("INSERT INTO medicines(name, company, medicine_type, medicine_category, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setString(1, name.getText()); statement.setString(2, company.getText()); statement.setString(3, type.getText()); statement.setString(4, category.getText()); statement.setDouble(5, Double.parseDouble(price.getText())); statement.setInt(6, Integer.parseInt(quantity.getText())); statement.setInt(7, Integer.parseInt(reorder.getText())); statement.setDate(8, Date.valueOf(LocalDate.parse(expiry.getText()))); statement.setInt(9, Integer.parseInt(supplier.getText())); statement.executeUpdate(); loadMedicines(table);
            } catch (Exception error) { JOptionPane.showMessageDialog(frame, "Could not add medicine: " + error.getMessage()); }
        }
    }

    private void deleteMedicine(JTable table) {
        int row = table.getSelectedRow(); if (row < 0) return;
        try { connect(); PreparedStatement statement = connection.prepareStatement("DELETE FROM medicines WHERE medicine_id = ?"); statement.setInt(1, (Integer) table.getValueAt(row, 0)); statement.executeUpdate(); loadMedicines(table); }
        catch (SQLException error) { showDatabaseError(error); }
    }

    private void editMedicine(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        JTextField name = new JTextField(table.getValueAt(row, 1).toString());
        JTextField company = new JTextField(table.getValueAt(row, 2).toString());
        JTextField type = new JTextField(table.getValueAt(row, 3).toString());
        JTextField category = new JTextField(table.getValueAt(row, 4).toString());
        JTextField price = new JTextField(table.getValueAt(row, 5).toString());
        JTextField quantity = new JTextField(table.getValueAt(row, 6).toString());
        JTextField reorder = new JTextField(table.getValueAt(row, 7).toString());
        JTextField expiry = new JTextField(table.getValueAt(row, 8).toString());
        JTextField supplier = new JTextField(table.getValueAt(row, 9).toString());
        JTextField[] fields = {name, company, type, category, price, quantity, reorder, expiry, supplier};
        JPanel panel = new JPanel(new GridLayout(9, 2));
        String[] labels = {"Name", "Company", "Type", "Category", "Price", "Quantity", "Reorder level", "Expiry", "Supplier ID"};
        for (int i = 0; i < fields.length; i++) { panel.add(new JLabel(labels[i])); panel.add(fields[i]); }
        if (JOptionPane.showConfirmDialog(frame, panel, "Edit medicine", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                connect();
                PreparedStatement statement = connection.prepareStatement("UPDATE medicines SET name = ?, company = ?, medicine_type = ?, medicine_category = ?, price = ?, quantity_in_stock = ?, reorder_level = ?, expiry_date = ?, supplier_id = ? WHERE medicine_id = ?");
                statement.setString(1, name.getText()); statement.setString(2, company.getText()); statement.setString(3, type.getText()); statement.setString(4, category.getText()); statement.setDouble(5, Double.parseDouble(price.getText())); statement.setInt(6, Integer.parseInt(quantity.getText())); statement.setInt(7, Integer.parseInt(reorder.getText())); statement.setDate(8, Date.valueOf(LocalDate.parse(expiry.getText()))); statement.setInt(9, Integer.parseInt(supplier.getText())); statement.setInt(10, (Integer) table.getValueAt(row, 0)); statement.executeUpdate(); loadMedicines(table);
            } catch (Exception error) { JOptionPane.showMessageDialog(frame, "Could not edit medicine: " + error.getMessage()); }
        }
    }

    private class SupplierPanel extends JPanel {
        SupplierPanel() {
            setLayout(new BorderLayout()); JTable table = makeTable(new String[]{"ID", "Name", "Contact", "Phone", "Email", "Address"});
            JPanel buttons = new JPanel(); JButton refresh = new JButton("Refresh suppliers"); JButton add = new JButton("Add supplier"); JButton edit = new JButton("Edit selected"); JButton delete = new JButton("Delete selected"); buttons.add(refresh); buttons.add(add); buttons.add(edit); buttons.add(delete); add(buttons, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER); refresh.addActionListener(e -> loadSuppliers(table)); add.addActionListener(e -> addSupplier(table)); edit.addActionListener(e -> editSupplier(table)); delete.addActionListener(e -> deleteSupplier(table)); loadSuppliers(table);
        }
    }

    private void loadSuppliers(JTable table) { DefaultTableModel model = (DefaultTableModel) table.getModel(); model.setRowCount(0); try { connect(); ResultSet result = connection.createStatement().executeQuery("SELECT supplier_id, name, contact_person, phone, email, address FROM suppliers"); while (result.next()) model.addRow(new Object[]{result.getInt(1), result.getString(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6)}); } catch (SQLException error) { showDatabaseError(error); } }

    private void addSupplier(JTable table) { JTextField name = new JTextField(), contact = new JTextField(), phone = new JTextField(), email = new JTextField(), address = new JTextField(); JTextField[] fields = {name, contact, phone, email, address}; String[] labels = {"Name", "Contact person", "Phone", "Email", "Address"}; JPanel panel = new JPanel(new GridLayout(5, 2)); for (int i = 0; i < fields.length; i++) { panel.add(new JLabel(labels[i])); panel.add(fields[i]); } if (JOptionPane.showConfirmDialog(frame, panel, "Add supplier", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) { try { connect(); PreparedStatement statement = connection.prepareStatement("INSERT INTO suppliers(name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)"); for (int i = 0; i < fields.length; i++) statement.setString(i + 1, fields[i].getText()); statement.executeUpdate(); loadSuppliers(table); } catch (SQLException error) { showDatabaseError(error); } } }

    private void deleteSupplier(JTable table) { int row = table.getSelectedRow(); if (row < 0) return; try { connect(); PreparedStatement statement = connection.prepareStatement("DELETE FROM suppliers WHERE supplier_id = ?"); statement.setInt(1, (Integer) table.getValueAt(row, 0)); statement.executeUpdate(); loadSuppliers(table); } catch (SQLException error) { showDatabaseError(error); } }

    private void editSupplier(JTable table) { int row = table.getSelectedRow(); if (row < 0) return; JTextField name = new JTextField(table.getValueAt(row, 1).toString()), contact = new JTextField(table.getValueAt(row, 2).toString()), phone = new JTextField(table.getValueAt(row, 3).toString()), email = new JTextField(table.getValueAt(row, 4).toString()), address = new JTextField(table.getValueAt(row, 5).toString()); JTextField[] fields = {name, contact, phone, email, address}; String[] labels = {"Name", "Contact person", "Phone", "Email", "Address"}; JPanel panel = new JPanel(new GridLayout(5, 2)); for (int i = 0; i < fields.length; i++) { panel.add(new JLabel(labels[i])); panel.add(fields[i]); } if (JOptionPane.showConfirmDialog(frame, panel, "Edit supplier", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) { try { connect(); PreparedStatement statement = connection.prepareStatement("UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?"); for (int i = 0; i < fields.length; i++) statement.setString(i + 1, fields[i].getText()); statement.setInt(6, (Integer) table.getValueAt(row, 0)); statement.executeUpdate(); loadSuppliers(table); } catch (SQLException error) { showDatabaseError(error); } } }

    private class UserPanel extends JPanel {
        UserPanel() { setLayout(new BorderLayout()); JTable table = makeTable(new String[]{"ID", "Username", "Full name", "Role"}); JPanel buttons = new JPanel(); JButton refresh = new JButton("Refresh users"); JButton add = new JButton("Add cashier"); JButton edit = new JButton("Edit cashier"); JButton delete = new JButton("Delete selected"); buttons.add(refresh); buttons.add(add); buttons.add(edit); buttons.add(delete); add(buttons, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER); refresh.addActionListener(e -> loadUsers(table)); add.addActionListener(e -> addCashier(table)); edit.addActionListener(e -> editCashier(table)); delete.addActionListener(e -> deleteUser(table)); loadUsers(table); }
    }

    private void loadUsers(JTable table) { DefaultTableModel model = (DefaultTableModel) table.getModel(); model.setRowCount(0); try { connect(); ResultSet result = connection.createStatement().executeQuery("SELECT user_id, username, full_name, role FROM users"); while (result.next()) model.addRow(new Object[]{result.getInt(1), result.getString(2), result.getString(3), result.getString(4)}); } catch (SQLException error) { showDatabaseError(error); } }

    private void addCashier(JTable table) { JTextField username = new JTextField(), password = new JTextField(), fullName = new JTextField(); JTextField[] fields = {username, password, fullName}; String[] labels = {"Username", "Password", "Full name"}; JPanel panel = new JPanel(new GridLayout(3, 2)); for (int i = 0; i < fields.length; i++) { panel.add(new JLabel(labels[i])); panel.add(fields[i]); } if (JOptionPane.showConfirmDialog(frame, panel, "Add cashier", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) { try { connect(); PreparedStatement statement = connection.prepareStatement("INSERT INTO users(username, password, role, full_name) VALUES (?, ?, 'Cashier', ?)"); statement.setString(1, username.getText()); statement.setString(2, password.getText()); statement.setString(3, fullName.getText()); statement.executeUpdate(); loadUsers(table); } catch (SQLException error) { showDatabaseError(error); } } }

    private void deleteUser(JTable table) { int row = table.getSelectedRow(); if (row < 0) return; try { connect(); PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ? AND role = 'Cashier'"); statement.setInt(1, (Integer) table.getValueAt(row, 0)); statement.executeUpdate(); loadUsers(table); } catch (SQLException error) { showDatabaseError(error); } }

    private void editCashier(JTable table) { int row = table.getSelectedRow(); if (row < 0 || !table.getValueAt(row, 3).toString().equals("Cashier")) return; JTextField username = new JTextField(table.getValueAt(row, 1).toString()), password = new JTextField(), fullName = new JTextField(table.getValueAt(row, 2).toString()); JPanel panel = new JPanel(new GridLayout(3, 2)); panel.add(new JLabel("Username")); panel.add(username); panel.add(new JLabel("New password")); panel.add(password); panel.add(new JLabel("Full name")); panel.add(fullName); if (JOptionPane.showConfirmDialog(frame, panel, "Edit cashier", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) { try { connect(); PreparedStatement statement = connection.prepareStatement("UPDATE users SET username = ?, password = ?, full_name = ? WHERE user_id = ? AND role = 'Cashier'"); statement.setString(1, username.getText()); statement.setString(2, password.getText().isEmpty() ? "cash123" : password.getText()); statement.setString(3, fullName.getText()); statement.setInt(4, (Integer) table.getValueAt(row, 0)); statement.executeUpdate(); loadUsers(table); } catch (SQLException error) { showDatabaseError(error); } } }

    private class ReportPanel extends JPanel {
        ReportPanel() { setLayout(new GridLayout(2, 2, 8, 8)); add(reportButton("Sales report", "SELECT DATE(sale_date), COUNT(*), SUM(total_amount) FROM sales GROUP BY DATE(sale_date)")); add(reportButton("Item wise report", "SELECT m.name, SUM(i.quantity_sold) FROM sale_items i JOIN medicines m ON i.medicine_id = m.medicine_id GROUP BY m.name")); add(reportButton("Low stock report", "SELECT name, quantity_in_stock, reorder_level FROM medicines WHERE quantity_in_stock <= reorder_level")); add(reportButton("Expiry report", "SELECT name, expiry_date FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH)")); }
        private JButton reportButton(String title, String sql) { JButton button = new JButton(title); button.addActionListener(e -> showReport(title, sql)); return button; }
    }

    private void showReport(String title, String sql) { try { connect(); ResultSet result = connection.createStatement().executeQuery(sql); StringBuilder text = new StringBuilder(title + "\n\n"); ResultSetMetaData metadata = result.getMetaData(); while (result.next()) { for (int i = 1; i <= metadata.getColumnCount(); i++) text.append(result.getString(i)).append("    "); text.append("\n"); } JTextArea area = new JTextArea(text.toString()); area.setEditable(false); JOptionPane.showMessageDialog(frame, new JScrollPane(area), title, JOptionPane.INFORMATION_MESSAGE); } catch (SQLException error) { showDatabaseError(error); } }
}