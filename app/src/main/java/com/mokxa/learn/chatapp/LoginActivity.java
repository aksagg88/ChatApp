package com.mokxa.learn.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.TooltipCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private android.support.v7.widget.Toolbar mToolbar;

    private TextInputLayout mLoginEmail, mLoginPassword;
    private Button mLoginBtn;
    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS);

        mLoginProgress = new ProgressDialog(this);

        mLoginEmail = (TextInputLayout) findViewById(R.id.et_login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.et_login_password);
        mLoginBtn = (Button) findViewById(R.id.b_login_login);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email)|!TextUtils.isEmpty(password)){
                    mLoginProgress.setTitle("Logging in");
                    mLoginProgress.setMessage("Please wait while we log you in");
                    mLoginProgress.setCanceledOnTouchOutside(false); //prevent touching to exit
                    mLoginProgress.show();
                    login_user(email, password);
                }

            }
        });


    }

    private void login_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);

                            mLoginProgress.dismiss();

                            //store the token ID of the user for notifications
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            mUserDatabaseRef.child(currentUserId).child(Constants.USERS_CHILD_DEVICE_TOKEN).setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //go to home after back button
                                        startActivity(mainIntent);
                                        finish(); //on back button press close this activity
                                    }
                                    else {
                                        Toast.makeText(LoginActivity.this, "Device token failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });




                        } else {
                            // If sign in fails, display a message to the user.
                            mLoginProgress.hide();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                });
    }
}
