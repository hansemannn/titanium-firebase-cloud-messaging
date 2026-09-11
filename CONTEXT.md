# Internals

Notes on how the module behaves under the hood. Not needed to use it.

## iOS: why load order matters

`_configure` calls `[[TiApp app] registerApplicationDelegate:self]`, and the module's `didRegisterForRemoteNotificationsWithDeviceToken` is what hands the APNs token to `FIRMessaging`.

Require the module after APNs has already answered and nobody catches that token, so Firebase never issues one of its own. Nothing errors: the permission is granted, APNs returns its 64 characters, and `fetchToken` stays silent.

Hence "load at startup" in the readme.

## Android: how a tap reaches JavaScript

`TiFirebaseMessagingService.onMessageReceived()` fires `didReceiveMessage`, but only for a message the app receives. A tap goes elsewhere: `PushHandlerActivity` writes the payload into the launcher intent as the `fcm_data` extra and starts the activity.

`parseBootIntent()` turns that extra into `didReceiveMessage`, and it is called once, at the end of `registerForPushNotifications()`. A cold start therefore gets the event and a resume does not, because registration does not run again.

That is why the tap fires `didOpenNotification` from `setNotificationData()` instead. The intent cannot carry it while the app is alive: its relaunch intent matches the one `TiRootActivity` accepts, so Android treats it as "bring the existing task to the front" and does not deliver it a second time.

`setNotificationData()` reports whether a listener took the payload, and `PushHandlerActivity` only puts it on the intent when nobody did. Both routes at once would deliver twice, because Titanium resumes its root activity twice while handling an intent and an app listening for `resumed` sees the same notification again on the second pass.
