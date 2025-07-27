package com.example.tbus;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchRoutesActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView recyclerViewRoutes;
    RouteAdapter routeAdapter;
    List<Route> routeList; // List to hold route data
    ProgressBar loader;
    TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_route); // layout with RecyclerView

        // Initialize Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance("https://tbus-@code-default-rtdb.firebaseio.com");
        databaseReference = firebaseDatabase.getReference("buses"); // Reference to buses node

        // Find the RecyclerView in the layout
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes);
        recyclerViewRoutes.setLayoutManager(new LinearLayoutManager(this)); // Set LayoutManager
        messageTextView = findViewById(R.id.messageTextView);

        // Initialize the list and adapter
        routeList = new ArrayList<>();
        routeAdapter = new RouteAdapter(routeList);
        recyclerViewRoutes.setAdapter(routeAdapter);
        loader = findViewById(R.id.loader);

        // Get the data passed from MainActivity (for filtering)
        String from = getIntent().getStringExtra("fromField");
        String to = getIntent().getStringExtra("toField");
        String selectedDate = getIntent().getStringExtra("selectedDate");

        if (isNetworkAvailable()) {
            // Show loader while fetching data
            loader.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);

// Call searchRoutes method directly to populate the RecyclerView with data
        searchRoutes(from, to, selectedDate);
    } else {
            // No internet connection, show a Toast and hide the loader
            loader.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText("No internet connection. Please check your network.");        }
    }

    // Method to check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Method to search for routes in Firebase based on from, to, and date
    private void searchRoutes(String from, String to, String selectedDate) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routeList.clear(); // Clear previous results
                loader.setVisibility(View.GONE);
                Log.d("SearchRoutes", "Entire buses snapshot: " + snapshot.getValue());
                Log.d("SearchRoutes", "Input Criteria - From: " + from + ", To: " + to + ", Date: " + selectedDate);

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat scheduleDateFormat = new SimpleDateFormat("dd");

                try {
                    Date parsedInputDate = inputDateFormat.parse(selectedDate);
                    String formattedInputDate = scheduleDateFormat.format(parsedInputDate);

                    Log.d("SearchRoutes", "Formatted Input Month-Day: " + formattedInputDate);

                    // Check all route categories like "routes", "routes1", "routes2", etc.
                    for (DataSnapshot routeCategorySnapshot : snapshot.getChildren()) {
                        // Extract "from", "to", and "schedule" fields dynamically
                        String routeFrom = routeCategorySnapshot.child("from").getValue(String.class);
                        String routeTo = routeCategorySnapshot.child("to").getValue(String.class);
                        DataSnapshot scheduleSnapshot = routeCategorySnapshot.child("schedule");

                        Log.d("SearchRoutes", "Extracted Route - From: " + routeFrom + ", To: " + routeTo);

                        // Validate "from" and "to" criteria
                        if (routeFrom != null && routeTo != null &&
                                routeFrom.trim().equalsIgnoreCase(from.trim()) &&
                                routeTo.trim().equalsIgnoreCase(to.trim())) {

                            Log.d("SearchRoutes", "Route Match Found - From: " + routeFrom + ", To: " + routeTo);

                            // Extract final station
                            String finalStation = routeCategorySnapshot.child("finalStation").getValue(String.class);

                            // Check schedule for the input date
                            if (scheduleSnapshot.exists() && scheduleSnapshot.hasChildren()) {
                                boolean dateMatched = false;

                                for (DataSnapshot dateSnapshot : scheduleSnapshot.getChildren()) {
                                    String snapshotDate = dateSnapshot.getKey();
                                    Log.d("SearchRoutes", "Comparing Schedule - Input Date: " + formattedInputDate + ", Snapshot Date: " + snapshotDate);

                                    if (snapshotDate != null && snapshotDate.trim().equals(formattedInputDate.trim())) {
                                        dateMatched = true;

                                        for (DataSnapshot busSnapshot : dateSnapshot.getChildren()) {
                                            String busName = busSnapshot.child("busName").getValue(String.class);
                                            String price = busSnapshot.child("price").getValue(String.class);

                                            // Extract boarding points
                                            DataSnapshot boardingPointsSnapshot = busSnapshot.child("boardingPoints");
                                            HashMap<String, String> boardingPoints = new HashMap<>();
                                            if (boardingPointsSnapshot.exists()) {
                                                for (DataSnapshot boardingPoint : boardingPointsSnapshot.getChildren()) {
                                                    boardingPoints.put(boardingPoint.getKey(), boardingPoint.getValue(String.class));
                                                }
                                                Log.d("SearchRoutes", "Boarding Points for Bus: " + busName + " -> " + boardingPoints);

                                            }

                                            // Extract times for each schedule
                                            DataSnapshot timeSnapshot = busSnapshot.child("time");

                                            if (timeSnapshot.exists() && timeSnapshot.hasChildren()) {
                                                // Loop through the time children to get each departure and arrival time
                                                for (DataSnapshot timeNode : timeSnapshot.getChildren()) {
                                                    String departureTime = timeNode.child("departureTime").getValue(String.class);
                                                    String arrivalTime = timeNode.child("arrivalTime").getValue(String.class);

                                                    // Handle missing values for time fields
                                                    if (departureTime == null) departureTime = "Not Available";
                                                    if (arrivalTime == null) arrivalTime = "Not Available";
                                                    if (price == null) price = "Not Available";

                                                    Log.d("SearchRoutes", "Matched Bus: " + busName + ", Departure: " + departureTime + ", Arrival: " + arrivalTime + ", Price: " + price);

                                                    // Create a Route object for each time entry
                                                    Route route = new Route(busName, departureTime, arrivalTime, routeFrom, routeTo, selectedDate, price, boardingPoints, finalStation);
                                                    routeList.add(route);
                                                }
                                            } else {
                                                Log.d("SearchRoutes", "No valid time entries for Bus: " + busName);
                                            }
                                        }
                                    }
                                }

                                if (!dateMatched) {
                                    // No schedule found, hide the RecyclerView and show the message
                                    messageTextView.setVisibility(View.VISIBLE);
                                    messageTextView.setText("No routes found for the selected date.");                                }
                            } else {
                                // No valid schedule entries
                                messageTextView.setVisibility(View.VISIBLE);
                                messageTextView.setText("No routes available.");                            }
                        } else {
                            Log.d("SearchRoutes", "Route Mismatch - From: " + routeFrom + ", To: " + routeTo);
                        }
                    }

                    // Notify the adapter about the updated data
                    routeAdapter.notifyDataSetChanged();

                    if (routeList.isEmpty()) {
                        messageTextView.setVisibility(View.VISIBLE);
                        messageTextView.setText("No routes match the criteria.");
                    }

                } catch (Exception e) {
                    Log.e("SearchRoutes", "Date Format Error: " + e.getMessage());
                }
                loader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchRoutesActivity.this, "Failed to read data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SearchRoutes", "Database Error: " + error.getMessage());
                loader.setVisibility(View.GONE);
            }
        });
    }
}
