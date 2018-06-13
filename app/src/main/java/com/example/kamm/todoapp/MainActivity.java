package com.example.kamm.todoapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private TextView headerTitle,
                     headerDate,
                     headerPriority,
                     headerDone;

    private DatabaseReference mDatabase;
    private String mUserId;

    private List<Item> items = new ArrayList<Item>();

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        headerTitle = (TextView) findViewById(R.id.header_title);
        headerTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortFirebase("title");
            }
        });

        headerDate = (TextView) findViewById(R.id.header_date);
        headerDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortFirebase("date");
            }
        });

        headerPriority = (TextView) findViewById(R.id.header_priority);
        headerPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortFirebase("priority");
            }
        });

        headerDone = (TextView) findViewById(R.id.header_done);
        headerDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortFirebase("done");
            }
        });



        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        }
        else {
            mUserId = mFirebaseUser.getUid();

            // Set up ListView
            listView = (ListView) findViewById(R.id.listView);

            // Use Firebase to populate the list.
            mDatabase.child("users").child(mUserId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    updateListView(dataSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    updateListView(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    updateListView(dataSnapshot);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    updateListView(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    if (databaseError != null){
                        showAlertMessage(databaseError.getMessage(), "Error!");
                    }
                }
            });

            // show dialog to edit or delete
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    final Item listItem = (Item)listView.getItemAtPosition(position);

                    final CharSequence[] items = { "Edit", "Delete" };

                    AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                    adb.setTitle("Item: " + listItem.getTitle())
                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (item == 0) {
                                        editRecord(listItem);
                                    }
                                    else if (item == 1){
                                        deleteRecord(listItem);
                                    }
                                }
                            }).show();
                }
            });
        }
    }

    private void sortFirebase(String order){
        mDatabase.child("users").child(mUserId).orderByChild(order);
        mDatabase.child("users").child(mUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateListView(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateListView(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updateListView(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                updateListView(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (databaseError != null){
                    showAlertMessage(databaseError.getMessage(), "Error!");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        boolean logoutClicked = item.getItemId() == R.id.action_logout;
        //noinspection SimplifiableIfStatement
        if (logoutClicked) {
            mFirebaseAuth.signOut();
            loadLogInView();
        }

        boolean addItemClicked = item.getItemId() == R.id.add_btn;
        if (addItemClicked){
            loadAddItemView();
        }

//        boolean exportItemClicked = item.getItemId() == R.id.action_export;
//        if (exportItemClicked){
//            //loadAddItemView();
//        }

        return super.onOptionsItemSelected(item);
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadAddItemView(){
        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
        startActivity(intent);
    }

    private void editRecord(Item item){
        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    private void deleteRecord (Item item){
        mDatabase.child("users").child(mUserId).child("items").child(item.getKey()).removeValue(new DatabaseReference.CompletionListener(){

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null){
                    showAlertMessage(databaseError.getMessage(), "Error!");
                }
            }
        });
    }

    private void updateListView(DataSnapshot dataSnapshot){
        items.clear();
        for (DataSnapshot itemsSnapshot: dataSnapshot.getChildren()){
            Item item = itemsSnapshot.getValue(Item.class);
            item.key = itemsSnapshot.getKey();
            items.add(item);
        }

        items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item left, Item right) {
                return 0;
            }
        });
        listView.setAdapter(new ListAdapter(getApplicationContext(), items));
    }


    private void showAlertMessage(String message, String title){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


}
