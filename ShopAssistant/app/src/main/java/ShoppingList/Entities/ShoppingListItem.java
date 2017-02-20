package ShoppingList.Entities;

import android.util.Log;

import java.io.Serializable;

import General.Exceptions.AmountOutOfRangeException;
import General.Exceptions.StringEmptyException;

/**
 * Created by hannes on 2016-12-05.
 *
 * Container for single item in shopping-list
 */

public class ShoppingListItem implements Serializable {

    private String itemName;
    private int itemAmount;

    public ShoppingListItem(String itemName, int itemAmount) throws StringEmptyException, AmountOutOfRangeException {
        Log.d("--SHLISTITEM--", "Amount: " + itemAmount);

        // Prevents empty strings from being passed to constructor
        if (itemName.trim().equals(""))
            throw new StringEmptyException("Tried to pass empty string to constructor \"ShoppingList\"");

        // Prevents itemAmount from being to small/large
        if (itemAmount <= 0 || itemAmount > 99)
            throw new AmountOutOfRangeException("Given itemAmount not in range (1-99)");

        this.itemName = itemName;
        this.itemAmount = itemAmount;
    }


    public String getItemName() { return itemName; }
    public int getItemAmount() { return itemAmount; }
}
