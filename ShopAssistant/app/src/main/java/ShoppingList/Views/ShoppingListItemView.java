package ShoppingList.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ShoppingList.Entities.ShoppingListItem;
import se.liu.ida.hanal086.hannes.shopassistant.R;

/**
 * Created by hannes on 2016-12-06.
 */

public class ShoppingListItemView extends RelativeLayout {
    private TextView itemName, itemAmount;

    public ShoppingListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.shoppinglist_single_item_view_children, this, true);
        setupChildren();
    }

    public static ShoppingListItemView inflate(ViewGroup parent) {
        ShoppingListItemView singleListItemView = (ShoppingListItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shoppinglist_single_item_view, parent, false);
        return singleListItemView;
    }

    private void setupChildren() {
        itemName = (TextView) findViewById(R.id.item_itemName);
        itemAmount = (TextView) findViewById(R.id.item_itemAmount);
    }

    public void setItem(ShoppingListItem item) {
        itemName.setText(item.getItemName());
        itemAmount.setText(Integer.toString(item.getItemAmount()));
    }
}
