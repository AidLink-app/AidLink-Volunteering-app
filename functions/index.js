const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Cloud Function triggered when a post is updated
exports.sendApprovalNotification = functions.firestore
  .document('posts/{postId}')
  .onUpdate((change, context) => {
    const beforeData = change.before.data();
    const afterData = change.after.data();

    // Check if approvedUsers changed (i.e. new approvals)
    const beforeApproved = beforeData.approvedUsers || [];
    const afterApproved = afterData.approvedUsers || [];

    // Identify newly approved users
    const newApprovals = afterApproved.filter(user => !beforeApproved.includes(user));

    // For each new approval, send a notification
    newApprovals.forEach(userId => {
      admin.firestore().collection('users').doc(userId).get()
        .then(doc => {
          if (doc.exists) {
            const token = doc.data().fcmToken;
            if (token) {
              const payload = {
                notification: {
                  title: 'Approval Notification',
                  body: 'Your volunteer application has been approved!',
                }
              };

              return admin.messaging().sendToDevice(token, payload);
            } else {
              console.log(`No token for user ${userId}`);
            }
          }
        })
        .then(response => {
          console.log(`Notification sent to ${userId}:`, response);
        })
        .catch(err => {
          console.error(`Error sending notification to ${userId}:`, err);
        });
    });

    return null;
  });
