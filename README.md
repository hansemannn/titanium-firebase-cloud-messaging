# Firebase Cloud Messaging Titanium Module
Use the native Firebase SDK in Axway Titanium. This repository is part of the [Titanium Firebase](https://github.com/hansemannn/titanium-firebase) project.

## Requirements
- [x] Titanium SDK 6.2.0 or later

## API's

### `FirebaseCloudMessaging`

#### Methods

##### `configure()`

##### `registerForPushNotifications()`

##### `appDidReceiveMessage(parameters)`
  - `parameters` (Dictionary)
  
##### `sendMessage(parameters)`
  - `parameters` (Dictionary)
    - `message` (String)
    - `messageID` (String)
    - `to` (String)
    - `timeTiLive` (Number)
  
##### `subcribeToTopic(topic)`
  - `topic` (String)

##### `unsubcribeFromTopic(topic)`
  - `topic` (String)
  
#### Properties

##### `fcmToken` (String, get)

##### `apnsToken` (String, set)

##### `shouldEstablishDirectChannel` (Number, get/set)

#### Events

##### `didReceiveMessage`
  - `message` (Dictionary)
  
##### `didRefreshRegistrationToken`
  - `fcmToken` (String)

## Example
```js
var FirebaseCloudMessaging = require('firebase.cloudmessaging');

FirebaseCloudMessaging.configure();
```

## Build
```js
cd iphone
appc ti build -p ios --build-only
```

## Legal

This module is Copyright (c) 2017-Present by Appcelerator, Inc. All Rights Reserved. 
Usage of this module is subject to the Terms of Service agreement with Appcelerator, Inc.  
