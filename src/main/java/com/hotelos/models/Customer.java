package com.hotelos.models;

import java.io.Serializable;

/**
 * Encapsulated Customer class to hold guest details.
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String contactNumber;
    private int daysStayed;

    public Customer(String name, String contactNumber, int daysStayed) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.daysStayed = daysStayed;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public int getDaysStayed() { return daysStayed; }
    public void setDaysStayed(int daysStayed) { this.daysStayed = daysStayed; }

    @Override
    public String toString() {
        return "Customer{" + "name='" + name + '\'' + ", contact=" + contactNumber + "}";
    }
}
