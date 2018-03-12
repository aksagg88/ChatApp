package com.mokxa.learn.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mokxa.learn.chatapp.Constants.CURRENT_STATE_FRIENDS;
import static com.mokxa.learn.chatapp.Constants.CURRENT_STATE_REQEST_RECEIVED;
import static com.mokxa.learn.chatapp.Constants.CURRENT_STATE_REQEST_SENT;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_CHILD_FRIENDS;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_CHILD_FRIEND_REQUESTS;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_CHILD_NOTIFICATIONS;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_CHILD_REQUEST_TYPE;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_CHILD_USERS;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_VALUE_REQUEST_TYPE_RECIEVED;
import static com.mokxa.learn.chatapp.Constants.FIREBASE_VALUE_REQUEST_TYPE_SENT;

public class ProfileActivity extends AppCompatActivity {

    private static String TAG = "ProfileActivity";
    ImageView mProfileImage;
    TextView mDisplayNameTv, mProfileStatusTv;
    Button mProfileSendReqBtn, mProfileDeclineReqBtn;
    ProgressDialog mProfileLoadProgress;

    private Users mUser;
    private ArrayList<String> mKeyList = new ArrayList<>();
    private String mUserUid, mProfileName;
    private int mCurrentstate;
    private int mPostion;
    private DatabaseReference mRootDatabaseRef, mUserDatabaseRef, mFriendReqDatabaseRef, mFriendsDatabaseRef, mNotificationsDatabaseRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUserUid = getUidFromIntent();

        mRootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FIREBASE_CHILD_USERS).child(mUserUid);
        mFriendReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FIREBASE_CHILD_FRIEND_REQUESTS);
        mFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FIREBASE_CHILD_FRIENDS);
        mNotificationsDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FIREBASE_CHILD_NOTIFICATIONS);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mProfileImage = (ImageView) findViewById(R.id.im_profile_image);
        mDisplayNameTv = (TextView) findViewById(R.id.tv_profile_display_name);
        //mUid = (TextView) findViewById(R.id.tv_profile_uid);
        mProfileStatusTv = (TextView) findViewById(R.id.tv_profile_status);
        mProfileSendReqBtn = (Button) findViewById(R.id.b_profile_send_request);
        mProfileDeclineReqBtn = (Button) findViewById(R.id.b_profile_decline_request);

        // this button only should be seen when a request has been sent
        mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineReqBtn.setEnabled(false);

        mCurrentstate = Constants.CURRENT_STATE_NOT_FRIENDS;

        if(mUserUid.equals(mCurrentUser.getUid())){
            mProfileSendReqBtn.setVisibility(View.INVISIBLE);
        }

        mProfileLoadProgress = new ProgressDialog(this);
        mProfileLoadProgress.setTitle("Loading User Data");
        mProfileLoadProgress.setMessage("Please wait while we load this awesome information");
        mProfileLoadProgress.setCanceledOnTouchOutside(false);
        mProfileLoadProgress.show();

        mUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(Users.class);
                mDisplayNameTv.setText(mUser.getName());
                mProfileStatusTv.setText(mUser.getStatus());
                Picasso.with(ProfileActivity.this).load(mUser.getImage()).placeholder(R.mipmap.default_avatar).into(mProfileImage);

                //------------ FRIENDS LIST / REQUEST FEATURE
                mFriendReqDatabaseRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(mUserUid)){
                            String req_type = dataSnapshot.child(mUserUid).child(FIREBASE_CHILD_REQUEST_TYPE).getValue().toString();

                            if(req_type.equals(FIREBASE_VALUE_REQUEST_TYPE_RECIEVED)){
                                // uUserUid has send us a request
                                mCurrentstate = CURRENT_STATE_REQEST_RECEIVED;
                                mProfileSendReqBtn.setText(R.string.profile_b_accept_friend_request);

                                mProfileDeclineReqBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineReqBtn.setEnabled(true);

                            } else if(req_type.equals(FIREBASE_VALUE_REQUEST_TYPE_SENT)){
                                mCurrentstate = CURRENT_STATE_REQEST_SENT;
                                mProfileSendReqBtn.setText(R.string.profile_b_cancel_friend_request);
                            }

                        } else {
                            // if the current user is not in the request database, they might already be friends
                            mFriendsDatabaseRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(mUserUid)){
                                        //the current user is a friend
                                        mCurrentstate = CURRENT_STATE_FRIENDS;
                                        mProfileSendReqBtn.setText(R.string.profile_b_unfriend);
                                        mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        mProfileLoadProgress.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProfileLoadProgress.dismiss();
                Toast.makeText(ProfileActivity.this, "Error in connecting to databse", Toast.LENGTH_LONG).show();
            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);// disable while the querry is being processed

                // --------------- NOT FRIENDS REQUEST STATE-------------------

                if(mCurrentstate == Constants.CURRENT_STATE_NOT_FRIENDS)
                {
                    Map requestMap = createSendFriendRequestMap(mCurrentUser.getUid(), mUserUid);

                    mRootDatabaseRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                //if error
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //Success
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentstate = CURRENT_STATE_REQEST_SENT;
                                mProfileSendReqBtn.setText(R.string.profile_b_cancel_friend_request);
                                mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccentOriginal));

                                Toast.makeText(ProfileActivity.this, "request sent", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    /*
                    mFriendReqDatabaseRef.child(mCurrentUser.getUid()).child(mUserUid).child(FIREBASE_CHILD_REQUEST_TYPE)
                            .setValue(FIREBASE_VALUE_REQUEST_TYPE_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                            // request sent => add request recieved for the other user.
                                mFriendReqDatabaseRef.child(mUserUid).child(mCurrentUser.getUid()).child(FIREBASE_CHILD_REQUEST_TYPE)
                                        .setValue(Constants.FIREBASE_VALUE_REQUEST_TYPE_RECIEVED).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        final HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from",mCurrentUser.getUid());
                                        notificationData.put("type","request");

                                        mNotificationsDatabaseRef.child(mUserUid).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.d(TAG, "Notification added"+notificationData.toString());
                                                } else {
                                                    Toast.makeText(ProfileActivity.this, "Unable to add notification", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                        mProfileSendReqBtn.setEnabled(true);
                                        mCurrentstate = CURRENT_STATE_REQEST_SENT;
                                        mProfileSendReqBtn.setText(R.string.profile_b_cancel_friend_request);
                                        mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccentOriginal));

                                        Toast.makeText(ProfileActivity.this, "request sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Unable to send request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/
                }

                //--------------- REQUEST SENT REQUEST STATE-------------------
                if(mCurrentstate == CURRENT_STATE_REQEST_SENT){

                    Map requestMap = createCancelFriendRequestMap(mCurrentUser.getUid(), mUserUid);
                    mRootDatabaseRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null) {
                                //if error
                                Toast.makeText(ProfileActivity.this, "Unable to cancel friend request", Toast.LENGTH_LONG).show();
                            }
                            else {
                                //Success
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentstate = Constants.CURRENT_STATE_NOT_FRIENDS;
                                mProfileSendReqBtn.setText(R.string.profile_b_send_friend_request);
                                mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccent));

                                Toast.makeText(ProfileActivity.this, "Friend request canceled", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    /*
                    mFriendReqDatabaseRef.child(mCurrentUser.getUid()).child(mUserUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendReqDatabaseRef.child(mUserUid).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrentstate = Constants.CURRENT_STATE_NOT_FRIENDS;
                                            mProfileSendReqBtn.setText(R.string.profile_b_send_friend_request);
                                            mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccent));

                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Unable to cancel friend request", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(ProfileActivity.this, "Unable to cancel friend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/

                }

                //--------------- REQUEST RECEIVED STATE-------------------
                if(mCurrentstate== CURRENT_STATE_REQEST_RECEIVED){

                    Map requestMap = createAcceptFriendRequestMap(mCurrentUser.getUid(), mUserUid);
                    mRootDatabaseRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(ProfileActivity.this, "Unable to accept friend request", Toast.LENGTH_LONG).show();
                            }else {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentstate = CURRENT_STATE_FRIENDS;
                                mProfileSendReqBtn.setText(R.string.profile_b_unfriend);
                                mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccent));

                            }
                        }
                    });
                    /*
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendsDatabaseRef.child(mCurrentUser.getUid()).child(mUserUid).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendsDatabaseRef.child(mUserUid).child(mCurrentUser.getUid()).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            //delete the request once it is accepted
                                            mFriendReqDatabaseRef.child(mCurrentUser.getUid()).child(mUserUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){

                                                        mFriendReqDatabaseRef.child(mUserUid).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                    mCurrentstate = CURRENT_STATE_FRIENDS;
                                                                    mProfileSendReqBtn.setText(R.string.profile_b_unfriend);
                                                                    mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccent));

                                                                } else {
                                                                    Toast.makeText(ProfileActivity.this, "Unable to cancel friend request", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        Toast.makeText(ProfileActivity.this, "Unable to cancel friend request", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        }else {
                                            Toast.makeText(ProfileActivity.this, "Unable to accept friend request", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                });

                            } else {
                                Toast.makeText(ProfileActivity.this, "Unable to accept friend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/

                }

                ////--------------- FRIENDS STATE-------------------
                if(mCurrentstate == CURRENT_STATE_FRIENDS){
                    Map requestMap = createRemoveFriendRequestMap(mCurrentUser.getUid(), mUserUid);
                    mRootDatabaseRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(ProfileActivity.this, "Unable to unfriend request", Toast.LENGTH_LONG).show();
                            } else {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentstate = Constants.CURRENT_STATE_NOT_FRIENDS;
                                mProfileSendReqBtn.setText(R.string.profile_b_send_friend_request);
                                mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccent));

                            }
                        }
                    });
                    /*
                    mFriendsDatabaseRef.child(mCurrentUser.getUid()).child(mUserUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendsDatabaseRef.child(mUserUid).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrentstate = Constants.CURRENT_STATE_NOT_FRIENDS;
                                            mProfileSendReqBtn.setText(R.string.profile_b_send_friend_request);
                                            mProfileSendReqBtn.setBackgroundColor(mProfileSendReqBtn.getContext().getResources().getColor(R.color.colorAccent));

                                        }else {
                                            Toast.makeText(ProfileActivity.this, "Unable to unfriend request", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }else {
                                Toast.makeText(ProfileActivity.this, "Unable to unfriend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/

                }

            }
        });

        mProfileDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




    }

    private Map createRemoveFriendRequestMap(String senderID, String receiverID) {
        Map requestMap = new HashMap();
        requestMap.put(FIREBASE_CHILD_FRIENDS + "/" + senderID+ "/" +receiverID ,null);
        requestMap.put(FIREBASE_CHILD_FRIENDS + "/" + receiverID+ "/" + senderID, null);
        return requestMap;
    }

    private Map createAcceptFriendRequestMap(String senderID, String receiverID) {
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        DatabaseReference newNotificationref = mRootDatabaseRef.child(FIREBASE_CHILD_NOTIFICATIONS).child(receiverID).push();
        String newNotificationId = newNotificationref.getKey();

        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("from", receiverID);
        notificationData.put("type", "Accept");

        Map requestMap = new HashMap();
        requestMap.put(FIREBASE_CHILD_FRIENDS + "/" + senderID+ "/" +receiverID ,currentDate);
        requestMap.put(FIREBASE_CHILD_FRIENDS + "/" + receiverID+ "/" + senderID, currentDate);
        requestMap.put(FIREBASE_CHILD_FRIEND_REQUESTS + "/" + senderID+ "/" +receiverID, null);
        requestMap.put(FIREBASE_CHILD_FRIEND_REQUESTS + "/" + receiverID+ "/" + senderID,null);
        requestMap.put(FIREBASE_CHILD_NOTIFICATIONS+"/" + senderID + "/" + newNotificationId, notificationData);
        return requestMap;
    }

    private Map createCancelFriendRequestMap(String senderID, String receiverID) {
        Map requestMap = new HashMap();
        requestMap.put(FIREBASE_CHILD_FRIEND_REQUESTS + "/" + senderID+ "/" +receiverID, null);
        requestMap.put(FIREBASE_CHILD_FRIEND_REQUESTS + "/" + receiverID+ "/" + senderID,null);
        return requestMap;
    }

    private Map createSendFriendRequestMap(String senderID, String receiverID) {

        DatabaseReference newNotificationref = mRootDatabaseRef.child(FIREBASE_CHILD_NOTIFICATIONS).child(receiverID).push();
        String newNotificationId = newNotificationref.getKey();

        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("from", senderID);
        notificationData.put("type", "request");


        Map requestMap = new HashMap();

        requestMap.put(FIREBASE_CHILD_FRIEND_REQUESTS + "/" + senderID+ "/" +receiverID +"/"+ FIREBASE_CHILD_REQUEST_TYPE, FIREBASE_VALUE_REQUEST_TYPE_SENT);
        requestMap.put(FIREBASE_CHILD_FRIEND_REQUESTS + "/" + receiverID+ "/" + senderID+"/"+ FIREBASE_CHILD_REQUEST_TYPE, FIREBASE_VALUE_REQUEST_TYPE_RECIEVED);
        requestMap.put(FIREBASE_CHILD_NOTIFICATIONS+"/" + receiverID + "/" + newNotificationId, notificationData);

        return requestMap;
    }



    private String getUidFromIntent() {

        String UserUid = "";

        if(getIntent().getExtras() != null){
            Log.d(TAG, "intent get extras: "+getIntent().getExtras().keySet().toString());
            for(String key : getIntent().getExtras().keySet()){
                if(key.equals("key_list")){
                    mKeyList = getIntent().getStringArrayListExtra(key);
                }
                if (key.equals("position")){
                    mPostion = Integer.parseInt(getIntent().getStringExtra(key));
                }
                if (key.equals("user_id")){
                    UserUid = getIntent().getStringExtra(key);
                }

            }
        }
        //mKeyList = getIntent().getStringArrayListExtra("key_list");
        //mPostion = Integer.parseInt(getIntent().getStringExtra("position"));
        if(UserUid.equals("")) {
            UserUid = mKeyList.get(mPostion);
        }
        return UserUid;
    }


}
