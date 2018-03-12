package com.mokxa.learn.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.TestLooperManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "RegisterActivity";

    private TextInputLayout mDisplayName, mEmail, mPassword;
    private Button mCreateButton;

    //Firebase Auth
    private FirebaseAuth mAuth;

    //Firebase Database
    private DatabaseReference mDatabasereference;

    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //firebase get instance
        mAuth = FirebaseAuth.getInstance();

        mDisplayName = findViewById(R.id.et_reg_display_name);
        mEmail = findViewById(R.id.et_reg_email);
        mPassword = findViewById(R.id.et_reg_password);
        mCreateButton = findViewById(R.id.b_reg_create);
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);


        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: may check for log-in

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name)| !TextUtils.isEmpty(email)|!TextUtils.isEmpty(password)){

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false); //prevent touching to exit
                    mRegProgress.show();
                    register_user(display_name, email, password);
                }


            }
        });
    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String uid = currentUser.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mDatabasereference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            //Create hashmap
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name",display_name);
                            userMap.put("status", getResources().getString(R.string.status_settings));
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put(Constants.USERS_CHILD_DEVICE_TOKEN,deviceToken);

                            //push hashmap object and set oncomplete listener
                            mDatabasereference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // if success

                                    if(task.isSuccessful()){
                                        mRegProgress.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //go to home after back button
                                        startActivity(mainIntent);
                                        finish(); //on back button press close this activity
                                    }
                                    else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();

                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }
}
