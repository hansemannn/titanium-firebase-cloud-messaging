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

#pragma Public APIs



@end
