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

- (void)setApnsToken:(NSString *)apnsToken
{
  [[FIRMessaging messaging] setAPNSToken:[apnsToken dataUsingEncoding:NSUTF8StringEncoding]];
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

#pragma mark Deprecated / removed APIs

- (void)setShouldEstablishDirectChannel:(NSNumber *)shouldEstablishDirectChannel
{
  DEPRECATED_REMOVED(@"Firebase.CloudMessaging.shouldEstablishDirectChannel", @"3.0.0", @"3.0.0 (FCM direct channel is deprecated, please use APNs channel for downstream message delivery.)");
}

- (NSNumber *)shouldEstablishDirectChannel
{
  DEPRECATED_REMOVED(@"Firebase.CloudMessaging.shouldEstablishDirectChannel", @"3.0.0", @"3.0.0 (FCM direct channel is deprecated, please use APNs channel for downstream message delivery.)");

  return @(NO);
}

- (void)appDidReceiveMessage:(id)arguments
{
  ENSURE_SINGLE_ARG(arguments, NSDictionary);
  [[FIRMessaging messaging] appDidReceiveMessage:arguments];
}

- (void)sendMessage:(id)arguments
{
  DEPRECATED_REMOVED(@"Firebase.CloudMessaging.sendMessage", @"3.0.0", @"3.0.0 (Upstream messaging through direct channel is deprecated. For realtime updates, use Cloud Firestore, Realtime Database, or other services.)");
}

@end
