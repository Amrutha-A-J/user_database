/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.saskenergyapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author amrut
 */
public class SaskEnergyApp extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel loginStatusMessage;
    private UserDatabaseApp userDatabaseApp;

    public SaskEnergyApp() {
        setTitle("SaskEnergy Login");
        createUIComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 200);
        this.setLayout(new GridLayout(4, 1));
        this.add(usernameField);
        this.add(passwordField);
        this.add(loginButton);
        this.add(loginStatusMessage);
    }

    private void createUIComponents() {
        usernameField = new JTextField("Username");
        passwordField = new JPasswordField("Password");
        loginButton = new JButton("Login");
        loginStatusMessage = new JLabel("", SwingConstants.CENTER);
        loginButton.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (username.equals("admin") && password.equals("password")) {
                loginStatusMessage.setText("Login Successful");
                this.dispose();
                userDatabaseApp = new UserDatabaseApp();
                userDatabaseApp.setVisible(true);
            } else {
                loginStatusMessage.setText("Login Failed");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SaskEnergyApp().setVisible(true);
        });
    }
}

class User {

    int id;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;

    // Constructor for existing users (with id)
    User(int id, String firstName, String lastName, String email, String phoneNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Constructor for new users (without id)
    User(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", First Name: " + firstName + ", Last Name: " + lastName + ", Email: " + email + ", Phone Number: " + phoneNumber;
    }
}

class UserDatabaseApp extends JFrame {

    JLabel searchLabel = new JLabel("Search by name/phone :");
    private JTextField searchField;
    private JTable userTable;
    private JCheckBox emailCheckBox;
    private JCheckBox phoneCheckBox;
    private Connection conn;
    private DefaultTableModel tableModel;

    public UserDatabaseApp() {
        setTitle("SaskEnergy User Databse");
        createUIComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 300);
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(emailCheckBox);
        topPanel.add(phoneCheckBox);
        topPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        JPanel bottomPanel = new JPanel();
        JButton newUserButton = new JButton("Add New User");
        newUserButton.addActionListener((ActionEvent e) -> {
            User newUser = showNewUserDialog();
            if (newUser != null) {
                // Add the new user to the database
                addUserToDatabase(newUser);
                // Refresh the table
                displayUsers();
            }
        });
        bottomPanel.add(newUserButton);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(new JScrollPane(userTable), BorderLayout.CENTER);
        connectToDatabase();
        displayUsers();
    }

    private void createUIComponents() {
        searchField = new JTextField(10);
        emailCheckBox = new JCheckBox("Email", true);
        phoneCheckBox = new JCheckBox("Phone Number", true);
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone Number", "Update", "Delete"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; // make only the "Update" and "Delete" columns editable
            }
        };
        userTable = new JTable(tableModel);
        userTable.removeColumn(userTable.getColumn("ID"));  // Hide the "ID" column
        userTable.getColumn("Update").setCellRenderer((TableCellRenderer) new ButtonRenderer());
        userTable.getColumn("Update").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Update"));
        userTable.getColumn("Delete").setCellRenderer((TableCellRenderer) new ButtonRenderer());
        userTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Delete"));

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                displayUsers();
            }
        });
        emailCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                userTable.removeColumn(userTable.getColumn("Email"));
            } else {
                userTable.addColumn(new TableColumn(1));
                userTable.moveColumn(userTable.getColumnCount() - 1, 1);
                userTable.getColumnModel().getColumn(1).setHeaderValue("Email");
            }
        });
        phoneCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                userTable.removeColumn(userTable.getColumn("Phone Number"));
            } else {
                userTable.addColumn(new TableColumn(2));
                userTable.moveColumn(userTable.getColumnCount() - 1, 2);
                userTable.getColumnModel().getColumn(2).setHeaderValue("Phone Number");
            }
        });
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/users", "root", "Abcd1234");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("DB connection failed with error: " + e.getMessage());
        }
    }

    private void displayUsers() {
        String search = searchField.getText();
        tableModel.setRowCount(0); // clear the table
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE FirstName LIKE ? OR LastName LIKE ? OR PhoneNumber LIKE ?");
            stmt.setString(1, "%" + search + "%");
            stmt.setString(2, "%" + search + "%");
            stmt.setString(3, "%" + search + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{rs.getInt("ID"), rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Email"), rs.getString("PhoneNumber"), "Update", "Delete"});
            }
        } catch (SQLException e) {
            System.out.println("Fetching user list from DB failed with error: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No more users to display.");
        }
    }

    private User showNewUserDialog() {
        User newUser = null;
        while (newUser == null) {
            JTextField firstNameField = new JTextField();
            JTextField lastNameField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField phoneNumberField = new JTextField();
            Object[] message = {
                "First Name:", firstNameField,
                "Last Name:", lastNameField,
                "Email:", emailField,
                "Phone Number:", phoneNumberField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "New User", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String email = emailField.getText();
                String phone = phoneNumberField.getText();

                if ("".equals(firstName) || "".equals(lastName) || "".equals(email) || "".equals(phone)) {
                    JOptionPane.showMessageDialog(null, "User data missing, enter complete user details!");
                } else {
                    newUser = new User(firstName, lastName, email, phone);
                }
            } else {
                break; // User clicked cancel or closed the dialog
            }
        }
        return newUser;
    }

    private void addUserToDatabase(User user) {
        try {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Users (FirstName, LastName, Email, PhoneNumber) VALUES (?, ?, ?, ?)");
            stmt.setString(1, user.firstName);
            stmt.setString(2, user.lastName);
            stmt.setString(3, user.email);
            stmt.setString(4, user.phoneNumber);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "New user Added successfully!");
        } catch (SQLException e) {
            System.out.println("Adding new user failed with error: " + e.getMessage());
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private int row;
        private final UserDatabaseApp app;

        public ButtonEditor(JCheckBox checkBox, UserDatabaseApp app, String action) {
            super(checkBox);
            this.app = app;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener((ActionEvent e) -> {
                if ("Update".equals(action)) {
                    // Open a dialog to edit the row data
                    User user = getUserFromRow(row);
                    User updatedUser = showUpdateDialog(user);
                    if (updatedUser != null) {
                        // Update the user in the database
                        updateUserInDatabase(updatedUser);
                        // Refresh the table
                        app.displayUsers();
                    }
                } else if ("Delete".equals(action)) {
                    // Open a dialog to confirm and delete the row data
                    User user = getUserFromRow(row);
                    int option = showDeleteDialog(user);
                    if (option == JOptionPane.YES_OPTION) {
                        // Delete the user from the database
                        deleteUserInDatabase(user);
                        app.displayUsers();
                        // Refresh the table

                    }
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;  // return the label of the button that was clicked
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            if (row >= 0 && row < tableModel.getRowCount()) {
                super.fireEditingStopped();
            }
        }

        private User getUserFromRow(int row) {
            int id = (int) tableModel.getValueAt(row, 0);
            String firstName = (String) tableModel.getValueAt(row, 1);
            String lastName = (String) tableModel.getValueAt(row, 2);
            String email = (String) tableModel.getValueAt(row, 3);
            String phoneNumber = (String) tableModel.getValueAt(row, 4);
            return new User(id, firstName, lastName, email, phoneNumber);
        }

        private User showUpdateDialog(User user) {
            JTextField firstNameField = new JTextField(user.firstName);
            JTextField lastNameField = new JTextField(user.lastName);
            JTextField emailField = new JTextField(user.email);
            JTextField phoneNumberField = new JTextField(user.phoneNumber);
            Object[] message = {
                "First Name:", firstNameField,
                "Last Name:", lastNameField,
                "Email:", emailField,
                "Phone Number:", phoneNumberField,};

            int option = JOptionPane.showConfirmDialog(null, message, "Update User", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                return new User(user.id, firstNameField.getText(), lastNameField.getText(), emailField.getText(), phoneNumberField.getText());
            } else {
                return null; // User clicked cancel or closed the dialog
            }
        }

        private void deleteUserInDatabase(User user) {
            try {
                PreparedStatement stmt = app.conn.prepareStatement("DELETE FROM Users WHERE ID = ?");
                stmt.setInt(1, user.id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "User deleted successfully!");
            } catch (SQLException e) {
                System.out.println("User data deletion failed with error: " + e.getMessage());
            }
        }

        private int showDeleteDialog(User user) {
            String message = "Are you sure you want to delete the user: " + user.firstName + " " + user.lastName + "?";
            int option = JOptionPane.showConfirmDialog(null, message, "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            return option;
        }

        private void updateUserInDatabase(User user) {
            try {
                PreparedStatement stmt = app.conn.prepareStatement("UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, PhoneNumber = ? WHERE ID = ?");
                stmt.setString(1, user.firstName);
                stmt.setString(2, user.lastName);
                stmt.setString(3, user.email);
                stmt.setString(4, user.phoneNumber);
                stmt.setInt(5, user.id);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "User updated successfully!");
            } catch (SQLException e) {
                System.out.println("User data updation failed with error: " + e.getMessage());
            }
        }
    }
}
