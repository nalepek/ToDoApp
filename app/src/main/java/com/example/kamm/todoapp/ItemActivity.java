package com.example.kamm.todoapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ItemActivity extends AppCompatActivity {

    long convertedDate;
    Button addButton;
    EditText titleText,
             dateText,
             priorityText;
    CheckBox done;
    Calendar myCalendar;
    Toolbar toolbar;
    private boolean editMode = false;


    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        // Initialize Firebase Auth and Database Reference and get userId
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserId = mFirebaseUser.getUid();

        //Initialize toolbar and button and fields
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addButton = (Button) findViewById(R.id.add_item_btn);

        titleText = (EditText) findViewById(R.id.item_title);
        done = (CheckBox) findViewById(R.id.item_checked);
        myCalendar = Calendar.getInstance();
        dateText = (EditText) findViewById(R.id.item_date);
        priorityText = (EditText) findViewById(R.id.item_priority);

        //get values from mainActivity intent
        Intent i = getIntent();
        final Item intentItem = (Item)i.getSerializableExtra("item");

        if (intentItem != null){
            editMode = true;

            addButton.setText("Update item");
            done.setChecked(intentItem.getDone());
            titleText.setText(intentItem.getTitle());
            priorityText.setText(intentItem.getPriority());

            String dateString = DateFormat.format("MM/dd/yyyy", new Date(intentItem.getDate())).toString();
            dateText.setText(dateString);
        }

        //listener for datepicker
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "MM/dd/yy"; //In which you need put here
                convertedDate = myCalendar.getTimeInMillis();

                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                dateText.setText(sdf.format(myCalendar.getTime()));
            }
        };


        dateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(ItemActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //addButton item listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int priority = 0;

                try {
                    priority = Integer.parseInt(priorityText.getText().toString());
                    Item item = new Item(
                            titleText.getText().toString(),
                            convertedDate,
                            done.isChecked(),
                            priority
                            );

                    if (editMode) {
                        mDatabase.child("users").child(mUserId).child("items").child(intentItem.getKey()).setValue(item);
                    }
                    else {
                        mDatabase.child("users").child(mUserId).child("items").push().setValue(item);
                    }

                    loadMainView();

                } catch(NumberFormatException nfe) {

                    showAlertMessage("Could not parse" + nfe, "Invalid input in priority");
                }
            }
        });
    }

    private void loadMainView(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showAlertMessage(String message, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
