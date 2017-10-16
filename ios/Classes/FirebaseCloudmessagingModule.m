/**
 * titanium-firebase-cloud-messaging
 *
 * Created by Hans Knoechel
 * Copyright (c) 2017 Your Company. All rights reserved.
 */

#import "FirebaseCloudmessagingModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiApp.h"

@implementation FirebaseCloudmessagingModule

#pragma mark Internal

- (id)moduleGUID
{
  return @"36052f2b-8ddc-4177-b5a1-82c6b948f83d";
}

- (NSString *)moduleId
{
  return @"firebase.cloudmessaging";
}

#pragma mark Lifecycle

- (void)startup
{
  [super startup];
  NSLog(@"[DEBUG] %@ loaded", self);
}

// Uncomment once 7.0.0.GA is released, bump module-version as well
//
//- (id)_initWithPageContext:(id<TiEvaluator>)context
//{
//  if (self = [super _initWithPageContext:context]) {
//    [[TiApp app] setApplicationDelegate:self];
//  }
//
//  return self;
//}

#pragma Public APIs

- (NSString *)fcmToken
{
  return [[FIRMessaging messaging] FCMToken];
}

- (void)registerForPushNotifications:(id)arguments
{
  if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
    UIUserNotificationType allNotificationTypes =
    (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
    UIUserNotificationSettings *settings =
    [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
  } else {
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
    [UNUserNotificationCenter currentNotificationCenter].delegate = self;
    UNAuthorizationOptions authOptions = UNAuthorizationOptionAlert | UNAuthorizationOptionSound | UNAuthorizationOptionBadge;
    [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:authOptions completionHandler:^(BOOL granted, NSError * _Nullable error) {
    }];
#endif
  }
  
  [[UIApplication sharedApplication] registerForRemoteNotifications];
}

- (void)setApnsToken:(NSString *)apnsToken
{
  [[FIRMessaging messaging] setAPNSToken:[apnsToken dataUsingEncoding:NSUTF8StringEncoding]];
}


- (void)setShouldEstablishDirectChannel:(NSNumber *)shouldEstablishDirectChannel
{
  [[FIRMessaging messaging] setShouldEstablishDirectChannel:[TiUtils boolValue:shouldEstablishDirectChannel]];
}

- (NSNumber *)shouldEstablishDirectChannel
{
  return NUMBOOL([[FIRMessaging messaging] shouldEstablishDirectChannel]);
}

- (void)appDidReceiveMessage:(id)arguments
{
  ENSURE_SINGLE_ARG(arguments, NSDictionary);
  [[FIRMessaging messaging] appDidReceiveMessage:arguments];
}

- (void)sendMessage:(id)arguments
{
  ENSURE_SINGLE_ARG(arguments, NSDictionary);
  
  NSDictionary *message = [arguments objectForKey:@"message"];
  NSString *messageID = [arguments objectForKey:@"messageID"];
  NSString *to = [arguments objectForKey:@"to"];
  int64_t timeToLive = [(NSNumber *)[arguments objectForKey:@"timeToLive"] unsignedLongLongValue];

  [[FIRMessaging messaging] sendMessage:message
                                     to:to
                          withMessageID:messageID
                             timeToLive:timeToLive];
}

- (void)subscribeToTopic:(id)topic
{
  ENSURE_SINGLE_ARG(topic, NSString);
  [[FIRMessaging messaging] subscribeToTopic:topic];
}

- (void)unsubscribeFromTopic:(id)topic
{
  ENSURE_SINGLE_ARG(topic, NSString);
  [[FIRMessaging messaging] unsubscribeFromTopic:topic];
}

#pragma mark FIRMessaging Delegates

- (void)messaging:(FIRMessaging *)messaging didReceiveMessage:(FIRMessagingRemoteMessage *)remoteMessage
{
  if ([self _hasListeners:@"didReceiveMessage"]) {
    [self fireEvent:@"didReceiveMessage" withObject:@{ @"message": remoteMessage.appData }];
  }
}

- (void)messaging:(FIRMessaging *)messaging didRefreshRegistrationToken:(NSString *)fcmToken
{
  if ([self _hasListeners:@"didRefreshRegistrationToken"]) {
    [self fireEvent:@"didRefreshRegistrationToken" withObject:@{ @"fcmToken": fcmToken }];
  }
}

@end
