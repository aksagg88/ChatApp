package com.mokxa.learn.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private String TAG = "SettingsActiity";

    private DatabaseReference mUserDatabse;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef, mProfileImageStorageRef, mThumbStorageRef;

    //UI
    private CircleImageView mDisplayImage;
    private TextView mDisplayName, mDisplayStatus;
    private Button mChangeStatus, mChangeImage;
    private ProgressDialog mImageUpdateProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = (CircleImageView)findViewById(R.id.img_settings_display_img);
        mDisplayName = (TextView)findViewById(R.id.tv_settings_display_name);
        mDisplayStatus = (TextView)findViewById(R.id.tv_settings_status);
        mChangeImage = (Button)findViewById(R.id.b_settings_change_image);
        mChangeStatus = (Button)findViewById(R.id.b_settings_change_status);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();
        mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mUserDatabse.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //add value event listener
        mUserDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString());
                String name = dataSnapshot.child("name").getValue(String.class);
                String image_url = dataSnapshot.child("image").getValue(String.class);
                String status = dataSnapshot.child("status").getValue(String.class);
                String thumb_url = dataSnapshot.child("thumb_img").getValue(String.class);
                Log.d(TAG, "Name: "+ name+" Status: "+status);

                mDisplayName.setText(name);
                mDisplayStatus.setText(status);

                //set display image
                if(!image_url.equals("default")){
                    Picasso.with(SettingsActivity.this).load(image_url).placeholder(R.mipmap.default_avatar).into(mDisplayImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handling errors


            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String statusValue = mDisplayStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value",statusValue);
                startActivity(statusIntent);
            }
        });

        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //the document picker

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT DISPLAY IMAGE"), GALLERY_PICK);


                //using the picker from the crop image library
                // start picker to get image for cropping and then use the image in cropping activity
                /*
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
                */
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult resultCode: " + resultCode + " requestCode: " + requestCode);


        //Gallery Image Picker
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            //get URI of the image
            //CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri imageUri = data.getData();
            Log.d(TAG, "image Uri: " + imageUri);

            //crop image
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        //Start the crop activity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mImageUpdateProgress = new ProgressDialog(SettingsActivity.this);
                mImageUpdateProgress.setTitle("Uploading Image");
                mImageUpdateProgress.setMessage("Please wait while we save the image to your profile..");
                mImageUpdateProgress.show();

                Uri resultUri = result.getUri();
                File thumb_file_path = new File(resultUri.getPath());

                String currentUid = mCurrentUser.getUid();
                String Imagefolder = "profile_images";

                //Bitmap conversion
                byte[] thumb_byte = null;
                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file_path);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                mProfileImageStorageRef = mStorageRef.child(Imagefolder).child(currentUid + ".jpg");
                mThumbStorageRef = mProfileImageStorageRef.child("thumbs").child(currentUid + ".jpg");


                final byte[] finalThumb_byte = thumb_byte;
                mProfileImageStorageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Image uploaded");
                            //IF sucessfull, then add the image URL to the database
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = mThumbStorageRef.putBytes(finalThumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    final String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful()){

                                        Map updateHashMap = new HashMap<>();
                                        updateHashMap.put("image", download_url);
                                        updateHashMap.put("thumb_image", thumb_download_url);

                                        mUserDatabse.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mImageUpdateProgress.dismiss();

                                                Toast.makeText(SettingsActivity.this, "Image and Thumb Uploaded", Toast.LENGTH_LONG).show();
                                            }
                                        });


                                    } else {
                                        mImageUpdateProgress.dismiss();
                                        Log.d(TAG, "Error in Uploading Image");
                                    }


                                }
                            });


                        } else {
                            mImageUpdateProgress.dismiss();
                            Log.d(TAG, "Error in Uploading Image");
                        }
                    }

                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(TAG, "Error in cropping image");
            }
        }
    }


    //END OF SETTINGS CLASS

    public void showProgressDialog(Context context, String title, String msg, boolean show){
        ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(msg);
        if(show) {
            mProgressDialog.show();
        }else {
            mProgressDialog.dismiss();
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
