/**
 * titanium-firebase-cloud-messaging
 *
 * Created by Hans Knoechel
 * Copyright (c) 2017 Your Company. All rights reserved.
 */

#import "TiModule.h"

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
#import <UserNotifications/UserNotifications.h>
#endif

@import FirebaseMessaging;

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
@interface FirebaseCloudmessagingModule : TiModule<FIRMessagingDelegate, UIApplicationDelegate, UNUserNotificationCenterDelegate> {
#else
@interface FirebaseCloudmessagingModule : TiModule<FIRMessagingDelegate, UIApplicationDelegate> {
#endif
}

- (NSString *)fcmToken;

- (void)registerForPushNotifications:(id)arguments;

- (void)setApnsToken:(NSString *)apnsToken;

- (void)setShouldEstablishDirectChannel:(NSNumber *)shouldEstablishDirectChannel;

- (NSNumber *)shouldEstablishDirectChannel;

- (void)appDidReceiveMessage:(id)arguments;

- (void)sendMessage:(id)arguments;

- (void)subscribeToTopic:(id)topic;

- (void)unsubscribeFromTopic:(id)topic;
  
@end
