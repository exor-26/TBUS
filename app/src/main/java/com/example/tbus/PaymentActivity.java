package com.example.tbus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;
import java.security.SecureRandom;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private String selectedBoardingPoint, busName, date, arrivalTime, departureTime, routeFrom, routeTo;
    private double totalFare;
    private ArrayList<String> selectedSeats;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private ImageButton btnAdd, btnRemove;
    private int currentPassengerIndex = -1;
    private List<Passenger> passengerList;
    private EditText etName, etAge, etPhone, etEmail;
    private FirebaseFirestore db; // Firestore instance
    private TextView routeText, dateText, routeNameText, departureTimeText, arrivalTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        routeText = findViewById(R.id.routeText);
        dateText = findViewById(R.id.date);
        routeNameText = findViewById(R.id.routeName);
        departureTimeText = findViewById(R.id.departureTime);
        arrivalTimeText = findViewById(R.id.arrivalTime);

        // Initialize fields
        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        rbMale = findViewById(R.id.rb_male);
        rgGender = findViewById(R.id.rg_gender);
        rbFemale = findViewById(R.id.rb_female);
        btnAdd = findViewById(R.id.btn_add);
        btnRemove = findViewById(R.id.btn_remove);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // Show a popup dialog prompting the user to log in
            showLoginPopup();
            return; // Stop further execution
        }

        // Set the email field to the user's email and make it non-editable
        etEmail.setText(user.getEmail());
        etEmail.setFocusable(false);
        etEmail.setClickable(false);

        passengerList = new ArrayList<>();
        selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetching data from the previous activity
        selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
        selectedBoardingPoint = getIntent().getStringExtra("selectedBoardingPoint");
        totalFare = getIntent().getDoubleExtra("totalFare", 0.0);
        busName = getIntent().getStringExtra("busName");
        date = getIntent().getStringExtra("date");
        arrivalTime = getIntent().getStringExtra("arrivalTime");
        departureTime = getIntent().getStringExtra("departureTime");
        routeFrom = getIntent().getStringExtra("routeFrom");
        routeTo = getIntent().getStringExtra("routeTo");

        populateTextViews();
        btnAdd.setOnClickListener(v -> {
            Log.d("PaymentActivity", "Add Passenger button clicked.");
            addPassengerDetails(true);
        });

        btnRemove.setOnClickListener(v -> {
            Log.d("PaymentActivity", "Remove Passenger button clicked.");
            removePassengerDetails();
        });

// Set up Pay button
        Button payButton = findViewById(R.id.btn_pay);
        payButton.setOnClickListener(v -> {
            Log.d("PaymentActivity", "Pay button clicked. Current passengerList size: " + passengerList.size() + ", Selected seats: " + selectedSeats.size());

            // Automatically add the last entered passenger if the passenger list is incomplete
            while (passengerList.size() < selectedSeats.size()) {
                Log.d("PaymentActivity", "Automatically adding a passenger. Current passengerList size: " + passengerList.size());
                addPassengerDetails(passengerList.size() == selectedSeats.size() - 1); // isFinalPassenger = true for the last passenger
            }

            if (passengerList.isEmpty() || !areAllPassengersValid()) {
                Log.d("PaymentActivity", "Validation failed: Passenger list is empty or not all passengers are valid.");
                Toast.makeText(PaymentActivity.this, "Please fill in all passenger details before making payment.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (validateInputs()) {
                Log.d("PaymentActivity", "Input validation passed. Proceeding to validate seat availability.");
                validateSeatsAvailabilityFromFirestore();
            } else {
                Log.d("PaymentActivity", "Input validation failed.");
                Toast.makeText(PaymentActivity.this, "Please provide valid details before proceeding.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showLoginPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("You must log in to book a ticket. Redirecting to the login page.")
                .setCancelable(false) // Prevent dismissal by back press or tapping outside
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss(); // Close the dialog
                    // Redirect to MainActivity and open ThirdFragment
                    Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                    intent.putExtra("fragment", 2); // Indicate the ThirdFragment
                    startActivity(intent);
                    finish(); // Close PaymentActivity
                })
                .show();
    }
    // Method to validate seat availability from Firestore
    private void validateSeatsAvailabilityFromFirestore() {
        db.collection("bookings")
                .whereEqualTo("busName", busName)
                .whereEqualTo("date", date)
                .whereEqualTo("routeFrom", routeFrom)
                .whereEqualTo("arrivalTime", arrivalTime)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean seatFound = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve already booked seats from Firestore
                            List<String> bookedSeats = (List<String>) document.get("selectedSeats");

                            // Check if any of the selected seats are already booked
                            for (String seat : selectedSeats) {
                                if (bookedSeats != null && bookedSeats.contains(seat)) {
                                    seatFound = true;
                                    break;
                                }
                            }

                            if (seatFound) break; // Exit loop early if a match is found
                        }

                        if (seatFound) {
                            // Seat already booked; block payment initiation
                            Toast.makeText(PaymentActivity.this, "One or more selected seats are already booked. Please choose different seats.", Toast.LENGTH_LONG).show();
                        } else {
                            // No conflicting seats found; proceed with payment
                            initiateRazorpayPayment();
                        }
                    } else {
                        Toast.makeText(PaymentActivity.this, "Error checking seat availability: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void initiateRazorpayPayment() {
        // Initialize Razorpay Checkout
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_@key");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "T-BUS Ticket Booking");
            options.put("description", "Payment for route: " + selectedBoardingPoint);
            options.put("currency", "INR");

            // Convert fare to paise
            int amountInPaise = (int) (totalFare * 100);
            options.put("amount", amountInPaise);

            // Prefill information
            options.put("prefill.email", etEmail.getText().toString().trim());
            options.put("prefill.contact", etPhone.getText().toString().trim());

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        // Fetch user details for the first passenger (and potentially multiple passengers if needed)
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String uniquePNR = generatePNR(etName.getText().toString().trim(), phone);

        List<Map<String, Object>> passengersData = new ArrayList<>();

        // Loop through the passenger list to gather data for all passengers
        for (Passenger passenger : passengerList) {
            Map<String, Object> passengerData = new HashMap<>();
            passengerData.put("name", passenger.getName());
            passengerData.put("age", passenger.getAge());
            passengerData.put("gender", passenger.getGender());
            passengersData.add(passengerData);
        }

        // Prepare the payment data to save in Firestore
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("transactionID", razorpayPaymentID);
        paymentData.put("PNR", uniquePNR);
        paymentData.put("phone", phone);
        paymentData.put("email", email);
        paymentData.put("busName", busName);
        paymentData.put("date", date);
        paymentData.put("arrivalTime", arrivalTime);
        paymentData.put("departureTime", departureTime);
        paymentData.put("routeFrom", routeFrom);
        paymentData.put("routeTo", routeTo);
        paymentData.put("boardingPoint", selectedBoardingPoint);
        paymentData.put("selectedSeats", selectedSeats);
        paymentData.put("totalFare", totalFare);

        // Add the list of passengers to the payment data
        paymentData.put("passengers", passengersData);

        // Save the booking data to Firestore
        db.collection("bookings")
                .add(paymentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking saved! Transaction ID: " + razorpayPaymentID, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save booking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate Name
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);

            // Age must be a number between 4 and 99 (inclusive)
            if (age < 4 || age > 99) {
                etAge.setError("Age must be between 4 and 99");
                etAge.requestFocus();
                return false;
            }

            // Check if the age input is a valid two-digit number
            if (ageStr.length() > 2) {
                etAge.setError("Age must be a two-digit number");
                etAge.requestFocus();
                return false;
            }

        } catch (NumberFormatException e) {
            etAge.setError("Enter a valid number for age");
            etAge.requestFocus();
            return false;
        }

        // Validate Phone
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() != 10) {
            etPhone.setError("Enter a valid 10-digit phone number");
            etPhone.requestFocus();
            return false;
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }
    private String generatePNR(String name, String phone) {
        SecureRandom random = new SecureRandom();
        String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder pnrBuilder = new StringBuilder();

        // Add initials from user data
        if (name.length() >= 2) {
            pnrBuilder.append(name.substring(0, 2).toUpperCase());
        } else {
            pnrBuilder.append("XX");
        }

        if (phone.length() >= 2) {
            pnrBuilder.append(phone.substring(0, 2));
        } else {
            pnrBuilder.append("00");
        }

        // Add random alphanumeric characters to complete 10 characters
        for (int i = pnrBuilder.length(); i < 10; i++) {
            pnrBuilder.append(alphaNumeric.charAt(random.nextInt(alphaNumeric.length())));
        }
        return pnrBuilder.toString();
    }
    private void populateTextViews() {
        // Automatically populates the TextViews with the data passed from the previous activity
        routeText.setText(String.format("%s - %s", routeFrom, routeTo));
        dateText.setText(String.format("Date: %s", date));
        routeNameText.setText(String.format("%s", busName));
        departureTimeText.setText(String.format("Departure: %s", departureTime));
        arrivalTimeText.setText(String.format("Arrival: %s", arrivalTime));
    }
    private void addPassengerDetails(boolean isFinalPassenger) {
        Log.d("addPassengerDetails", "Started adding passenger details. isFinalPassenger: " + isFinalPassenger);

        String name = etName.getText().toString().trim();
        String ageText = etAge.getText().toString().trim();
        String gender = "";

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == rbMale.getId()) {
            gender = "Male";
        } else if (selectedGenderId == rbFemale.getId()) {
            gender = "Female";
        }

        // Log input values
        Log.d("addPassengerDetails", "Input values - Name: " + name + ", Age: " + ageText + ", Gender: " + gender);

        // Validate input
        if (name.isEmpty() || ageText.isEmpty() || gender.isEmpty()) {
            Log.d("addPassengerDetails", "Validation failed: Empty fields. Name: " + name + ", Age: " + ageText + ", Gender: " + gender);
            Toast.makeText(this, "Please fill all fields before adding.", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageText);
        Passenger passenger = new Passenger(name, age, gender);

        // Check seat capacity
        if (passengerList.size() >= selectedSeats.size()) {
            Log.d("addPassengerDetails", "Seat capacity exceeded. Current passengerList size: " + passengerList.size() + ", Selected seats: " + selectedSeats.size());
            new AlertDialog.Builder(this)
                    .setTitle("Seat Capacity Exceeded")
                    .setMessage("You cannot add more passengers than the number of selected seats.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Update or add the passenger
        boolean passengerExists = false;
        for (int i = 0; i < passengerList.size(); i++) {
            if (passengerList.get(i).getName().equals(name)) {
                Log.d("addPassengerDetails", "Passenger with name " + name + " already exists. Updating details.");
                passengerList.set(i, passenger);
                passengerExists = true;
                break;
            }
        }
        if (!passengerExists) {
            Log.d("addPassengerDetails", "Adding new passenger: " + passenger.toString());
            passengerList.add(passenger);
        }

        // Log the updated passenger list
        Log.d("addPassengerDetails", "Current passengerList size: " + passengerList.size());
        for (Passenger p : passengerList) {
            Log.d("addPassengerDetails", "Passenger in list: " + p.toString());
        }

        // Show success message only when all passengers are added
        if (passengerList.size() == selectedSeats.size() && isFinalPassenger) {
            Log.d("addPassengerDetails", "All passenger details have been filled.");
            Toast.makeText(this, "All passenger details have been filled.", Toast.LENGTH_SHORT).show();
        }

        // Clear fields for new input if not the last passenger
        if (passengerList.size() < selectedSeats.size()) {
            Log.d("addPassengerDetails", "Clearing input fields for next passenger.");
            clearFields();
            etName.requestFocus();
        }
    }

    private void removePassengerDetails() {
        // Check if there are passengers to remove
        if (passengerList.isEmpty()) {
            Toast.makeText(this, "No passengers to remove!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear all passengers from the list
        passengerList.clear();
        Toast.makeText(this, "All passengers have been removed.", Toast.LENGTH_SHORT).show();

        // Clear the input fields after removal
        clearFields();
    }

    private void clearFields() {
        etName.setText("");
        etAge.setText("");
        rgGender.clearCheck();  // Clear the gender selection
    }
    private boolean areAllPassengersValid() {
        for (Passenger passenger : passengerList) {
            if (TextUtils.isEmpty(passenger.getName()) ||
                    passenger.getAge() < 4 || passenger.getAge() > 99 ||
                    TextUtils.isEmpty(passenger.getGender())) {
                return false;
            }
        }
        return true;
    }
}
