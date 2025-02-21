const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendFCMNotification = functions.https.onCall((data, context) => {
    const message = {
        notification: {
            title: data.title,
            body: data.message
        },
        token: data.fcmToken
    };

    return admin.messaging().send(message)
        .then((response) => {
            return { success: true, response: response };
        })
        .catch((error) => {
            console.error("Error sending FCM message:", error);
            return { success: false, error: error };
        });
});
