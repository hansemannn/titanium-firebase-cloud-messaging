# Internals

Notes on how the module behaves under the hood. Not needed to use it.

## iOS: why load order matters

`_configure` calls `[[TiApp app] registerApplicationDelegate:self]`, and the module's `didRegisterForRemoteNotificationsWithDeviceToken` is what hands the APNs token to `FIRMessaging`.

Require the module after APNs has already answered and nobody catches that token, so Firebase never issues one of its own. Nothing errors: the permission is granted, APNs returns its 64 characters, and `fetchToken` stays silent.

Hence "load at startup" in the readme.

## Android: how a tap reaches JavaScript

`TiFirebaseMessagingService.onMessageReceived()` fires `didReceiveMessage`, but only for a message the app receives. A tap goes elsewhere: `PushHandlerActivity` writes the payload into the launcher intent as the `fcm_data` extra and starts the activity.

`parseBootIntent()` turns that extra into `didReceiveMessage`, and it is called once, at the end of `registerForPushNotifications()`. A cold start therefore gets the event and a resume does not, because registration does not run again.
