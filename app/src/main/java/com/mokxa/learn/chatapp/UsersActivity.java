package com.mokxa.learn.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private String TAG = "UserActivity";

    private Toolbar mUsersToolbar;

    private RecyclerView mUsersRecyclerView;
    private DatabaseReference mUsersReference;
    private FirebaseRecyclerAdapter mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUsersToolbar = findViewById(R.id.usersAppbar);
        setSupportActionBar(mUsersToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersRecyclerView = (RecyclerView)findViewById(R.id.rv_users);
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setUpFirebaseAdapter();
        mFirebaseAdapter.startListening();
        mUsersRecyclerView.setAdapter(mFirebaseAdapter);
    }


    private void setUpFirebaseAdapter() {

        Query query = mUsersReference.limitToLast(50);
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class)
                .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row_layout, parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int position, @NonNull Users users) {
                Log.d(TAG, "Binding user: "+users.getName()+ " at position: "+position+" thumb: "+users.getThumbImage());
                usersViewHolder.bindUser(users,position);
           }

        };

    }


    //END OF THE USERS CLASS
}