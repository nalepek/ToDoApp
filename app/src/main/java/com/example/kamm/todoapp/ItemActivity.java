package com.example.kamm.todoapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ItemActivity extends AppCompatActivity {

    long convertedDate;
    Button addButton,
            imageButton;
    EditText titleText,
             dateText,
             priorityText;
    CheckBox done;
    Calendar myCalendar;
    Toolbar toolbar;
    private ImageView imageView;
    private boolean editMode = false;

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

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
        imageButton = (Button) findViewById(R.id.edit_item_image_btn);

        titleText = (EditText) findViewById(R.id.item_title);
        done = (CheckBox) findViewById(R.id.item_checked);
        myCalendar = Calendar.getInstance();
        dateText = (EditText) findViewById(R.id.item_date);
        priorityText = (EditText) findViewById(R.id.item_priority);

        imageView = (ImageView) findViewById(R.id.item_image);

        //get values from mainActivity intent
        Intent i = getIntent();
        final Item intentItem = (Item)i.getSerializableExtra("item");

        if (intentItem != null){
            editMode = true;

            addButton.setText(R.string.update_item_btn_text);
            done.setChecked(intentItem.getDone());
            titleText.setText(intentItem.getTitle());

            String priority = Integer.toString(intentItem.getPriority());
            priorityText.setText(priority);

            if (intentItem.getDate() != 0){
                String dateString = DateFormat.format("MM/dd/yyyy", new Date(intentItem.getDate())).toString();
                dateText.setText(dateString);
            }

            String img = intentItem.getImage();
            if (img != null) {
                imageButton.setText(R.string.update_image_btn_text);
                Bitmap image = null;
                try {
                    image = decodeFromFirebaseBase64(intentItem.getImage());
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlertMessage( "Something went wrong", "Error!");
                }
                imageView.setImageBitmap(image);
            }
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

                    if (imageView.getDrawable() != null) {
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        item.setImage(encodeBitmapImage(bitmap));
                    }

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

        //actions for clicked on image
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImageDialog();
            }
        });
    }

    //image dialog on click
    private void loadImageDialog() {
        if (imageView.getDrawable() == null) {
            takePictureOrSelect();
        }
        else {
            final CharSequence[] items = {"Edit", "Delete"};

            android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(ItemActivity.this);
            adb.setTitle("Image")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (item == 0) {
                                takePictureOrSelect();
                            } else if (item == 1) {
                                deleteImage();
                            }
                        }
                    }).show();
        }
    }

    //take picture or select from gallery
    private void takePictureOrSelect() {
        final CharSequence[] items = { "Take photo", "Select photo from gallery" };

        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(ItemActivity.this);
        adb.setTitle("Item photo")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            takePicture();
                        }
                        else if (item == 1){
                            selectPicFromGallery();
                        }
                    }
                }).show();
    }

    //select pic from gallery
    private void selectPicFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    //take the picture
    private void takePicture() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, 0);//zero can be replaced with any action code
    }

    //take or choose photo
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    imageView.setImageBitmap(photo);
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(selectedImage);
                        imageButton.setText(R.string.update_image_btn_text);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        showAlertMessage( "Something went wrong", "Error!");
                    }
                }
                break;
        }
    }

    //delete image
    private void deleteImage() {
        imageView.setImageDrawable(null);
        imageButton.setText(R.string.add_image_btn_text);
    }

    private void loadMainView(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //dialog for alerts
    private void showAlertMessage(String message, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //convert image bitmap to base64 to save to Firebase
    private String encodeBitmapImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }

    //retrieve image from Firebase and convert to bitmap
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
