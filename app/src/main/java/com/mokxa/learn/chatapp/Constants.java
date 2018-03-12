package com.mokxa.learn.chatapp;

/**
 * Created by enterprise on 3/8/18.
 */

public class Constants {
    //public static final String PREFERENCES_LOCATION_KEY = "location";
    //public static final String YELP_LOCATION_QUERY_PARAMETER = "location";
    public static final String FIREBASE_CHILD_USERS = "Users";
    public static final String USERS_CHILD_DEVICE_TOKEN = "Device_Token";

    public static final String FIREBASE_CHILD_FRIEND_REQUESTS = "Friend_req";
    public static final String FIREBASE_CHILD_FRIENDS = "Friends";
    public static final String FIREBASE_CHILD_NOTIFICATIONS = "Notifications";




    public static final String FIREBASE_CHILD_REQUEST_TYPE = "request_type";
    public static final String FIREBASE_VALUE_REQUEST_TYPE_SENT = "sent";
    public static final String FIREBASE_VALUE_REQUEST_TYPE_RECIEVED = "recieved";

    public static final int CURRENT_STATE_NOT_FRIENDS = 0;
    public static final int CURRENT_STATE_REQEST_SENT = 1;
    public static final int CURRENT_STATE_REQEST_RECEIVED = 2;
    public static final int CURRENT_STATE_REQEST_DECLINED = 3;
    public static final int CURRENT_STATE_FRIENDS = 4;
}
