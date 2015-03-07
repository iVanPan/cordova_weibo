
#import "YCWeibo.h"
#import "AppDelegate.h"
#import "WeiboSDK.h"


@implementation YCWeibo
-(void)ssoLogin:(CDVInvokedUrlCommand *)command{
    self.callback=command.callbackId;
    NSUserDefaults *saveDefaults=[NSUserDefaults standardUserDefaults];
    NSString *URI = [saveDefaults valueForKey:@"redirectURI"];
    if(!URI){
        CDVPluginResult *pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        WBAuthorizeRequest *request = [WBAuthorizeRequest request];
        request.redirectURI = URI;
        request.scope = @"all";
        request.userInfo = @{@"SSO_From": @"YCWeibo",
                             @"Other_Info_1": [NSNumber numberWithInt:123],
                             @"Other_Info_2": @[@"obj1", @"obj2"],
                             @"Other_Info_3": @{@"key1": @"obj1", @"key2": @"obj2"}};
        [WeiboSDK sendRequest:request];
    }

}
-(void)logout:(CDVInvokedUrlCommand *)command
{
    NSUserDefaults *saveDefaults=[NSUserDefaults standardUserDefaults];
    NSString *token = [saveDefaults objectForKey:@"access_token"];
    [saveDefaults removeObjectForKey:@"userid"];
    [saveDefaults removeObjectForKey:@"access_token"];
    [saveDefaults synchronize];
    NSLog(@"保存的token 是 %@",token);
    if(token){
        [WeiboSDK logOutWithToken:token delegate:self.appDelegate withTag:nil];
    }
    CDVPluginResult *pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)registerApp:(NSString *)weiboAppId
{
    self.weiboAppId = weiboAppId;
    [WeiboSDK registerApp:weiboAppId];
    NSLog(@"Register weibo app: %@", weiboAppId);
}

-(void)saveredirectURI:(NSString *)redirectURI
{
    NSUserDefaults *saveDefaults=[NSUserDefaults standardUserDefaults];
    [saveDefaults setValue:redirectURI forKey:@"redirectURI"];
    [saveDefaults synchronize];
    NSLog(@"Save weibo redirectURI: %@", redirectURI);
}

- (void)handleOpenURL:(NSNotification *)notification
{
    NSURL* url = [notification object];
    NSString *wb=@"wb";
    if ([url isKindOfClass:[NSURL class]] && [url.absoluteString hasPrefix:[wb stringByAppendingString:self.weiboAppId]])
    {     
        [WeiboSDK handleOpenURL:url delegate:self];
    }
}

-(void)didReceiveWeiboResponse:(WBBaseResponse *)response
{
    if ([response isKindOfClass:WBSendMessageToWeiboResponse.class])
    {
    }
    else if ([response isKindOfClass:WBAuthorizeResponse.class])
    {
        NSLog(@"授权回调");
        NSMutableDictionary *Dic =[NSMutableDictionary dictionaryWithCapacity:2];
        [Dic setObject:[(WBAuthorizeResponse *)response userID] forKey:@"userid"];
        [Dic setObject:[(WBAuthorizeResponse *)response accessToken] forKey:@"access_token"];
        NSUserDefaults *saveDefaults=[NSUserDefaults standardUserDefaults];
        [saveDefaults setValue:[(WBAuthorizeResponse *)response userID] forKey:@"userid"];
        [saveDefaults setValue:[(WBAuthorizeResponse *)response accessToken] forKey:@"access_token"];
        [saveDefaults synchronize];
        NSLog(@"开始保存 dic is %@",Dic);
        

        CDVPluginResult *pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Dic];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callback];
    }
    
}
-(void)didReceiveWeiboRequest:(WBBaseRequest *)request
{
    
}

@end
