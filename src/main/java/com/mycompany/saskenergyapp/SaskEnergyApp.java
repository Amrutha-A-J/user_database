/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.saskenergyapp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private JTextField searchField;
    private JTable userTable;
    private JButton searchButton;
    private JCheckBox emailCheckBox;
    private JCheckBox phoneCheckBox;
    private Connection conn;
    private DefaultTableModel tableModel;

    public UserDatabaseApp() {
        createUIComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 300);
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.add(searchField);
        topPanel.add(emailCheckBox);
        topPanel.add(phoneCheckBox);
        topPanel.add(searchButton);
        JButton newUserButton = new JButton("New User");
        newUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                User newUser = showNewUserDialog();
                if (newUser != null) {
                    // Add the new user to the database
                    addUserToDatabase(newUser);
                    // Refresh the table
                    displayUsers();
                }
            }
        });
        topPanel.add(newUserButton);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(new JScrollPane(userTable), BorderLayout.CENTER);
        connectToDatabase();
        displayUsers();
    }

    private void createUIComponents() {
        searchField = new JTextField(10);
        emailCheckBox = new JCheckBox("Email", true);
        phoneCheckBox = new JCheckBox("Phone Number", true);
        searchButton = new JButton("Search");
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone Number", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // make only the "Actions" column editable
            }
        };
        userTable = new JTable(tableModel);
        userTable.removeColumn(userTable.getColumn("ID"));  // Hide the "ID" column
        userTable.getColumn("Actions").setCellRenderer((TableCellRenderer) new ButtonRenderer());
        userTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox(), this));

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
        } catch (Exception e) {
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
                tableModel.addRow(new Object[]{rs.getInt("ID"), rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Email"), rs.getString("PhoneNumber"), "Update/Delete"});
            }
        } catch (SQLException e) {
            System.out.println("Fetching user list from DB failed with error: " + e.getMessage());
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

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private boolean isPushed;
        private int row;
        private JTable table;
        private UserDatabaseApp app;

        public ButtonEditor(JCheckBox checkBox, UserDatabaseApp app) {
            super(checkBox);
            this.app = app;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if ("Update/Delete".equals(label)) {
                        // Open a dialog to edit the row data
                        User user = getUserFromRow(row);
                        User updatedUser = showUpdateDialog(user);
                        if (updatedUser != null) {
                            // Update the user in the database
                            updateUserInDatabase(updatedUser);
                            // Refresh the table
                            app.displayUsers();
                        }
                    }
                    fireEditingStopped();
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // The button has been clicked
                if ("Update/Delete".equals(label)) {
                    // The "Update/Delete" button was clicked
                    User user = getUserFromRow(row);
                    User updatedUser = showUpdateDialog(user);
                    if (updatedUser != null) {
                        // The user made changes in the dialog
                        updateUserInDatabase(updatedUser);
                        // Refresh the table
                        ((UserDatabaseApp) table.getTopLevelAncestor()).displayUsers();
                    }
                }
            }
            isPushed = false;
            return label;  // return the label of the button that was clicked
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
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
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteUserInDatabase(user);
                    ((UserDatabaseApp) table.getTopLevelAncestor()).displayUsers();
                }
            });
            Object[] message = {
                "First Name:", firstNameField,
                "Last Name:", lastNameField,
                "Email:", emailField,
                "Phone Number:", phoneNumberField,
                deleteButton
            };

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
