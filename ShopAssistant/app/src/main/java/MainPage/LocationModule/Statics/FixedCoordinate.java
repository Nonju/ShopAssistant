package MainPage.LocationModule.Statics;

import MainPage.LocationModule.Entities.Coordinate;

/**
 * Created by Hannes on 2016-12-14.
 *
 * Fixed coordinate as third point when calculating angle to DEST.
 */

public class FixedCoordinate {
    // Default constructor for Coordinate = (0,0) --> return new Coordinate without parameters
    public static Coordinate getFixed() { return new Coordinate(); }
}
