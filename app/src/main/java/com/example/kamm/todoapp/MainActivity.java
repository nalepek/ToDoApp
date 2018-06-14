package com.example.kamm.todoapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
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

    NotificationManager notificationManager;
    private DatabaseReference mDatabase;
    private String mUserId;

    private List<Item> items = new ArrayList<Item>();

    ListView listView;

    private ListParams params = new ListParams(Enums.Order.Title, true);


    private int requestCode;
    private int grantResults[];

    @Override
    protected void onStart() {
        super.onStart();

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    //initial method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ){
            //if you dont have required permissions ask for it (only required for API 23+)
            ActivityCompat.requestPermissions(this, new String[]{ android.Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);


            onRequestPermissionsResult(requestCode,new String[]{ android.Manifest.permission.WRITE_EXTERNAL_STORAGE},grantResults);
        }

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        headerTitle = (TextView) findViewById(R.id.header_title);
        headerTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (params.getOrder() == Enums.Order.Title){
                    params.asc = !params.asc;
                }
                params.setOrder(Enums.Order.Title);
                updateUI(params);
                sortFirebase(params);
            }
        });

        headerDate = (TextView) findViewById(R.id.header_date);
        headerDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (params.getOrder() == Enums.Order.Date){
                    params.asc = !params.asc;
                }
                params.setOrder(Enums.Order.Date);
                updateUI(params);
                sortFirebase(params);
            }
        });

        headerPriority = (TextView) findViewById(R.id.header_priority);
        headerPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (params.getOrder() == Enums.Order.Priority){
                    params.asc = !params.asc;
                }
                params.setOrder(Enums.Order.Priority);
                updateUI(params);
                sortFirebase(params);
            }
        });

        headerDone = (TextView) findViewById(R.id.header_done);
        headerDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (params.getOrder() == Enums.Order.Done){
                    params.asc = !params.asc;
                }
                params.setOrder(Enums.Order.Done);
                updateUI(params);
                sortFirebase(params);
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
                    updateListView(dataSnapshot, params);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    updateListView(dataSnapshot, params);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    updateListView(dataSnapshot, params);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    updateListView(dataSnapshot, params);
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

    //permissions for access to external storage
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    showAlertMessage( "Permission denied to read your External storage", "Error!");

                    //app cannot function without this permission for now so close it...
                    onDestroy();
                }
                return;
            }
        }
    }

    //extra method to change sorting for listview
    private void sortFirebase(final ListParams params){
        mDatabase.child("users").child(mUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateListView(dataSnapshot, params);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateListView(dataSnapshot, params);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                updateListView(dataSnapshot, params);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                updateListView(dataSnapshot, params);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (databaseError != null){
                    showAlertMessage(databaseError.getMessage(), "Error!");
                }
            }
        });
    }

    //creating top right menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //listener to clicked item from top right menu
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

        boolean exportItemClicked = item.getItemId() == R.id.action_export;
        if (exportItemClicked){
            exportList(items);
        }

        return super.onOptionsItemSelected(item);
    }

    //export list to file
    private void exportList(List<Item> items) {

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {

                Gson gson = new Gson();
                JsonElement element = gson.toJsonTree(items, new TypeToken<List<Item>>() {}.getType());

                if (! element.isJsonArray()) {
                    showAlertMessage("Problem with saving file", "Error!");
                }
                else {
                    JsonArray jsonArray = element.getAsJsonArray();
                    Writer output = null;
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "todolist.json");
                    output = new BufferedWriter(new FileWriter(file));

                    output.write(jsonArray.toString());
                    output.close();
                    showAlertMessage("JSON file has been written", "Success!");
                }
            } catch(Exception ex) {
                showAlertMessage("JSON didn't write", "Error!");
            }
        }
    }

    //load intent to login
    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //load intent to add new item
    private void loadAddItemView(){
        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
        startActivity(intent);
    }

    //edit record from list
    private void editRecord(Item item){
        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    //delete items from list
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

    //update listview, update sorting, show alarm
    private void updateListView(DataSnapshot dataSnapshot, ListParams params){
        items.clear();
        for (DataSnapshot itemsSnapshot: dataSnapshot.getChildren()){
            Item item = itemsSnapshot.getValue(Item.class);
            item.key = itemsSnapshot.getKey();
            items.add(item);
        }

        if (params.getOrder() == Enums.Order.Title){
            Collections.sort(items, new Comparators.ItemTitleComparator());
        }
        else if (params.getOrder() == Enums.Order.Date){
            Collections.sort(items, new Comparators.ItemDateComparator());
        }
        else if (params.getOrder() == Enums.Order.Priority){
            Collections.sort(items, new Comparators.ItemPriorityComparator());
        }
        else {
            Collections.sort(items, new Comparators.ItemDoneComparator());
        }

        if (!params.getAsc())
            Collections.reverse(items);

        listView.setAdapter(new ListAdapter(getApplicationContext(), items));

        notificationManager.cancelAll();
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        if (activeNotifications.length == 0) {
            int i = 0;
            for (Item item : items
                    ) {
                long date = new Date().getTime();
                if (date >= item.getDate()) {
                    AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(getApplicationContext(), OnAlarmReceiver.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("item", item);
                    intent.putExtra("bundle", bundle);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 100, pendingIntent);
                    i++;
                }
            }
        }
    }

    //Update header row
    private void updateUI (ListParams params){
        if (params.getOrder() == Enums.Order.Title) {
            if (params.asc)
                headerTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            else
                headerTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);

            headerDate.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerPriority.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerDone.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }
        else if (params.getOrder() == Enums.Order.Date) {
            if (params.asc)
                headerDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            else
                headerDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);

            headerTitle.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerPriority.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerDone.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }
        else if (params.getOrder() == Enums.Order.Priority) {
            if (params.asc)
                headerPriority.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            else
                headerPriority.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);

            headerDate.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerTitle.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerDone.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }
        else {
            if (params.asc)
                headerDone.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
            else
                headerDone.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);

            headerDate.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerPriority.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            headerTitle.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }
    }

    //method to show alert messages
    private void showAlertMessage(String message, String title){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }


}
