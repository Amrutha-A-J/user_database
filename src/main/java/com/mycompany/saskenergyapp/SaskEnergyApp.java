/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.saskenergyapp;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

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
