package com.example.tbus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatActivity extends AppCompatActivity {

    private double busPrice = 0.0;
    private FareManager fareManager;
    private String busName;
    private String date;
    private String arrivalTime;
    private String departureTime;
    private String routeFrom;
    private String routeTo;
    private String selectedDate;
    private String selectedRouteFrom;
    private String selectedTime;
    private FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        db = FirebaseFirestore.getInstance();
        // Retrieve the route object
        Route route = (Route) getIntent().getSerializableExtra("routeDetails");

        if (route != null) {
            busName = route.getBusName();
            date = route.getDate();
            arrivalTime = route.getArrivalTime();
            departureTime = route.getDepartureTime();
            routeFrom = route.getRouteFrom();
            routeTo = route.getRouteTo();
            selectedDate = route.getDate();
            selectedRouteFrom = route.getRouteFrom();
            selectedTime = route.getArrivalTime();

            String price = route.getPrice();  // Example: "₹129"

            // Remove the currency symbol (₹) and parse the number
            String priceWithoutCurrency = price.replaceAll("₹", "").trim();
            try {
                busPrice = Double.parseDouble(priceWithoutCurrency);  // Convert to a numeric value
            } catch (NumberFormatException e) {
                Log.e("SeatActivity", "Error parsing price: " + e.getMessage());
            }

            // Initialize the FareManager with busPrice
            fareManager = new FareManager(busPrice);

            loadReservedSeats(selectedDate, selectedRouteFrom, selectedTime);

            Button nextButton = findViewById(R.id.next_button);
            nextButton.setOnClickListener(v -> {
                if (isSeatSelected()) { // Ensure at least one seat is selected
                    showBoardingPointSelection(route.getBoardingPoints());
                } else {
                    Toast.makeText(SeatActivity.this, "Please select at least one seat", Toast.LENGTH_SHORT).show();
                }
            });

            // Initialize seat selection (replace these IDs with actual seat IDs from your XML)
            String[] seatIds = {
                    "seat_01", "seat_02", "seat_03", "seat_04",
                    "seat_05", "seat_06", "seat_07", "seat_08",
                    "seat_09", "seat_10", "seat_11", "seat_12",
                    "seat_13", "seat_14", "seat_15", "seat_16",
                    "seat_17", "seat_18", "seat_19", "seat_20",
                    "seat_21", "seat_22", "seat_23", "seat_24",
                    "seat_25", "seat_26", "seat_27", "seat_28",
                    "seat_29", "seat_30", "seat_31", "seat_32",
                    "seat_33", "seat_34", "seat_35", "seat_36",
                    "seat_37", "seat_38", "seat_39", "seat_40",
                    "seat_41", "seat_42", "seat_43", "seat_44"
            };

            // Initialize fare and price text views
            TextView priceText = findViewById(R.id.price_text);
            TextView fareText = findViewById(R.id.fare_text);

            // Set initial values (both should show 0 initially)
            if (priceText != null) {
                priceText.setText("₹0");  // Set price to ₹0 initially
            }
            if (fareText != null) {
                fareText.setText("1 Fare:");  // Set initial fare text without the price
            }

            // Loop through each seat and set click listeners
            for (String seatId : seatIds) {
                int resId = getResources().getIdentifier(seatId, "id", getPackageName());
                Log.d("SeatActivity", "Trying to get seat with ID: " + seatId + ", resource ID: " + resId);
                TextView seat = findViewById(resId);

                if (seat != null) {
                    // Initialize seat selection state
                    seat.setTag(false); // false means not selected

                    seat.setOnClickListener(v -> {
                        boolean isSelected = (boolean) seat.getTag();
                        if (isSelected) {
                            // Deselect the seat
                            seat.setBackgroundResource(R.drawable.seat_background); // Replace with default drawable
                            seat.setTag(false);
                            Toast.makeText(SeatActivity.this, "Seat " + seat.getText() + " deselected", Toast.LENGTH_SHORT).show();
                        } else {
                            int selectedSeatCount = getSelectedSeats().size();
                            if (selectedSeatCount >= 4) {
                                new AlertDialog.Builder(SeatActivity.this)
                                        .setTitle("Seat Selection Limit")
                                        .setMessage("You can select up to 4 seats only.")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                                return;
                            }
                            // Select the seat
                            seat.setBackgroundResource(R.drawable.seat_select); // Replace with selected drawable
                            seat.setTag(true);
                            Toast.makeText(SeatActivity.this, "Seat " + seat.getText() + " selected", Toast.LENGTH_SHORT).show();
                        }

                        // Update the fare and price values
                        updateFareAndPrice();
                    });
                } else {
                    Log.w("SeatActivity", "Seat ID " + seatId + " not found in layout.");
                }
            }
        }
    }

    private void loadReservedSeats(String selectedDate, String selectedRouteFrom, String selectedTime) {
        // Get the arrival time passed from the previous activity
        String arrivalTime = getIntent().getStringExtra("arrivalTime");
        Log.d("SeatActivity", "Loading reserved seats for date: " + selectedDate
                + ", route from: " + selectedRouteFrom + " and arrival time: " + arrivalTime);

        // Query Firestore for all bookings on the selected date, route, and arrival time
        db.collection("bookings")
                .whereEqualTo("busName", busName) // Bus name should match
                .whereEqualTo("date", selectedDate) // Date should match
                .whereEqualTo("routeFrom", selectedRouteFrom) // Route should match
                .whereEqualTo("arrivalTime", selectedTime) // Arrival time should match
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve reserved seats from Firestore
                            List<String> bookedSeats = (List<String>) document.get("selectedSeats");

                            if (bookedSeats != null) {
                                for (String seatId : bookedSeats) {
                                    // Add seat_ prefix to match the XML ID format (e.g., seat_01)
                                    String seatIdWithPrefix = "seat_" + seatId;

                                    // Get seat from layout by its ID
                                    int resId = getResources().getIdentifier(seatIdWithPrefix, "id", getPackageName());
                                    TextView seat = findViewById(resId);

                                    if (seat != null) {
                                        // Mark seat as reserved in the UI
                                        seat.setBackgroundResource(R.drawable.seat_reserved);
                                        seat.setTag(false); // Seat is not available for selection
                                        // Make seat non-clickable
                                        seat.setClickable(false);  // Prevent interaction
                                        seat.setEnabled(false);    // Optionally, also disable the seat
                                        runOnUiThread(() -> {
                                            if (seat != null) {
                                                // Mark seat as reserved in the UI
                                                seat.setBackgroundResource(R.drawable.seat_reserved);
                                                seat.setTag(false); // Seat is not available for selection
                                                Log.d("SeatActivity", "Marked seat " + seatIdWithPrefix + " as reserved");
                                            }
                                        });
                                        Log.d("SeatActivity", "Marked seat " + seatIdWithPrefix + " as reserved");
                                    }
                                }
                            }
                        }
                    } else {
                        // Handle Firestore query failure
                        Log.e("SeatActivity", "Error loading reserved seats: " + task.getException().getMessage());
                        Toast.makeText(SeatActivity.this, "Error loading reserved seats: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showBoardingPointSelection(HashMap<String, String> boardingPoints) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SeatActivity.this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // References to boarding and drop-off point containers
        RadioGroup boardingPointGroup = bottomSheetView.findViewById(R.id.boarding_point_group);
        RadioGroup dropOffPointGroup = bottomSheetView.findViewById(R.id.drop_off_point_group);

        // Get the first boarding point (default)
        Map.Entry<String, String> firstEntry = boardingPoints.entrySet().iterator().next();
        String defaultBoardingKey = firstEntry.getKey();
        String defaultBoardingValue = firstEntry.getValue();

        // Add the default boarding point to the boarding container
        RadioButton defaultRadioButton = new RadioButton(this);
        defaultRadioButton.setId(View.generateViewId());
        defaultRadioButton.setText(defaultBoardingValue);
        defaultRadioButton.setTag(defaultBoardingKey);
        defaultRadioButton.setChecked(true); // Pre-select the first radio button
        boardingPointGroup.addView(defaultRadioButton);

        // Add remaining drop-off points to the drop-off container
        for (HashMap.Entry<String, String> entry : boardingPoints.entrySet()) {
            if (!entry.getKey().equals(defaultBoardingKey)) {
                RadioButton dropOffRadioButton = new RadioButton(this);
                dropOffRadioButton.setId(View.generateViewId());
                dropOffRadioButton.setText(entry.getValue());
                dropOffRadioButton.setTag(entry.getKey());
                dropOffPointGroup.addView(dropOffRadioButton);
            }
        }

        // Confirm button handling
        Button confirmButton = bottomSheetView.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            // Get selected drop-off point (mandatory selection)
            int dropOffSelectedId = dropOffPointGroup.getCheckedRadioButtonId();
            if (dropOffSelectedId != -1) {
                RadioButton selectedDropOffButton = bottomSheetView.findViewById(dropOffSelectedId);
                String selectedDropOffPointName = selectedDropOffButton.getText().toString();

                Toast.makeText(SeatActivity.this, "Selected Drop-Off Point: " + selectedDropOffPointName, Toast.LENGTH_SHORT).show();

                // Retrieve selected seats and calculate total fare
                ArrayList<String> selectedSeats = getSelectedSeats();
                double totalFare = fareManager.calculateTotalFare(selectedSeats.size());

                // Bundle data and pass to PaymentActivity
                Intent intent = new Intent(SeatActivity.this, PaymentActivity.class);
                intent.putStringArrayListExtra("selectedSeats", selectedSeats); // Passing selected seats
                intent.putExtra("selectedBoardingPoint", defaultBoardingValue + " -> " + selectedDropOffPointName); // Combine default and user-selected points
                intent.putExtra("totalFare", totalFare); // Passing total fare

                intent.putExtra("busName", busName);
                intent.putExtra("date", date);
                intent.putExtra("arrivalTime", arrivalTime);
                intent.putExtra("departureTime", departureTime);
                intent.putExtra("routeFrom", routeFrom);
                intent.putExtra("routeTo", routeTo);

                startActivity(intent);

                bottomSheetDialog.dismiss();
            } else {
                Toast.makeText(SeatActivity.this, "Please select a drop-off point", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private boolean isSeatSelected() {
        String[] seatIds = {
                "seat_01", "seat_02", "seat_03", "seat_04",
                "seat_05", "seat_06", "seat_07", "seat_08",
                "seat_09", "seat_10", "seat_11", "seat_12",
                "seat_13", "seat_14", "seat_15", "seat_16",
                "seat_17", "seat_18", "seat_19", "seat_20",
                "seat_21", "seat_22", "seat_23", "seat_24",
                "seat_25", "seat_26", "seat_27", "seat_28",
                "seat_29", "seat_30", "seat_31", "seat_32",
                "seat_33", "seat_34", "seat_35", "seat_36",
                "seat_37", "seat_38", "seat_39", "seat_40",
                "seat_41", "seat_42", "seat_43", "seat_44"
        };

        for (String seatId : seatIds) {
            int resId = getResources().getIdentifier(seatId, "id", getPackageName());
            TextView seat = findViewById(resId);
            if (seat != null && (boolean) seat.getTag()) {
                return true; // At least one seat is selected
            }
        }
        return false; // No seats selected
    }

    private void updateFareAndPrice() {
        int selectedSeats = 0;

        // Count the number of selected seats
        String[] seatIds = {
                "seat_01", "seat_02", "seat_03", "seat_04",
                "seat_05", "seat_06", "seat_07", "seat_08",
                "seat_09", "seat_10", "seat_11", "seat_12",
                "seat_13", "seat_14", "seat_15", "seat_16",
                "seat_17", "seat_18", "seat_19", "seat_20",
                "seat_21", "seat_22", "seat_23", "seat_24",
                "seat_25", "seat_26", "seat_27", "seat_28",
                "seat_29", "seat_30", "seat_31", "seat_32",
                "seat_33", "seat_34", "seat_35", "seat_36",
                "seat_37", "seat_38", "seat_39", "seat_40",
                "seat_41", "seat_42", "seat_43", "seat_44"
        };

        for (String seatId : seatIds) {
            int resId = getResources().getIdentifier(seatId, "id", getPackageName());
            TextView seat = findViewById(resId);
            if (seat != null && (boolean) seat.getTag()) {
                selectedSeats++;
            }
        }

        // Log the selected seats count
        Log.d("SeatActivity", "Selected seats: " + selectedSeats);

        // Update Fare Text (show number of selected seats, e.g., "1 Fare:", "2 Fare:", etc.)
        TextView fareText = findViewById(R.id.fare_text);
        if (fareText != null) {
            fareText.setText(selectedSeats + " Fare:");  // Correctly display the number of seats
        }

        // Update Price Text (show total fare with ₹ symbol)
        TextView priceText = findViewById(R.id.price_text);
        if (priceText != null) {
            double totalFare = busPrice * selectedSeats;  // Calculate total fare
            Log.d("SeatActivity", "Total fare: ₹" + totalFare);
            priceText.setText("₹" + totalFare);  // Display total fare with ₹ symbol
        }
    }

    // Method to retrieve selected seats
    private ArrayList<String> getSelectedSeats() {
        ArrayList<String> selectedSeats = new ArrayList<>();
        String[] seatIds = {
                "seat_01", "seat_02", "seat_03", "seat_04",
                "seat_05", "seat_06", "seat_07", "seat_08",
                "seat_09", "seat_10", "seat_11", "seat_12",
                "seat_13", "seat_14", "seat_15", "seat_16",
                "seat_17", "seat_18", "seat_19", "seat_20",
                "seat_21", "seat_22", "seat_23", "seat_24",
                "seat_25", "seat_26", "seat_27", "seat_28",
                "seat_29", "seat_30", "seat_31", "seat_32",
                "seat_33", "seat_34", "seat_35", "seat_36",
                "seat_37", "seat_38", "seat_39", "seat_40",
                "seat_41", "seat_42", "seat_43", "seat_44"
        };

        for (String seatId : seatIds) {
            int resId = getResources().getIdentifier(seatId, "id", getPackageName());
            TextView seat = findViewById(resId);
            if (seat != null && (boolean) seat.getTag()) {
                selectedSeats.add(seat.getText().toString());
            }
        }
        return selectedSeats;
    }
}