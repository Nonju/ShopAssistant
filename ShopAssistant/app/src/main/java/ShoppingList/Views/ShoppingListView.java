package ShoppingList.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ShoppingList.Entities.ShoppingList;
import se.liu.ida.hanal086.hannes.shopassistant.R;

/**
 * Created by hannes on 2016-11-23.
 */

public class ShoppingListView extends RelativeLayout {
    private int listID; // Needed ??
    private TextView titleTextView;
    //private TextView descTextView; // REMOVE ???
    //private Button deleteListButton; // Replace with swipe ???


    public ShoppingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shoppinglist_item_view_children, this, true);
        setupChildren();
    }

    public static ShoppingListView inflate(ViewGroup parent) {
        ShoppingListView shoppingListView = (ShoppingListView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shoppinglist_item_view, parent, false);
        return shoppingListView;
    }

    private void setupChildren() {
        titleTextView = (TextView) findViewById(R.id.item_titleTextView);
        //descTextView = (TextView) findViewById(R.id.item_descriptionTextView);
    }


    public void setItem(ShoppingList item) {
        titleTextView.setText(item.getTitle());
        //descTextView.setText(item.getDescription());
    }
}
