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

- (id)_initWithPageContext:(id<TiEvaluator>)context
{
  if (self = [super _initWithPageContext:context]) {
    [[FIRMessaging messaging] setDelegate:self];
  }
  
  return self;
}

- (void)_configure
{
  [super _configure];
  [[TiApp app] registerApplicationDelegate:self];
}

- (void)startup
{
  [super startup];
  NSLog(@"[DEBUG] %@ loaded", self);
}

#pragma Public APIs

- (NSString *)fcmToken
{
  return [[FIRMessaging messaging] FCMToken];
}

- (void)registerForPushNotifications:(id)arguments
{
  DEPRECATED_REPLACED_REMOVED(@"FirebaseCloudMessaging.registerForPushNotifications", @"2.0.0", @"2.0.0", @"Network.registerForPushNotifications");
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
  return @([[FIRMessaging messaging] shouldEstablishDirectChannel]);
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

- (void)messaging:(FIRMessaging *)messaging didReceiveRegistrationToken:(NSString *)fcmToken
{
  if ([self _hasListeners:@"didRefreshRegistrationToken"]) {
    [self fireEvent:@"didRefreshRegistrationToken" withObject:@{ @"fcmToken": fcmToken }];
  }
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
  [[FIRMessaging messaging] setAPNSToken:deviceToken];
}

@end
