package se.liu.ida.hanal086.hannes.shopassistant;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import ShoppingList.Builders.StringSeqBuilder;
import ShoppingList.Adapters.ShoppingListItemAdapter;
import ShoppingList.Entities.PopoverInputs;
import ShoppingList.Entities.ShoppingList;
import ShoppingList.Entities.ShoppingListItem;
import General.Exceptions.AmountOutOfRangeException;
import General.Exceptions.StringEmptyException;

public class SingleListActivity extends AppCompatActivity {
    // Activity global values
    private String TAG = "--SINGLELIST--";
    private String ERROR_TAG = "---SL_ERROR---";

    // Components
    private EditText listName;
    private ListView listItems;

    // Current ShoppingList
    ShoppingList currentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_list);

        // Extract current shoppingList form intent
        currentList = (ShoppingList) getIntent().getSerializableExtra("Data");

        // Setup Listname's Edittext
        listName = (EditText) findViewById(R.id.singleList_listName);
        listName.setText(currentList.getTitle()); // Default value
        listName.clearFocus(); // Make sure textarea isn't selected by default
        listName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { onTitleChange(String.valueOf(s)); }
        });

        // Setup Button for adding new listItems
        Button addButton = (Button) findViewById(R.id.singleList_addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { addNewItemButtonClicked(); }
        });

        // Setup BackButton
        Button backButton = (Button) findViewById(R.id.singleList_backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backButtonClicked(); }
        });

        // Setup ListView
        ShoppingListItemAdapter listItemsAdapter = new ShoppingListItemAdapter(this);
        listItems = (ListView) findViewById(R.id.singleList_listItemView);
        listItems.setAdapter(listItemsAdapter);
        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get and pass clicked item to clickHandler
                ShoppingListItemAdapter adapter = (ShoppingListItemAdapter) listItems.getAdapter();
                listItemClicked(adapter.getItem(position), position);
            }
        });

        Log.d(TAG, "OnCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");

        updateListItems();
    }

    private void onTitleChange(String newTitle) {
        currentList.setTitle(newTitle);
    }

    private void updateListItems() {
        Log.d(TAG, "UpdateListItems");

        // Get items from currentList
        ArrayList<ShoppingListItem> items = currentList.getItems();

        if (items == null || items.isEmpty()) return; // Return if no items in list

        // Get adapter from listView
        ShoppingListItemAdapter adapter = (ShoppingListItemAdapter) listItems.getAdapter();

        // Add all found items to adapter
        for (ShoppingListItem item: items) { adapter.add(item); }

    }

    private LinearLayout createPopoverLayout(PopoverInputs inputs) {

        LinearLayout containerLayout = new LinearLayout(this);
        containerLayout.setOrientation(LinearLayout.VERTICAL);

        for (Map.Entry<String, EditText> input: inputs.getInputs().entrySet()) {
            // Create inner layout to carry label and input-area
            LinearLayout innerLayout = new LinearLayout(this);
            innerLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Get EditText to capture input
            EditText editText = input.getValue();

            // Create label with key
            TextView label = new TextView(this);
            label.setText(input.getKey());

            // Use default values on label/EditText textSizes
            float defaultTextSize = 25f;
            label.setHint(StringSeqBuilder.genString(14, ' '));
            label.setTextSize(defaultTextSize);
            editText.setHint(StringSeqBuilder.genString(14, ' '));
            editText.setTextSize(defaultTextSize);

            // Assemble label with pre-created input-field in innerLayout
            innerLayout.addView(label);
            innerLayout.addView(editText);


            // Append finished result to containerLayout
            containerLayout.addView(innerLayout);
        }

        return containerLayout;
    }

    private void listItemClicked(ShoppingListItem item, final int position) {
        // Add popover
        Log.d(TAG, "ListItemClicked");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update item!");

        // Create inputbox for itemName with existing name
        final EditText inputName = new EditText(this);
        inputName.setText(item.getItemName());
        inputName.setInputType(InputType.TYPE_CLASS_TEXT);


        // Create inputbox for itemAmount with existing value
        final EditText inputAmount = new EditText(this);
        inputAmount.setText(Integer.toString(item.getItemAmount()));
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Add layout to builder
        PopoverInputs poInputs = new PopoverInputs();
        poInputs.addInput("ItemName: ", inputName);
        poInputs.addInput("ItemAmount: ", inputAmount);
        builder.setView(createPopoverLayout(poInputs));

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add item to ListView
                ShoppingListItemAdapter adapter = (ShoppingListItemAdapter) listItems.getAdapter();
                try {

                    // Create new updated listItem from given data
                    ShoppingListItem updatedItem = new ShoppingListItem(
                            inputName.getText().toString(),
                            Integer.parseInt(inputAmount.getText().toString())
                    );
                    // Add new item to listItems and tell user about it
                    adapter.updateItem(position, updatedItem); // Update item in adapter
                    currentList.updateItem(position, updatedItem); // .. as well as to currentList variable
                    Toast.makeText(SingleListActivity.this, "Updated item!", Toast.LENGTH_SHORT).show();

                }
                catch (StringEmptyException | AmountOutOfRangeException e) { // Catch possible exceptions
                    Log.d(ERROR_TAG, e.getMessage());
                    Toast.makeText(SingleListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) { // Other possible General.Exceptions
                    Log.d(ERROR_TAG, "Unknown error occured: " + e.getMessage());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
        });

        // Display builder to user
        builder.show();
    }

    private void addNewItemButtonClicked() {
        Log.d(TAG, "AddNewItemButtonClicked");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set new item values!");

        // Create inputbox for itemName and set its inputType
        final EditText inputName = new EditText(this);
        inputName.setInputType(InputType.TYPE_CLASS_TEXT);

        // Create inputbox for itemAmount and set its inputType
        final EditText inputAmount = new EditText(this);
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Add layout to builder
        PopoverInputs poInputs = new PopoverInputs();
        poInputs.addInput("ItemName: ", inputName);
        poInputs.addInput("ItemAmount: ", inputAmount);
        builder.setView(createPopoverLayout(poInputs));

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add item to ListView
                ShoppingListItemAdapter adapter = (ShoppingListItemAdapter) listItems.getAdapter();
                try {
                    // Prevent int-convertion errors
                    if (inputAmount.getText().toString().equals("")) inputAmount.setText("0");

                    // Create new listItem from given data
                    ShoppingListItem item = new ShoppingListItem(
                            inputName.getText().toString(),
                            Integer.parseInt(inputAmount.getText().toString())
                    );
                    // Add new item to listItems and tell user about it
                    adapter.add(item); // add item to adapter
                    currentList.addItem(item); // .. as well as to currentList variable
                    Toast.makeText(SingleListActivity.this, "Added new item!", Toast.LENGTH_SHORT).show();

                }
                catch (StringEmptyException | AmountOutOfRangeException e) { // Catch possible exceptions
                    Log.d(ERROR_TAG, e.getMessage());
                    Toast.makeText(SingleListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) { // Other possible General.Exceptions
                    Log.d(ERROR_TAG, "Unknown error occured: " + e.getMessage());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
        });

        // Display builder to user
        builder.show();
    }


    private void backButtonClicked() {
        Log.d(TAG, "BackButtonClicked");

        // Get current intent and pass the updated list back to caller
        Intent intent = getIntent();
        intent.putExtra("Data", currentList);
        setResult(Activity.RESULT_OK, intent);

        finish();
    }

}
