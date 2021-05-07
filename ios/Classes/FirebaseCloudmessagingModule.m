/**
 * titanium-firebase-cloud-messaging
 *
 * Created by Hans Knoechel
 * Copyright (c) 2017-present by Hans Knöchel. All rights reserved.
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

#pragma Public APIs

- (NSString *)fcmToken
{
  return NULL_IF_NIL([[FIRMessaging messaging] FCMToken]);
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

- (void)fetchToken:(id)callback
{
  ENSURE_SINGLE_ARG_OR_NIL(callback, KrollCallback);
  [[FIRMessaging messaging] tokenWithCompletion:^(NSString * _Nullable token, NSError * _Nullable error) {
    [callback call:@[@{ @"token": token ?: [NSNull null], @"error": (error.localizedDescription) ?: [NSNull null] }] thisObject:self];
  }];
}

- (void)deleteToken:(id)callback
{
  ENSURE_SINGLE_ARG_OR_NIL(callback, KrollCallback);
  
  [[FIRMessaging messaging] deleteTokenWithCompletion:^(NSError * _Nullable error) {
    if (callback != nil) {
      NSDictionary *dict = nil;
      if (error != nil) {
        dict = @{ @"success": @(NO), @"error": [error localizedDescription] };
      } else {
        dict = @{ @"success": @(YES) };
      }
      [callback call:@[dict] thisObject:nil];
    }
  }];
}

- (void)deleteTokenForSenderID:(id)args
{
  ENSURE_ARG_COUNT(args, 2);

  NSString *senderID;
  ENSURE_ARG_AT_INDEX(senderID, args, 0, NSString);
  
  KrollCallback *callback;
  ENSURE_ARG_AT_INDEX(callback, args, 1, KrollCallback);
  
  [[FIRMessaging messaging] deleteFCMTokenForSenderID:senderID completion:^(NSError * _Nullable error) {
    if (callback != nil) {
      NSDictionary *dict = nil;
      if (error != nil) {
        dict = @{ @"success": @(NO), @"error": [error localizedDescription] };
      } else {
        dict = @{ @"success": @(YES) };
      }
      [callback call:@[dict] thisObject:nil];
    }
  }];
}

#pragma mark FIRMessaging Delegates

- (void)messaging:(FIRMessaging *)messaging didReceiveRegistrationToken:(NSString *)fcmToken
{
  if ([self _hasListeners:@"didRefreshRegistrationToken"]) {
    [self fireEvent:@"didRefreshRegistrationToken" withObject:@{ @"fcmToken": NULL_IF_NIL(fcmToken) }];
  }
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
  [[FIRMessaging messaging] setAPNSToken:deviceToken];
}

- (void)appDidReceiveMessage:(id)arguments
{
  ENSURE_SINGLE_ARG(arguments, NSDictionary);
  [[FIRMessaging messaging] appDidReceiveMessage:arguments];
}

@end
