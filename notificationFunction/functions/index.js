'use strict'
//https://github.com/firebase/functions-samples/blob/master/fcm-notifications/functions/index.js

//import firebase functions modules
const functions = require('firebase-functions');

//import admin module
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}').onWrite(event => {
  //like add value listener. The Realtime Database supports the onWrite() event, which triggers anytime data is created, // destroyed, or changed in a specified database location.



    const user_id = event.params.user_id;
    const notification_id = event.params.notification_id;


    console.log('We have a notification to send to  :',user_id);

  //avoid sending notification on unrelated changes
    if(!event.data.val()){
        return console.log('A Notification has been deleted from the database : ', notification_id);
    }

    const fromUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');

    return fromUser.then(fromUserResult => {

        const from_user_id = fromUserResult.val().from;
        const request_type = fromUserResult.val().type;

        console.log('You have new notification from : ', from_user_id);

        const userQuerry  = admin.database().ref(`Users/${user_id}/name`).once('value');
        return userQuerry.then(userResult => {

            const userName = userResult.val();
            const deviceToken = admin.database().ref(`/Users/${user_id}/Device_Token`).once('value');

            return deviceToken.then(result => {

                const token_id = result.val();

                const payload = {
                  notification: {
                    title : "Friend Request",
                    body: `${userName} has sent you a friend request`,
                    icon: "default",
                    click_action: "com.mokxa.learn.chatapp_TARGET_NOTIFICATION"
                  },
                  data : {
                    from_user_id : from_user_id
                  }
                };

                return admin.messaging().sendToDevice(token_id, payload).then(response =>{

                  return console.log('This was the notification Feature');
                });

            });


        });


    });

});




// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
