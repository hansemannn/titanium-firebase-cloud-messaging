# Firebase Cloud Messaging - Titanium Module

Use the native Firebase SDK (iOS/Android) in Axway Titanium. This repository is part of the [Titanium Firebase](https://github.com/hansemannn/titanium-firebase) project.

## Requirements
- [x] iOS: Titanium SDK 6.2.0+
- [x] Android: Titanium SDK 7.0.0+, [Ti.PlayServices](https://github.com/appcelerator-modules/ti.playservices) module

## Download
- [x] [Stable release](https://github.com/hansemannn/titanium-firebase-cloud-messaging/releases)
- [x] [![gitTio](http://hans-knoechel.de/shields/shield-gittio.svg)](http://gitt.io/component/firebase.cloudmessaging)

## API's

### `FirebaseCloudMessaging`

#### Methods

##### `registerForPushNotifications()`

##### `appDidReceiveMessage(parameters)`  - iOS only
  - `parameters` (Dictionary)

Note: Only call this method if method swizzling is disabled (enabled by default). Messages are received via the native delegates instead,
so receive the `gcm.message_id` key from the notification payload instead.
  
##### `sendMessage(parameters)`
  - `parameters` (Dictionary)
    - `messageID` (String)
    - `to` (String)
    - `timeToLive` (Number)
    - `data` (Dictionary)
  
##### `subcribeToTopic(topic)`
  - `topic` (String)

##### `unsubcribeFromTopic(topic)`
  - `topic` (String)
  
#### Properties

##### `fcmToken` (String, get)

##### `apnsToken` (String, set) - iOS only

##### `shouldEstablishDirectChannel` (Number, get/set)

#### Events

##### `didReceiveMessage`
  - `message` (Dictionary)
  
iOS Note: This method is only called on iOS 10+ and only for direct messages sent by Firebase. Normal Firebase push notifications
are still delivered via the Titanium notification events, e.g. `notification` and `remotenotification`.
  
##### `didRefreshRegistrationToken`
  - `fcmToken` (String)

## Example
```js
var core = require('firebase.core');
var fcm = require('firebase.cloudmessaging');

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

// check if token is already available
if (fcm.fcmToken !== null) {
    Ti.API.info('FCM-Token', fcm.fcmToken);
} else {
    Ti.API.info('Token is empty. Waiting for the token callback ...');
}

// subscribe to topic
fcm.subcribeToTopic('testTopic');
```

## Send FCM messages with PHP
To test your app you can use this PHP script to send messages to the device/topic:

```php
<?php $url = 'https://fcm.googleapis.com/fcm/send';

	$fields = array (
			'to' => '/topics/testTopic', // or device token
			'notification' => array (
					'title' => 'TiFirebaseMessaging',
					'body' => 'Message received'
			),
			'data' => array(
				'key1' => 'value1',
				'key2' => 'value2'
			)
	);

	$headers = array (
			'Authorization: key=SERVER_ID_FROM_FIREBASE_SETTIGNS_CLOUD_MESSAGING',
			'Content-Type: application/json'
	);

	$ch = curl_init ();
	curl_setopt ( $ch, CURLOPT_URL, $url );
	curl_setopt ( $ch, CURLOPT_POST, true );
	curl_setopt ( $ch, CURLOPT_HTTPHEADER, $headers );
	curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
	curl_setopt ( $ch, CURLOPT_POSTFIELDS, json_encode($fields));

	$result = curl_exec ( $ch );
	echo $result;
	curl_close ( $ch );
?>
```

Run it locally with `php filelane.php` or put it on a webserver where you can execute PHP files.

## Build

### iOS

```js
cd ios
appc ti build -p ios --build-only
```

### Android

```js
cd android
appc ti build -p android --build-only
```

## Legal

(c) 2017-Present by Hans Knöchel & Michael Gangolf
