package ShoppingList.Entities;


import java.io.Serializable;
import java.util.ArrayList;

import General.Exceptions.StringEmptyException;

/**
 * Created by hannes on 2016-11-23.
 *
 * Container for a single shopping-list
 */

public class ShoppingList implements Serializable {
    private String title;
    private ArrayList<ShoppingListItem> items;

    public ShoppingList(String title) throws StringEmptyException {
        this(title, new ArrayList<ShoppingListItem>());
    }
    public ShoppingList(String title, ArrayList<ShoppingListItem> items) throws StringEmptyException {
        // Prevents empty strings from being passed to constructor
        if (title.equals(""))
            throw new StringEmptyException("Tried to pass empty string to constructor \"ShoppingList\"");

        this.title = title;
        this.items = items;
    }

    // Getters
    public String getTitle() {
        return title;
    }
    public ArrayList<ShoppingListItem> getItems() { return items; }

    // Setters
    public void setTitle(String newTitle) { this.title = newTitle; }
    public void addItem(ShoppingListItem item) { items.add(item); }
    public void updateItem(int position, ShoppingListItem item) { items.set(position, item); }


}
