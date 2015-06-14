
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVPluginResult.h>
#import "WeiboSDK.h"

@interface YCWeibo : CDVPlugin<WeiboSDKDelegate>{}

@property (strong, retain) NSString* callback;
@property (nonatomic, copy) NSString *redirectURI;
@property (nonatomic, copy) NSString *weiboAppId;
-(void)ssoLogin:(CDVInvokedUrlCommand*)command;
-(void)logout:(CDVInvokedUrlCommand*)command;
-(void)shareToWeibo:(CDVInvokedUrlCommand *)command;
-(void)checkClientInstalled:(CDVInvokedUrlCommand *)command;
@end
