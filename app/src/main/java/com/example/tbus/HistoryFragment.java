package com.example.tbus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private FirebaseFirestore firestore;
    private List<HistoryItem> historyList;
    private ProgressBar loader; // ProgressBar to show loading state
    private TextView messageTextView; // TextView to display messages

    public HistoryFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.viewpager2, container, false);

        // Initialize Firestore and RecyclerView
        firestore = FirebaseFirestore.getInstance();
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Enable nested scrolling for the RecyclerView
        recyclerViewHistory.setNestedScrollingEnabled(true);

        // Initialize data list and adapter
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList, getActivity());
        recyclerViewHistory.setAdapter(historyAdapter);

        // Initialize ProgressBar (loader) and Message TextView
        loader = view.findViewById(R.id.loader);
        messageTextView = view.findViewById(R.id.messageTextView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            loadHistoryData();
        } else {
            showMessage("Please sign in to view your bookings.");
        }
    }

    private void loadHistoryData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // If no user is signed in, show a message and stop any loading indicators
            showMessage("Please sign in to view your bookings.");
            loader.setVisibility(View.GONE);
            return; // Exit early to prevent further execution
        }

        // Show the progress bar while loading
        loader.setVisibility(View.VISIBLE);

        // Query Firestore for bookings
        String userEmail = currentUser.getEmail();
        firestore.collection("bookings")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        historyList.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pnr = document.getString("PNR");
                            HistoryItem item = document.toObject(HistoryItem.class);
                            if (item != null) {
                                item.setPnr(pnr);
                                historyList.add(item);
                            }
                        }

                        // Update UI based on data
                        if (historyList.isEmpty()) {
                            showMessage("No bookings found yet.");
                        } else {
                            showBookings();
                        }
                    } else {
                        // Handle query error
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                        showMessage("An error occurred while loading your bookings.");
                    }

                    // Hide loader
                    loader.setVisibility(View.GONE);
                });
    }

    private void showMessage(String message) {
        messageTextView.setText(message);
        messageTextView.setVisibility(View.VISIBLE);
        recyclerViewHistory.setVisibility(View.GONE); // Hide RecyclerView to prevent interaction
    }

    private void showBookings() {
        messageTextView.setVisibility(View.GONE);
        recyclerViewHistory.setVisibility(View.VISIBLE);
        historyAdapter.notifyDataSetChanged();
    }
}