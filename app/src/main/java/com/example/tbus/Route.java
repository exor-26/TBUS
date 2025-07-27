package com.example.tbus;

import java.io.Serializable;
import java.util.HashMap;

public class Route implements Serializable {
    private String busName;
    private String departureTime;
    private String arrivalTime;
    private String routeFrom;
    private String routeTo;
    private String date;
    private String price;
    private HashMap<String, String> boardingPoints; // Add boarding points
    private String finalStation; // Add final station

    // Constructor
    public Route(String busName, String departureTime, String arrivalTime, String routeFrom, String routeTo, String date, String price, HashMap<String, String> boardingPoints, String finalStation) {
        this.busName = busName;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.routeFrom = routeFrom;
        this.routeTo = routeTo;
        this.date = date;
        this.price = price;
        this.boardingPoints = boardingPoints;
        this.finalStation = finalStation;
    }

    // Getters
    public String getBusName() {
        return busName;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getRouteFrom() {
        return routeFrom;
    }

    public String getRouteTo() {
        return routeTo;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() { return price; }

    public HashMap<String, String> getBoardingPoints() { return boardingPoints; }

    public String getFinalStation() { return finalStation; }

    // Setters
    public void setBusName(String busName) {
        this.busName = busName;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setRouteFrom(String routeFrom) {
        this.routeFrom = routeFrom;
    }

    public void setRouteTo(String routeTo) {
        this.routeTo = routeTo;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPrice(String price) {
        this.price = price; // Add setter for price
    }

    public void setBoardingPoints(HashMap<String, String> boardingPoints) {
        this.boardingPoints = boardingPoints;
    }

    public void setFinalStation(String finalStation) {
        this.finalStation = finalStation;
    }
}