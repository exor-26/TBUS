package com.example.tbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private Context context;
    private FirebaseFirestore db;

    public HistoryAdapter(List<HistoryItem> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        String pnr = item.getPnr();

        // Display basic information
        holder.transactionID.setText("Transaction ID: " + item.getTransactionID());
        holder.route.setText(item.getRouteFrom() + " -> " + item.getRouteTo());
        holder.departureTime.setText("Departure: " + item.getDepartureTime());

        // Set default color for PNR text (blue by default)
        holder.pnr.setText("PNR: " + pnr);
        holder.pnr.setTextColor(Color.parseColor("#27A8BA")); // Default color (blue)

        // Check if the cancellation status is already cached in the item
        if (!item.isCancelled()) {
            // If not, check the cancellation status in Firestore
            checkIfCancelled(pnr, item, holder);
        } else {
            // If already cancelled, apply red color
            holder.pnr.setTextColor(context.getResources().getColor(R.color.red));
        }

        // Set the onClickListener for the item
        holder.itemView.setOnClickListener(v -> {
            // Check if the cancellation status has been updated
            if (item.isCancelled()) {
                // Show a message or toast for cancelled tickets
                Toast.makeText(context, "This ticket has already been cancelled.", Toast.LENGTH_SHORT).show();
            } else {
                // Only navigate if the ticket is not cancelled
                Intent intent = new Intent(context, HistoryContainer.class);
                intent.putExtra("pnr", pnr); // Pass PNR data
                context.startActivity(intent);
            }
        });
    }

    private void checkIfCancelled(String pnr, HistoryItem item, HistoryViewHolder holder) {
        db.collection("cancellations")
                .whereEqualTo("PNR", pnr)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // PNR is found in the cancellations collection, so change text color to red
                        item.setCancelled(true); // Mark this item as cancelled
                        holder.pnr.setTextColor(context.getResources().getColor(R.color.red));
                    } else {
                        // PNR is not cancelled, set the normal color (blue)
                        item.setCancelled(false); // Mark this item as not cancelled
                        holder.pnr.setTextColor(Color.parseColor("#27A8BA")); // Default color for non-cancelled tickets (blue)
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur while checking Firestore
                    Log.e("HistoryAdapter", "Error checking cancellation status: ", e);
                });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView pnr, transactionID, route, departureTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            pnr = itemView.findViewById(R.id.text_pnr);
            transactionID = itemView.findViewById(R.id.text_transaction_id);
            route = itemView.findViewById(R.id.text_route);
            departureTime = itemView.findViewById(R.id.text_departure_time);
        }
    }
}