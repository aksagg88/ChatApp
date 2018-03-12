package com.mokxa.learn.chatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by enterprise on 3/7/18.
 * Reference: https://www.learnhowtoprogram.com/android/data-persistence/firebase-recycleradapter
 */



public class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private static String TAG = "UsersViewHolder";
    View mView;
    Context mContext;

    private static final int MAX_WIDTH = 64;
    private static final int MAX_HEIGHT = 64;
    private int mPosition;

    public UsersViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();//get context fromt the passed itemview
        itemView.setOnClickListener(this);
    }

    public void bindUser(Users user, int position) {

        TextView mUserDisplayName = mView.findViewById(R.id.tv_users_row_display_name);
        TextView mUserStatus = mView.findViewById(R.id.tv_users_row_status);
        CircleImageView mDisplayImg = mView.findViewById(R.id.im_users_row);


        mUserDisplayName.setText(user.getName());
        mUserStatus.setText(user.getStatus());

        Picasso.with(mContext)
                .load(user.getThumbImage())
                .placeholder(R.mipmap.default_avatar)
                .resize(MAX_WIDTH,MAX_HEIGHT)
                .centerCrop()
                .into(mDisplayImg);

        mPosition = position;
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "Click!");

        //final ArrayList<Users> users_list = new ArrayList<>();
        final ArrayList<String> key_list = new ArrayList<>();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //users_list.add(snapshot.getValue(Users.class));
                    key_list.add(snapshot.getKey());
                    //Log.d(TAG+"onDataChangeForLoof","snapshot.getKey(): "+snapshot.getKey());
                }
                int itemPosition = getLayoutPosition();
                Log.d(TAG+"onDataChange", "Click Position: "+itemPosition);
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra("position", itemPosition + "");
                //profileIntent.putExtra("users_list",users_list);
                profileIntent.putExtra("key_list", key_list);
                mContext.startActivity(profileIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,"Database Error", Toast.LENGTH_LONG).show();
            }
        });



    }
}

