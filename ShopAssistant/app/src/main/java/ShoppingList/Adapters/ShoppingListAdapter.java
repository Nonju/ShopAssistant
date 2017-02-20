package ShoppingList.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import ShoppingList.Entities.ShoppingList;
import ShoppingList.Entities.ShoppingListItem;
import ShoppingList.Views.ShoppingListView;


/**
 * Created by hannes on 2016-11-23.
 */

public class ShoppingListAdapter extends ArrayAdapter<ShoppingList> {

    private Context context;

    public ShoppingListAdapter(Context context) { this(context, new ArrayList<ShoppingList>()); }
    public ShoppingListAdapter(Context context, List<ShoppingList> items) {
        super(context, 0, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShoppingListView shoppingListView = (ShoppingListView)convertView;
        Log.d("SH_L_ADAPTER", "INFLATE");
        if (shoppingListView == null) shoppingListView = ShoppingListView.inflate(parent);
        shoppingListView.setItem(getItem(position));
        return shoppingListView;
    }

    public void updateItem(String key, ShoppingList newObject) {
        // Container for current data
        ArrayList<ShoppingList> items = new ArrayList<>();

        // Copy stored data to container and modify <position>
        for (int i = 0; i < this.getCount(); i++) {
            if (key == getItem(i).getTitle()) items.add(i, newObject); // Replace old with modified
            else items.add(i, this.getItem(i)); // Regular copy to new list
        }

        // Clear current data and replace with modified
        this.clear();
        this.addAll(items);
    }

}
