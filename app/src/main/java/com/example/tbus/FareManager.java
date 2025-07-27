package com.example.tbus;

public class FareManager {

    private double pricePerSeat;

    // Constructor to initialize the price per seat
    public FareManager(double pricePerSeat) {
        this.pricePerSeat = pricePerSeat;
    }

    // Method to calculate total fare based on selected seats
    public double calculateTotalFare(int selectedSeats) {
        return pricePerSeat * selectedSeats;
    }

    // Method to get fare string (e.g., "1 Fare: ₹100")
    public String getFareText(int selectedSeats) {
        double totalFare = calculateTotalFare(selectedSeats);
        return selectedSeats + " Fare: ₹" + totalFare;
    }
}