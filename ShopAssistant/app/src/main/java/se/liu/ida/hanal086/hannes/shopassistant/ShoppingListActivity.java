package se.liu.ida.hanal086.hannes.shopassistant;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import General.Exceptions.AmountOutOfRangeException;
import ShoppingList.Adapters.ShoppingListAdapter;
import ShoppingList.Adapters.ShoppingListItemAdapter;
import ShoppingList.Entities.ShoppingList;
import General.Exceptions.StringEmptyException;
import ShoppingList.Entities.ShoppingListItem;

public class ShoppingListActivity extends AppCompatActivity {
    // Activity global values
    private String TAG = "----SHOPLIST----";
    private String ERROR_TAG = "--SH_L_ERROR--";
    private int ACTIVITY_REQUEST_CODE = 8888;

    // Firebase User
    FirebaseUser currentUser;

    // Database variables
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();


    // Component list
    private TextView listAmount;
    private ListView shoppingLists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Initiate FirebaseUser currentUser
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // ListAmount TextView
        listAmount = (TextView) findViewById(R.id.shoplist_listAmount);

        // BackToMainMenu Button
        Button backToMainMenuButton = (Button) findViewById(R.id.shoplist_backToMainMenuButton);
        backToMainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainMenu();
            }
        });

        // Shoppinglist Listview setup
        ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(this);
        shoppingLists = (ListView) findViewById(R.id.shoplist_shopLists);
        shoppingLists.setAdapter(shoppingListAdapter);
        shoppingLists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass clicked item to clickHandler
                ShoppingListAdapter adapter = (ShoppingListAdapter) shoppingLists.getAdapter();
                listItemClicked(adapter.getItem(position));
            }
        });

        // Setup button for creating new lists
        Button createNewListButton = (Button) findViewById(R.id.shoplist_createNewListButton);
        createNewListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { createNewListButtonClicked(); }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onSTART!");

        // Load any existing lists from users database
        DatabaseReference userlistsRef = dbRef.child("users").child(currentUser.getUid()).child("lists");
        userlistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Updating current shoppinglists");

                // Retrieve listViews adapter in order to add any existing items
                ShoppingListAdapter adapter = (ShoppingListAdapter) shoppingLists.getAdapter();
                adapter.clear(); // Clear old lists appending those stored in db

                // Build listItemView from existing data
                Log.d(TAG, "KEY: " + dataSnapshot.getKey());
                for (DataSnapshot listChild : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Title (" + listChild.getKey() + "): " + listChild.child("title").getValue(String.class));

                    // Create new ShoppingListItems-list
                    ArrayList<ShoppingListItem> items = new ArrayList<ShoppingListItem>();

                    // Go through all eventual items in listChild
                    DataSnapshot listChildItems = listChild.child("items");
                    for (DataSnapshot lcItems : listChildItems.getChildren()) {
                        try {
                            items.add(new ShoppingListItem(
                                    lcItems.child("itemName").getValue(String.class),
                                    lcItems.child("itemAmount").getValue(Integer.class)
                            ));
                        } catch (StringEmptyException | AmountOutOfRangeException e) {
                            Log.d(ERROR_TAG, e.getMessage());
                        }
                    }

                    // Add ShoppingList with eventual items to adapter
                    try {
                        adapter.add(new ShoppingList(listChild.child("title").getValue(String.class), items));
                    } catch (StringEmptyException e) {
                        Log.d(ERROR_TAG, e.getMessage());
                    }
                }

                // Set count to match amount of lists
                listAmount.setText(Long.toString(dataSnapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(ERROR_TAG, databaseError.getMessage());
            }
        });

    }

    private void listItemClicked(ShoppingList item) {
        // Create new intent and pass childData to it
        Intent intent = new Intent(this, se.liu.ida.hanal086.hannes.shopassistant.SingleListActivity.class);
        intent.putExtra("Data", item);
        intent.putExtra("OriginalName", item.getTitle()); // Get original title in case it was updated

        // Start new Activity
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    private void createNewListButtonClicked() {
        Log.d(TAG, "CreateNewListButtonClicked");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set new list name!");

        // Set up the input
        final EditText input = new EditText(this);
        // Set inputType
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Add item to ListView
                ShoppingListAdapter adapter = (ShoppingListAdapter) shoppingLists.getAdapter();
                try {
                    // Create new listItem from inputed data
                    ShoppingList item = new ShoppingList(input.getText().toString());

                    // Store item in database
                    DatabaseReference userListRef = dbRef.child("users")
                            .child(currentUser.getUid())
                            .child("lists");
                    userListRef.push().setValue(item);

                    // Store original version of item in adapter
                    adapter.add(item);

                    // Open new list in singleList editor
                    listItemClicked(item);

                } catch (StringEmptyException e) {
                    Log.d(ERROR_TAG, e.getMessage());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_REQUEST_CODE) {
            final ShoppingList updatedList = (ShoppingList) data.getSerializableExtra("Data");
            final String originalTitle = data.getStringExtra("OriginalName");

            // Update existing listname
            ShoppingListAdapter adapter = (ShoppingListAdapter) shoppingLists.getAdapter();
            adapter.updateItem(originalTitle, updatedList);

            // Update list in database
            DatabaseReference userListRef = dbRef.child("users")
                    .child(currentUser.getUid())
                    .child("lists");

            userListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot listChild: dataSnapshot.getChildren()) {
                        if (listChild.child("title").getValue(String.class).equals(originalTitle)) {

                            // Update list with new values
                            DatabaseReference listChildRef = listChild.getRef();
                            listChildRef.child("title").setValue(updatedList.getTitle());
                            //listChildRef.child("items").setValue(updatedList.getItems());

                            ArrayList<ShoppingListItem> items = updatedList.getItems();
                            Log.d(TAG, "ITEMS: " + items.size());
                            for (int i = 0; i < items.size(); i++) {
                                listChildRef.child("items")
                                        .child(Integer.toString(i))
                                        .setValue(items.get(i));
                            }
                            break; // No need to look any further
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

        }
    }

    private void backToMainMenu() { finish(); }
}
