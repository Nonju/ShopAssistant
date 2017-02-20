package ShoppingList.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import ShoppingList.Entities.ShoppingListItem;
import ShoppingList.Views.ShoppingListItemView;

/**
 * Created by hannes on 2016-12-06.
 */

public class ShoppingListItemAdapter extends ArrayAdapter<ShoppingListItem> {

    private Context context;

    public ShoppingListItemAdapter(Context context) { this(context, new ArrayList<ShoppingListItem>()); }
    public ShoppingListItemAdapter(Context context, ArrayList<ShoppingListItem> items) {
        super(context, 0, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShoppingListItemView shoppingListItemView = (ShoppingListItemView)convertView;
        Log.d("SL_ADAPTER", "INFLATE");
        if (shoppingListItemView == null) shoppingListItemView = ShoppingListItemView.inflate(parent);
        shoppingListItemView.setItem(getItem(position));
        return shoppingListItemView;
    }

    public void updateItem(int position, ShoppingListItem newObject) {
        // Container for current data
        ArrayList<ShoppingListItem> items = new ArrayList<>();

        // Copy stored data to container and modify <position>
        for (int i = 0; i < this.getCount(); i++) {
            if (i == position) items.add(position, newObject); // Replace old with modified
            else items.add(i, this.getItem(i)); // Regular copy to new list
        }

        // Clear current data and replace with modified
        this.clear();
        this.addAll(items);
    }

}
