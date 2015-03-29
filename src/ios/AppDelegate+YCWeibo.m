//
//  AppDelegate＋YCWeibo.m
//  
//
//  Created by Van on 15/3/3.
//
//

#import "AppDelegate+YCWeibo.h"
#import "YCWeibo.h"
#import <objc/runtime.h>

@implementation AppDelegate (YCWeibo)

void swizzleMethod(Class c, SEL originalSelector)
{
    NSString *original = NSStringFromSelector(originalSelector);
    
    SEL swizzledSelector = NSSelectorFromString([@"swizzled_" stringByAppendingString:original]);
    SEL noopSelector = NSSelectorFromString([@"noop_" stringByAppendingString:original]);
    
    Method originalMethod, swizzledMethod, noop;
    originalMethod = class_getInstanceMethod(c, originalSelector);
    swizzledMethod = class_getInstanceMethod(c, swizzledSelector);
    noop = class_getInstanceMethod(c, noopSelector);
    
    BOOL didAddMethod = class_addMethod(c,
                                        originalSelector,
                                        method_getImplementation(swizzledMethod),
                                        method_getTypeEncoding(swizzledMethod));
    
    if (didAddMethod)
    {
        class_replaceMethod(c,
                            swizzledSelector,
                            method_getImplementation(noop),
                            method_getTypeEncoding(originalMethod));
    }
    else
    {
        method_exchangeImplementations(originalMethod, swizzledMethod);
    }
}

+ (void)load
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        Class cls = [self class];
        
        swizzleMethod(cls, @selector(application:didFinishLaunchingWithOptions:));
        swizzleMethod(cls, @selector(application:didRegisterForRemoteNotificationsWithDeviceToken:));
        swizzleMethod(cls, @selector(application:didReceiveRemoteNotification:));
    });
}

- (BOOL)swizzled_application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    BOOL ret = [self swizzled_application:application didFinishLaunchingWithOptions:launchOptions];    
    if (ret)
    {
        NSString *appId = [self.viewController.settings objectForKey:@"weibo_app_id"];
        NSString *redirectURI = [self.viewController.settings objectForKey:@"redirecturi"];
        if (appId)
        {
            YCWeibo *weibo = [self.viewController getCommandInstance:@"YCWeibo"];
            [weibo registerApp:appId];
            if(redirectURI){
                [weibo saveredirectURI:redirectURI];
            }
        }
    }
    
    return ret;
}

- (BOOL)noop_application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    return YES;
}
@end
