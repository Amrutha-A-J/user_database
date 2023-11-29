/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.saskenergyapp;

/**
 *
 * @author amrut
 */
public class User extends Person {
    int id;
    String email;
    String phoneNumber;

    // Constructor for existing users (with id)
    User(int id, String firstName, String lastName, String email, String phoneNumber) {
        super(firstName, lastName);
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Constructor for new users (without id)
    User(String firstName, String lastName, String email, String phoneNumber) {
        super(firstName, lastName);
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getContactInfo() {
        return "Email: " + email + ", Phone Number: " + phoneNumber;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", First Name: " + firstName + ", Last Name: " + lastName + ", " + getContactInfo();
    }
}
