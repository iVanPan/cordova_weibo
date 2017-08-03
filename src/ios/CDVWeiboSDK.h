#import <Cordova/CDVPlugin.h>
#import "WeiboSDK.h"

@interface CDVWeiboSDK : CDVPlugin <WeiboSDKDelegate, WBHttpRequestDelegate>

@property (nonatomic, copy) NSString *callbackId;
@property (nonatomic, copy) NSString *redirectURI;
@property (nonatomic, copy) NSString *weiboAppId;

- (void)ssoLogin:(CDVInvokedUrlCommand *)command;

- (void)logout:(CDVInvokedUrlCommand *)command;

- (void)shareToWeibo:(CDVInvokedUrlCommand *)command;

- (void)checkClientInstalled:(CDVInvokedUrlCommand *)command;

- (void)shareImageToWeibo:(CDVInvokedUrlCommand *)command;

- (void)shareTextToWeibo:(CDVInvokedUrlCommand *)command;

@end
