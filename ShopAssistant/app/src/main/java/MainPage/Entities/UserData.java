package MainPage.Entities;

import java.util.ArrayList;

import MainPage.LocationModule.Enums.CompassState;
import General.Exceptions.StringEmptyException;
import MainPage.LocationModule.Entities.Coordinate;
import ShoppingList.Entities.ShoppingList;

/**
 * Created by Hannes on 2016-11-30.
 *
 * Entity to contain userData as stored in database
 */

public class UserData {
    private String uid;
    private String username;
    private ArrayList<ShoppingList> lists;
    private Coordinate position;
    private CompassState compassState;

    public UserData(String uid, String username) throws StringEmptyException {
        // Prevents empty strings from being passed to constructor
        if (uid.equals("") || username.equals(""))
            throw new StringEmptyException("Tried to pass empty string to constructor \"UserData\"");

        this.uid = uid;
        this.username = username;
        this.lists = new ArrayList<>();
        this.position = new Coordinate(0, 0); // default position'
        this.compassState = CompassState.OFF; // default OFF
    }

    // Getters
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public ArrayList<ShoppingList> getLists() { return lists; }
    public CompassState getCompassState() { return compassState; }
    public Coordinate getPosition() { return position; }

    // Setters
    public void setLists(ArrayList<ShoppingList> lists) { this.lists = lists; }
    public void appendToList(ShoppingList item) { this.lists.add(item); }
    public void setCompassState(CompassState compassState) { this.compassState = compassState; }
    public void setPosition(Coordinate userLocation) { this.position = userLocation; }

}
