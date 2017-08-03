#import "CDVWeiboSDK.h"

NSString *WEBIO_APP_ID = @"weibo_app_id";
NSString *WEBIO_REDIRECT_URI = @"redirecturi";
NSString *WEBIO_DEFUALT_REDIRECT_URI = @"https://api.weibo.com/oauth2/default.html";
NSString *WEIBO_CANCEL_BY_USER = @"cancel by user";
NSString *WEIBO_SHARE_INSDK_FAIL = @"share in sdk failed";
NSString *WEIBO_SEND_FAIL = @"send failed";
NSString *WEIBO_UNSPPORTTED = @"Weibo unspport";
NSString *WEIBO_AUTH_ERROR = @"Weibo auth error";
NSString *WEIBO_UNKNOW_ERROR = @"Weibo unknow error";
NSString *WEIBO_TOKEN_EMPTY = @"Weibo token is empty";
NSString *WEIBO_USER_CANCEL_INSTALL = @"user cancel install weibo";

@implementation CDVWeiboSDK
/**
 *  插件初始化主要用于appkey的注册
 */
- (void)pluginInitialize {
    NSString *weiboAppId = [[self.commandDelegate settings] objectForKey:WEBIO_APP_ID];
    self.weiboAppId = weiboAppId;
    [WeiboSDK registerApp:weiboAppId];
    NSString *redirectURI = [[self.commandDelegate settings] objectForKey:WEBIO_REDIRECT_URI];
    if (nil == redirectURI) {
        self.redirectURI = WEBIO_DEFUALT_REDIRECT_URI;
    } else {
        self.redirectURI = redirectURI;
    }
}
/**
 *  检查微博官方客户端是否安装
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)checkClientInstalled:(CDVInvokedUrlCommand *)command {
    if ([WeiboSDK isWeiboAppInstalled]) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    } else {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

/**
 *  微博单点登录
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)ssoLogin:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    WBAuthorizeRequest *request = [WBAuthorizeRequest request];
    request.redirectURI = self.redirectURI;
    request.scope = @"all";
    request.userInfo = @{ @"SSO_From" : @"CDVWeiboSDK",
                          @"Other_Info_1" : [NSNumber numberWithInt:123],
                          @"Other_Info_2" : @[ @"obj1", @"obj2" ],
                          @"Other_Info_3" : @{@"key1" : @"obj1", @"key2" : @"obj2"} };
    [WeiboSDK sendRequest:request];
}

/**
 *  微博登出
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)logout:(CDVInvokedUrlCommand *)command {
    NSUserDefaults *saveDefaults = [NSUserDefaults standardUserDefaults];
    NSString *token = [saveDefaults objectForKey:@"access_token"];
    [saveDefaults removeObjectForKey:@"userId"];
    [saveDefaults removeObjectForKey:@"access_token"];
    [saveDefaults removeObjectForKey:@"expires_time"];
    [saveDefaults synchronize];
    if (token) {
        [WeiboSDK logOutWithToken:token delegate:self withTag:nil];
        self.callbackId = command.callbackId;
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_TOKEN_EMPTY];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

/**
 *  分享网页到微博
 *
 *  @param command CDVInvokedUrlCommand
 */
 - (void)shareToWeibo:(CDVInvokedUrlCommand *)command {
     self.callbackId = command.callbackId;
     WBAuthorizeRequest *authRequest = [WBAuthorizeRequest request];
     authRequest.redirectURI = self.redirectURI;
     authRequest.scope = @"all";
     NSDictionary *params = [command.arguments objectAtIndex:0];
     WBMessageObject *message = [WBMessageObject message];
     WBWebpageObject *webpage = [WBWebpageObject object];
     webpage.objectID = [NSString stringWithFormat:@"%f", [[NSDate date] timeIntervalSince1970]];
     webpage.title = [self check:@"title" in:params];
     webpage.description = [NSString stringWithFormat:[self check:@"description" in:params], [[NSDate date] timeIntervalSince1970]];
     webpage.webpageUrl = [self check:@"url" in:params];
     NSString *image = [self check:@"image" in:params];
     NSData *imageData = [self processImage:image];
     webpage.thumbnailData = imageData;
     message.mediaObject = webpage;
     NSUserDefaults *saveDefaults = [NSUserDefaults standardUserDefaults];
     NSString *token = [saveDefaults objectForKey:@"access_token"];
     WBSendMessageToWeiboRequest *request = [WBSendMessageToWeiboRequest requestWithMessage:message authInfo:authRequest access_token:token];
     request.userInfo = @{ @"ShareMessageFrom" : @"CDVWeiboSDK",
                           @"Other_Info_1" : [NSNumber numberWithInt:123],
                           @"Other_Info_2" : @[ @"obj1", @"obj2" ],
                           @"Other_Info_3" : @{@"key1" : @"obj1", @"key2" : @"obj2"} };
     [WeiboSDK sendRequest:request];
 }
/**
 *  分享图片到微博
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)shareImageToWeibo:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    WBAuthorizeRequest *authRequest = [WBAuthorizeRequest request];
    authRequest.redirectURI = self.redirectURI;
    authRequest.scope = @"all";
    NSDictionary *params = [command.arguments objectAtIndex:0];
    WBMessageObject *message = [WBMessageObject message];
    WBImageObject *imageObject = [WBImageObject object];
    NSString *image = [self check:@"image" in:params];
    NSData *imageData = [self processImage:image];
    imageObject.imageData = imageData;
    message.imageObject = imageObject;
    NSUserDefaults *saveDefaults = [NSUserDefaults standardUserDefaults];
    NSString *token = [saveDefaults objectForKey:@"access_token"];
    WBSendMessageToWeiboRequest *request = [WBSendMessageToWeiboRequest requestWithMessage:message authInfo:authRequest access_token:token];
    request.userInfo = @{ @"ShareMessageFrom" : @"CDVWeiboSDK",
                          @"Other_Info_1" : [NSNumber numberWithInt:123],
                          @"Other_Info_2" : @[ @"obj1", @"obj2" ],
                          @"Other_Info_3" : @{@"key1" : @"obj1", @"key2" : @"obj2"} };
    [WeiboSDK sendRequest:request];
}
/**
 *  分享文字到微博
 *
 *  @param command CDVInvokedUrlCommand
 */
- (void)shareTextToWeibo:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    WBAuthorizeRequest *authRequest = [WBAuthorizeRequest request];
    authRequest.redirectURI = self.redirectURI;
    authRequest.scope = @"all";
    NSDictionary *params = [command.arguments objectAtIndex:0];
    WBMessageObject *message = [WBMessageObject message];
    NSString *text = [self check:@"text" in:params];
    message.text = text;
    NSUserDefaults *saveDefaults = [NSUserDefaults standardUserDefaults];
    NSString *token = [saveDefaults objectForKey:@"access_token"];
    WBSendMessageToWeiboRequest *request = [WBSendMessageToWeiboRequest requestWithMessage:message authInfo:authRequest access_token:token];
    request.userInfo = @{ @"ShareMessageFrom" : @"CDVWeiboSDK",
                          @"Other_Info_1" : [NSNumber numberWithInt:123],
                          @"Other_Info_2" : @[ @"obj1", @"obj2" ],
                          @"Other_Info_3" : @{@"key1" : @"obj1", @"key2" : @"obj2"} };
    [WeiboSDK sendRequest:request];
}

/**
 *  处理URL
 *
 *  @param notification cordova传递的消息对象
 */
- (void)handleOpenURL:(NSNotification *)notification {
    NSURL *url = [notification object];
    if ([url isKindOfClass:[NSURL class]] && [url.absoluteString hasPrefix:[@"wb" stringByAppendingString:self.weiboAppId]]) {
        [WeiboSDK handleOpenURL:url delegate:self];
    }
}

#pragma mark - WeiboSDKDelegate
- (void)didReceiveWeiboResponse:(WBBaseResponse *)response {
    if ([response isKindOfClass:WBSendMessageToWeiboResponse.class]) {
        if (response.statusCode == WeiboSDKResponseStatusCodeSuccess) {
            WBSendMessageToWeiboResponse *sendMessageToWeiboResponse = (WBSendMessageToWeiboResponse *)response;
            NSString *accessToken = [sendMessageToWeiboResponse.authResponse accessToken];
            NSString *userId = [sendMessageToWeiboResponse.authResponse userID];
            NSString *expirationTime = [NSString stringWithFormat:@"%f", [sendMessageToWeiboResponse.authResponse.expirationDate timeIntervalSince1970] * 1000];
            if (accessToken && userId && expirationTime) {
                NSUserDefaults *saveDefaults = [NSUserDefaults standardUserDefaults];
                [saveDefaults setValue:accessToken forKey:@"access_token"];
                [saveDefaults setValue:userId forKey:@"userId"];
                [saveDefaults setValue:expirationTime forKey:@"expires_time"];
                [saveDefaults synchronize];
            }
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUserCancel) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_CANCEL_BY_USER];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeSentFail) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_SEND_FAIL];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeShareInSDKFailed) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_SHARE_INSDK_FAIL];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUnsupport) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_UNSPPORTTED];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUnknown) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_UNKNOW_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeAuthDeny) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_AUTH_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUserCancelInstall) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_USER_CANCEL_INSTALL];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        }
    } else if ([response isKindOfClass:WBAuthorizeResponse.class]) {
        if (response.statusCode == WeiboSDKResponseStatusCodeSuccess) {
            NSMutableDictionary *Dic = [NSMutableDictionary dictionaryWithCapacity:2];
            [Dic setObject:[(WBAuthorizeResponse *)response userID] forKey:@"userId"];
            [Dic setObject:[(WBAuthorizeResponse *)response accessToken] forKey:@"access_token"];
            [Dic setObject:[NSString stringWithFormat:@"%f", [(WBAuthorizeResponse *)response expirationDate].timeIntervalSince1970 * 1000] forKey:@"expires_time"];
            NSUserDefaults *saveDefaults = [NSUserDefaults standardUserDefaults];
            [saveDefaults setValue:[(WBAuthorizeResponse *)response userID] forKey:@"userId"];
            [saveDefaults setValue:[(WBAuthorizeResponse *)response accessToken] forKey:@"access_token"];
            [saveDefaults setValue:[NSString stringWithFormat:@"%f", [(WBAuthorizeResponse *)response expirationDate].timeIntervalSince1970 * 1000] forKey:@"expires_time"];
            [saveDefaults synchronize];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Dic];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUserCancel) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_CANCEL_BY_USER];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeSentFail) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_SEND_FAIL];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeShareInSDKFailed) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_SHARE_INSDK_FAIL];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUnsupport) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_UNSPPORTTED];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUnknown) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_UNKNOW_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeAuthDeny) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_AUTH_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        } else if (response.statusCode == WeiboSDKResponseStatusCodeUserCancelInstall) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:WEIBO_USER_CANCEL_INSTALL];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        }
    }
}

- (void)didReceiveWeiboRequest:(WBBaseRequest *)request {
}

#pragma mark - WBHttpRequestDelegate

- (void)request:(WBHttpRequest *)request didFinishLoadingWithResult:(NSString *)result {
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void)request:(WBHttpRequest *)request didFailWithError:(NSError *)error {
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}
/**
 图片处理

 @param image 图片数据
 @return 图片NSdata数据
 */
- (NSData *)processImage:(NSString *)image {
    if ([self isBase64Data:image]) {
        return [[NSData alloc] initWithBase64EncodedString:image options:0];
    } else if ([image hasPrefix:@"http://"] || [image hasPrefix:@"https://"]) {
        NSURL *url = [NSURL URLWithString:image];
        return [NSData dataWithContentsOfURL:url];
    } else {
        return [NSData dataWithContentsOfFile:image];
    }
}

/**
 检查图片是不是Base64

 @param data 图片数据
 @return 结果true or false
 */
- (BOOL)isBase64Data:(NSString *)data {
    data = [[data componentsSeparatedByCharactersInSet:
             [NSCharacterSet whitespaceAndNewlineCharacterSet]]
            componentsJoinedByString:@""];
    if ([data length] % 4 == 0) {
        static NSCharacterSet *invertedBase64CharacterSet = nil;
        if (invertedBase64CharacterSet == nil) {
            invertedBase64CharacterSet = [[NSCharacterSet characterSetWithCharactersInString:@"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="] invertedSet];
        }
        return [data rangeOfCharacterFromSet:invertedBase64CharacterSet options:NSLiteralSearch].location == NSNotFound;
    }
    return NO;
}

/**
 检查参数是否存在

 @param param 要检查的参数
 @param args 参数字典
 @return 参数
 */
- (NSString *)check:(NSString *)param in:(NSDictionary *)args {
    NSString *data = [args objectForKey:param];
    return data?data:@"";
}
@end
