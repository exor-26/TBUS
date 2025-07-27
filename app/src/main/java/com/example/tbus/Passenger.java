package com.example.tbus;

public class Passenger {
    private String name;
    private int age;
    private String gender;

    // No-argument constructor
    public Passenger() {
        // Default constructor required for Firestore deserialization
    }

    // Constructor with parameters
    public Passenger(String name, int age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }
}