package MainPage.LocationModule.Entities;

/**
 * Created by hannes on 2016-12-07.
 *
 * Entitiy to store a coordinate
 */

public class Coordinate {

    private double longitude, latitude;

    public Coordinate() { this(0, 0); }
    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() { return longitude; }
    public double getLatitude() { return latitude; }
}
