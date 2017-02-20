package MainPage.LocationModule;

import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.util.Log;

import MainPage.LocationModule.Entities.Coordinate;
import MainPage.LocationModule.Statics.FixedCoordinate;

/**
 * Created by Hannes on 2016-12-11.
 *
 * Listener to update users angle to set destination
 *
 * Longitude = X
 * Latitude  = Y
 */

public class UserLocationListener implements LocationListener {

    private float angleToDest; // TODO Check if needed
    private Coordinate destCoord;

    public UserLocationListener() { this(new Coordinate()); }
    public UserLocationListener(Coordinate initDestCoord) {
        super();
        this.angleToDest = 0f; // default value
        this.destCoord = initDestCoord;
    }


    public float calcAngle2(Location location) { // TODO Remove uneccessary code
        // Calculated angle has to be double
        double newAngle = 0d;
        // Fixed point to work as third reference when calculating angle
        //Coordinate fixed = FixedCoordinate.getFixed();

        Location destLocation = new Location("");
        destLocation.setLatitude(destCoord.getLatitude());
        destLocation.setLongitude(destCoord.getLongitude());
        return location.bearingTo(destLocation);

        /*double angle1 = Math.atan2(
                location.getLatitude() - fixed.getLatitude(),
                location.getLongitude() - fixed.getLongitude()
        );
        double angle2 = Math.atan2(
                destCoord.getLatitude() - fixed.getLatitude(),
                destCoord.getLongitude() - fixed.getLongitude()
        );

        //newAngle = Math.toDegrees(angle1) - Math.toDegrees(angle2);
        newAngle = Math.toDegrees((angle1 - angle2));
        return (float) newAngle;*/
    }

    public double calcAngle(Location location) {

        //float newAngle = 0f;
        double newAngle = Math.atan2(
                (destCoord.getLatitude() - location.getLatitude()),
                (destCoord.getLongitude() - location.getLongitude())
        ) * (180 / Math.PI);

        // Update angle towards destination
        /*newAngle = (float)(
                (destCoord.getLatitude() - location.getLatitude()) /
                (destCoord.getLongitude() - location.getLongitude())
        );*/

        // Add phone "angle-from-North" to properly display direction to destination
        //newAngle += (azimut * (180 / Math.PI));

        Log.d("---USRLOCLIST---", "New Angle: " + newAngle);
        //return Math.toDegrees(newAngle);
        return newAngle;
    }

    @Override
    public void onLocationChanged(Location location) {  }

    // Getters
    public float getAngleToDest() { return angleToDest; }
    public Coordinate getDestCoord() { return destCoord; }

    // Setters
    public void setAngleToDest(float angleToDest) { this.angleToDest = angleToDest; }
    public void setDestCoord(Coordinate destCoord) { this.destCoord = destCoord; }


}
