
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVPluginResult.h>
#import "WeiboSDK.h"

@interface YCWeibo : CDVPlugin<WeiboSDKDelegate>{}

@property (strong, retain) NSString* callback;
@property (nonatomic, strong) NSString *weiboAppId;
-(void)ssoLogin:(CDVInvokedUrlCommand*)command;
-(void)logout:(CDVInvokedUrlCommand*)command;
-(void)registerApp:(NSString *)weiboAppId;
-(void)saveredirectURI:(NSString *)redirectURI;
@end
