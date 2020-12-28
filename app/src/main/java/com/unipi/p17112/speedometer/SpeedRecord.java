package com.unipi.p17112.speedometer;

//Class for speed records
public class SpeedRecord {
    private final double latitude;
    private final double longitude;
    private final double speed;
    private final int limit;
    private final String datetime;

    public SpeedRecord(double longitude, double latitude, double speed, int limit, String datetime){
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.limit = limit;
        this.datetime = datetime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public int getLimit() {
        return limit;
    }

    public String getDatetime() {
        return datetime;
    }
}
