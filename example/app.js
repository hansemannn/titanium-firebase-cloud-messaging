var core = require('firebase.core');
var isAndroid = Ti.Platform.osname === 'android';

// Configure core module (required for all Firebase modules)
core.configure();

// Important: Include cloud messaging module after the initial configure()
var fcm = require('firebase.cloudmessaging');

// Called when the Firebase token is ready
fcm.addEventListener('didRefreshRegistrationToken', onToken);

// Called when direct messages arrive. Note that these are different from push notifications
fcm.addEventListener('didReceiveMessage', onMessage);

function onToken(e) {
    Ti.API.info('Token', e.fcmToken);
}

function onMessage(e) {
    Ti.API.info('Message', e.message);
}

// Android: For configuring custom sounds and importance for the generated system
// notifications when app is in the background
if (isAndroid) {
    fcm.createNotificationChannel({
        sound: 'warn_sound',
        channelId: 'general',
        channelName: 'General Notifications',
        importance: 'high' //will pop in from the top and make a sound
    });
    fcm.registerForPushNotifications();

	Ti.API.info("Last data: " + fcm.lastData);
} else {
    Ti.App.iOS.addEventListener('usernotificationsettings', function eventUserNotificationSettings() {
        // Remove the event again to prevent duplicate calls through the Firebase API
        Ti.App.iOS.removeEventListener('usernotificationsettings', eventUserNotificationSettings);

        // Register for push notifications
        Ti.Network.registerForPushNotifications({
            success: function () { },
            error: function () { },
            callback: function () { } // Fired for all kind of notifications (foreground, background & closed)
        });
    });

    // Register for the notification settings event
    Ti.App.iOS.registerUserNotificationSettings({
        types: [
            Ti.App.iOS.USER_NOTIFICATION_TYPE_ALERT,
            Ti.App.iOS.USER_NOTIFICATION_TYPE_SOUND,
            Ti.App.iOS.USER_NOTIFICATION_TYPE_BADGE
        ]
    });
}

// check if token is already available
if (fcm.fcmToken !== null) {
    Ti.API.info('FCM-Token', fcm.fcmToken);
} else {
    Ti.API.info('Token is empty. Waiting for the token callback ...');
}

// subscribe to topic
fcm.subscribeToTopic('testTopic');
