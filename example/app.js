var core = require('firebase.core');
var fcm = require('firebase.cloudmessaging');
var isAndroid = Ti.Platform.osname === 'android';

// Configure core module (required for all Firebase modules)
core.configure();

// Called when the Firebase token is ready
fcm.addEventListener('didRefreshRegistrationToken', onToken);

// Called when direct messages arrive. Note that these are different from push notifications
fcm.addEventListener('didReceiveMessage', onMessage);

fcm.registerForPushNotifications();

function onToken(e) {
    Ti.API.info('Token', e.fcmToken);
}

function onMessage(e) {
    Ti.API.info('Message', e.message);
}

// Android: For configuring custom sounds and importance for the generated system 
// notifications when app is in the background
isAndroid && fcm.createNotificationChannel({
  sound: 'warn_sound',
  channelId: 'general',
  channelName: 'General Notifications',
  importance: 'high' //will pop in from the top and make a sound
});

// check if token is already available
if (fcm.fcmToken !== null) {
    Ti.API.info('FCM-Token', fcm.fcmToken);
} else {
    Ti.API.info('Token is empty. Waiting for the token callback ...');
}

// subscribe to topic
fcm.subscribeToTopic('testTopic');
