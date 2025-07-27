package com.example.tbus;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private List<Route> routeList;

    // Constructor to initialize the route list
    public RouteAdapter(List<Route> routeList) {
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_route layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        // Ensure the list is not empty before binding data
        if (routeList != null && position < routeList.size()) {
            Route route = routeList.get(position);

            // Bind data to the respective views
            holder.routeName.setText(route.getBusName());
            holder.routeText.setText(route.getRouteFrom() + " - " + route.getRouteTo()); // Set the combined route text
            holder.departureTime.setText("Departure: " + route.getDepartureTime());
            holder.arrivalTime.setText("Arrival: " + route.getArrivalTime());
            holder.date.setText("Date: " + route.getDate());
            holder.busPrice.setText(route.getPrice());  // Bind the price to the TextView

            holder.itemView.setOnClickListener(view -> {
                // Intent to move to SearchActivity
                Intent intent = new Intent(view.getContext(), SeatActivity.class);
                intent.putExtra("routeDetails", route); // Pass the selected route object
                view.getContext().startActivity(intent);
            });
            Log.d("SeatActivity", "Received Route: " + route.getBusName());
            Log.d("RouteAdapter", "Binding: " + route.getBusName());
        }
    }

    @Override
    public int getItemCount() {
        return (routeList != null) ? routeList.size() : 0;  // Return 0 if the list is null
    }

    // Method to update the data list dynamically
    public void updateRouteList(List<Route> newRouteList) {
        this.routeList = newRouteList;
        notifyDataSetChanged();  // Notify adapter that data has changed
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeName, routeText, departureTime, arrivalTime, date, busPrice;

        public RouteViewHolder(View itemView) {
            super(itemView);
            // Initialize all the TextViews from the updated layout
            routeName = itemView.findViewById(R.id.routeName);
            routeText = itemView.findViewById(R.id.routeText); // Updated: Single TextView for "From - To"
            departureTime = itemView.findViewById(R.id.departureTime);
            arrivalTime = itemView.findViewById(R.id.arrivalTime);
            date = itemView.findViewById(R.id.date);
            busPrice = itemView.findViewById(R.id.busPrice);  // Initialize the busPrice TextView
        }
    }
}