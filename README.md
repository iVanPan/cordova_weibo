# Cordova_Weibo_Plugin
[![version](https://img.shields.io/badge/version-0.3.4-blue.svg?style=flat)](https://github.com/iVanPan/cordova_weibo)
[![platform](https://img.shields.io/badge/platform-iOS%2FAndroid-lightgrey.svg?style=flat)](https://github.com/iVanPan/cordova_weibo)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat)](https://github.com/iVanPan/cordova_weibo/blob/master/LICENSE)
[![Contact](https://img.shields.io/badge/contact-Van-green.svg?style=flat)](http://VanPan.me)	

This is a Cordova Plugin for WeiboSDK. [简体中文](https://github.com/iVanPan/cordova_weibo/blob/master/README_ZH.md)  
I also write a cordova plugin  for qq sdk [here](https://github.com/iVanPan/Cordova_QQ).
## Feature
- Weibo SSO Login
- Weibo Logout
- Weibo WebPage Share
- Check Weibo Client is Installed

## Requirements
- Cordova Version 3.5+ 
- Cordova-Android >=4.0			

## Installation
1. ```cordova plugin add https://github.com/iVanPan/cordova_weibo.git --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```  or    ```cordova plugin add cordova-plugin-weibosdk --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```
2. Add ```<preference name="REDIRECTURI" value="YOUR_WEIBO_REDIRECTURI" />``` in your config.xml If you don't add this preference the defualt redirecturi is https://api.weibo.com/oauth2/default.html               
3. cordova build 
				

##Notes
1. This plugin is required cordova-android version >=4.0,so using cordova  5.0.0 or higher is recommended
2. This plugin should be used after the deviceready event has been fired!!!				
3. ~~If cordova version  <5.1.1,when two cordova plugins are modifying “*-Info.plist” CFBundleURLTypes, only the first added plugin is getting the changes applied.so after installing plugin,please check the URLTypes in your Xcode project.You can find this issue [here](https://issues.apache.org/jira/browse/CB-8007).~~Update:This Bug is fixed in last cordova version(5.1.1)				

## About iOS 9

###App Transport Security                   
iOS 9 introduces a new security feature that blocks non-HTTPS traffic in your app. However,   WeiBo SDK not support HTTPS yet, this Plugin will turn off https and allow non-HTTPS traffic         


##ISSUES						
1.if you are sharing webpage without weibo app client	,the webpage sharing becomes text sharing.	

## Usage
### Weibo SSO Login
```Javascript
YCWeibo.ssoLogin(function(args){
         alert(args.access_token);
         alert(args.userid);
      },function(failReason){
         console.log(failReason);
});
```
### Weibo Logout
```Javascript
YCWeibo.logout(function(){
	console.log('logout success');
},function(failReason){
	console.log(failReason);
});
```
### Weibo Webpage Share
```Javascript
var args = {};
args.url = "http://www.baidu.com";
args.title = "Baidu";
args.description = "This is Baidu";
args.imageUrl = "https://www.baidu.com/img/bdlogo.png";//if you don't have imageUrl,for android http://www.sinaimg.cn/blog/developer/wiki/LOGO_64x64.png will be the defualt one
args.defaultText = "";
YCWeibo.shareToWeibo(function () {
    alert("share success");
 }, function (failReason) {
    alert(failReason);
 }, args);
```
### CheckClientInstalled
```Javascript
YCWeibo.checkClientInstalled(function(){
	console.log('client is installed');
},function(){
	console.log('client is not installed');
});
```
##Example			
1. install this plugin
2. backup www folder in your cordova project
3. replace www by example_www
4. cordova build & test	


##About WeiboSdk
you can downlaod last weibosdk [here](https://github.com/sinaweibosdk) .if you find any problem about weibosdk, open an isssus please.

##About Get User Info
after weibo sso Login,you can get access_token and userid,using get method to get user info directly with url https://api.weibo.com/2/users/show.json?uid=xxxx&access_token=xxxx


