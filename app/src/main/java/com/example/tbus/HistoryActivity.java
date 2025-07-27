package com.example.tbus;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private FirebaseFirestore firestore;
    private List<HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Firestore and RecyclerView
        firestore = FirebaseFirestore.getInstance();
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data list and adapter
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList, this);
        recyclerViewHistory.setAdapter(historyAdapter);

        // Load history data from Firestore
        loadHistoryData();
    }

    private void loadHistoryData() {
        // Access the Firestore collection directly
        firestore.collection("bookings")
                .get()  // Fetch all documents in the collection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve PNR as a direct field
                            String pnr = document.getString("PNR");

                            // Convert other structured fields to a HistoryItem object
                            HistoryItem item = document.toObject(HistoryItem.class);

                            if (item != null) {
                                item.setPnr(pnr); // Set the PNR value manually
                                historyList.add(item); // Add the mapped object to the list
                            }
                        }

                        // Notify the adapter that the data has changed to refresh the RecyclerView
                        historyAdapter.notifyDataSetChanged();
                    } else {
                        // Handle any errors
                        Toast.makeText(this, "Error loading data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
}