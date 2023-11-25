/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.saskenergyapp;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author amrut
 */
public class UserDatabaseApp extends JFrame {

    private final JLabel searchLabel = new JLabel("Search by name/phone :");
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
        this.setSize(900, 600);
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
                addUserToDatabase(newUser);
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
                return column == 5 || column == 6;
            }
        };
        userTable = new JTable(tableModel);
        userTable.removeColumn(userTable.getColumn("ID"));
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
        TableColumn emailColumn = userTable.getColumn("Email");
        TableColumn phoneColumn = userTable.getColumn("Phone Number");

        emailCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                userTable.removeColumn(userTable.getColumn("Email"));
            } else {
                // if phone colum is present, add email back before that. Else, add it before the index of the update button
                int insertIndex = phoneCheckBox.isSelected()
                        ? userTable.getColumnModel().getColumnIndex("Phone Number")
                        : userTable.getColumnModel().getColumnIndex("Update");
                userTable.addColumn(emailColumn);
                userTable.moveColumn(userTable.getColumnCount() - 1, insertIndex);
            }
        });

        phoneCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                userTable.removeColumn(userTable.getColumn("Phone Number"));
            } else {
                // always readd phone column right before the index of update button
                int updateIndex = userTable.getColumnModel().getColumnIndex("Update");
                userTable.addColumn(phoneColumn);
                userTable.moveColumn(userTable.getColumnCount() - 1, updateIndex);
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
        tableModel.setRowCount(0);
        try {
            String query = "SELECT * FROM Users WHERE FirstName LIKE ? OR LastName LIKE ? OR PhoneNumber LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
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
                break;
            }
        }
        return newUser;
    }

    private void addUserToDatabase(User user) {
        try {
            String query = "INSERT INTO Users (FirstName, LastName, Email, PhoneNumber) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
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
                    User user = getUserFromRow(row);
                    User updatedUser = showUpdateDialog(user);
                    if (updatedUser != null) {
                        updateUserInDatabase(updatedUser);
                        app.displayUsers();
                    }
                } else if ("Delete".equals(action)) {
                    User user = getUserFromRow(row);
                    int option = showDeleteDialog(user);
                    if (option == JOptionPane.YES_OPTION) {
                        deleteUserInDatabase(user);
                        app.displayUsers();

                    }
                }
                if (row >= 0 && row < tableModel.getRowCount()) {
                    super.fireEditingStopped();
                }
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
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
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
                return null;
            }
        }

        private void deleteUserInDatabase(User user) {
            try {
                String query = "DELETE FROM Users WHERE ID = ?";
                PreparedStatement stmt = app.conn.prepareStatement(query);
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
                String query = "UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, PhoneNumber = ? WHERE ID = ?";
                PreparedStatement stmt = app.conn.prepareStatement(query);
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
