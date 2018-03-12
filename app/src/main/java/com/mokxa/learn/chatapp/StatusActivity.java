package com.mokxa.learn.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatusInput;
    private Button mSaveStatus;
    private ProgressDialog mStatusProgress;

    private DatabaseReference mUserDatabaseReference;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCurrentUser.getUid();

        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        mToolbar = findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatusProgress = new ProgressDialog(this);

        String status_value = getIntent().getStringExtra("status_value");

        mStatusInput = findViewById(R.id.et_status_input);
        mStatusInput.getEditText().setText(status_value);

        mSaveStatus = findViewById(R.id.b_status_save);

        mSaveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mStatusProgress.setTitle("Saving Changes");
                mStatusProgress.setMessage("Please wait while we save the changes..");
                mStatusProgress.show();

                String user_status = mStatusInput.getEditText().getText().toString();
                mUserDatabaseReference.child("status").setValue(user_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProgress.dismiss();

                            Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            finish();

                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Error in saving changes.. try again", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

    }
}
