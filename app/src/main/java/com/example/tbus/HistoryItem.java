package com.example.tbus;

public class HistoryItem {
    private String pnr;
    private String transactionID;
    private String routeFrom;
    private String routeTo;
    private String departureTime;
    private boolean isCancelled;

    // Empty constructor for Firestore
    public HistoryItem() {}

    public HistoryItem(String pnr, String transactionID, String routeFrom, String routeTo, String departureTime, boolean isCancelled) {
        this.pnr = pnr;
        this.transactionID = transactionID;
        this.routeFrom = routeFrom;
        this.routeTo = routeTo;
        this.departureTime = departureTime;
        this.isCancelled = isCancelled;
    }

    // Getters and Setters
    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getRouteFrom() {
        return routeFrom;
    }

    public void setRouteFrom(String routeFrom) {
        this.routeFrom = routeFrom;
    }

    public String getRouteTo() {
        return routeTo;
    }

    public void setRouteTo(String routeTo) {
        this.routeTo = routeTo;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
    // Getter and Setter for isCancelled
    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}