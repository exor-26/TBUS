package com.example.tbus;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryContainer extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historycontainer);

        db = FirebaseFirestore.getInstance();

        // Fetch the PNR passed via the Intent
        Intent intent = getIntent();
        String targetPNR = intent.getStringExtra("pnr");

        if (targetPNR != null && !targetPNR.isEmpty()) {
            fetchAndDisplayBookingDetails(targetPNR);
        } else {
            Toast.makeText(this, "PNR not provided.", Toast.LENGTH_LONG).show();
            finish(); // Exit activity if PNR is missing
        }

        // Set up the Cancel button and its click listener
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            new AlertDialog.Builder(HistoryContainer.this)
                    .setTitle("Ticket Cancellation")
                    .setMessage("Are you sure you want to cancel your ticket? Please read the terms and conditions carefully before proceeding.")
                    .setPositiveButton("Yes", (dialog, which) -> cancelTicket(targetPNR))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void fetchAndDisplayBookingDetails(String targetPNR) {
        db.collection("bookings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isDataFound = false;

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Map<String, Object> data = document.getData();
                        if (data != null && data.get("PNR") != null && data.get("PNR").equals(targetPNR)) {
                            displayBookingDetails(data);
                            isDataFound = true;
                            break;
                        }
                    }

                    if (!isDataFound) {
                        Toast.makeText(this, "No booking found for PNR: " + targetPNR, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void displayBookingDetails(Map<String, Object> data) {
        // Update static TextViews with booking details
        ((TextView) findViewById(R.id.transactionIDTextView)).setText("Transaction ID: " + data.get("transactionID"));
        ((TextView) findViewById(R.id.pnrTextView)).setText("PNR: " + data.get("PNR"));
        ((TextView) findViewById(R.id.phoneTextView)).setText("Phone: " + data.get("phone"));
        ((TextView) findViewById(R.id.emailTextView)).setText("Email: " + data.get("email"));
        ((TextView) findViewById(R.id.busNameTextView)).setText("Bus Name: " + data.get("busName"));
        ((TextView) findViewById(R.id.dateTextView)).setText("Date: " + data.get("date"));
        ((TextView) findViewById(R.id.arrivalTimeTextView)).setText("Arrival Time: " + data.get("arrivalTime"));
        ((TextView) findViewById(R.id.departureTimeTextView)).setText("Departure Time: " + data.get("departureTime"));
        ((TextView) findViewById(R.id.routeFromTextView)).setText("Route From: " + data.get("routeFrom"));
        ((TextView) findViewById(R.id.routeToTextView)).setText("Route To: " + data.get("routeTo"));
        ((TextView) findViewById(R.id.boardingPointTextView)).setText("Boarding Point: " + data.get("boardingPoint"));
        ((TextView) findViewById(R.id.selectedSeatsTextView)).setText("Selected Seats: " + data.get("selectedSeats"));
        ((TextView) findViewById(R.id.totalFareTextView)).setText("Total Fare: ₹ " + data.get("totalFare"));

        // Dynamically add passenger details to passengerContainer
        LinearLayout passengerContainer = findViewById(R.id.passengerContainer);
        passengerContainer.removeAllViews(); // Clear any existing views

        List<Map<String, Object>> passengers = (List<Map<String, Object>>) data.get("passengers");
        if (passengers != null) {
            for (Map<String, Object> passenger : passengers) {
                String passengerDetails = "Name: " + passenger.get("name") +
                        ", Age: " + passenger.get("age") +
                        ", Gender: " + passenger.get("gender");

                TextView passengerTextView = new TextView(this);
                passengerTextView.setText(passengerDetails);
                passengerTextView.setTextSize(16); // Adjust text size as needed
                passengerTextView.setTextColor(Color.parseColor("#27A8BA"));
                passengerTextView.setPadding(0, 8, 0, 8); // Add some padding
                passengerContainer.addView(passengerTextView);
            }
        }
    }

    // Cancellation logic
    private void cancelTicket(String pnr) {
        db.collection("bookings")
                .whereEqualTo("PNR", pnr)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        Map<String, Object> bookingData = documentSnapshot.getData();

                        if (bookingData != null) {
                            // Prepare cancellation data
                            Map<String, Object> cancellationData = new HashMap<>();
                            cancellationData.put("PNR", bookingData.get("PNR"));
                            cancellationData.put("passengers", bookingData.get("passengers"));
                            cancellationData.put("selectedSeats", bookingData.get("selectedSeats"));
                            cancellationData.put("date", bookingData.get("date"));
                            cancellationData.put("email", bookingData.get("email"));

                            // Store the cancellation data in Firestore
                            db.collection("cancellations")
                                    .add(cancellationData)
                                    .addOnSuccessListener(aVoid -> {
                                        // After storing in "cancellations" collection, delete selectedSeats from "bookings"
                                        db.collection("bookings")
                                                .document(documentSnapshot.getId()) // Get the document ID
                                                .update("selectedSeats", FieldValue.delete()) // Remove selectedSeats
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(HistoryContainer.this, "Ticket cancelled successfully. Selected seats removed.", Toast.LENGTH_LONG).show();
                                                    finish(); // Close the activity after cancellation
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(HistoryContainer.this, "Failed to remove selected seats: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(HistoryContainer.this, "Cancellation failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HistoryContainer.this, "Error retrieving booking data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}