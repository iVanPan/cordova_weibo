# Cordova 微博插件
[![version](https://img.shields.io/badge/version-0.3.4-blue.svg?style=flat)](https://github.com/iVanPan/cordova_weibo)
[![platform](https://img.shields.io/badge/platform-iOS%2FAndroid-lightgrey.svg?style=flat)](https://github.com/iVanPan/cordova_weibo)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat)](https://github.com/iVanPan/cordova_weibo/blob/master/LICENSE)
[![Contact](https://img.shields.io/badge/contact-Van-green.svg?style=flat)](http://VanPan.me)	
这是一个微博的Cordova插件. [English](https://github.com/iVanPan/cordova_weibo)    
同时我还写了一个QQ的Cordova插件有兴趣的话可以查看[这里](https://github.com/iVanPan/Cordova_QQ)				
这两个插件将进行长期的维护，如果有任何问题或是建议，请开issuss，也欢迎star或是fork。微博SDK部分如有涉及版权问题，版权归新浪微博所有
## 功能
- 微博SSO 登录
- 微博登出
- 微博网页分享
- 检查微博官方客户端是否安装

##安装要求
- Cordova Version >=3.5
- Cordova-Android >=4.0

## 安装
1. 命令行运行 ```cordova plugin add https://github.com/iVanPan/cordova_weibo.git --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```   或是    ```cordova plugin add cordova-plugin-weibosdk --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```			
2. 在你的config.xml文件中添加 ```<preference name="REDIRECTURI" value="YOUR_WEIBO_REDIRECTURI" />``` 如果你不添加这个选项，那么默认的 redirecturi 是 https://api.weibo.com/oauth2/default.html               
3. 命令行运行cordova build   					

#注意事项
1. 这个插件要求cordova-android 的版本 >=4.0,推荐使用 cordova  5.0.0 或更高的版本，因为从cordova 5.0 开始cordova-android 4.0 是默认使用的android版本
2.   请在cordova的deviceready事件触发以后再调用本插件!!!!!  
3. <del>在低于5.1.1的cordova版本中存在一个Bug，如果你有多个插件要修改iOS工程中的 “*-Info.plist” CFBundleURLTypes, 只有第一个安装的插件才会生效.所以安装完插件请务必在你的Xcode工程里面检查一下URLTypes。 关于这个bug的详情你可以在 [这里](https://issues.apache.org/jira/browse/CB-8007)找到</del> 建议安装使用5.1.1及以上的cordova版本 				

## 问题				
1.在不使用客户端分享进行分享的时候，网页分享会变成文字分享			

## 关于 iOS 9 适配					
###App Transport Security							
在 iOS 9 中 Apple 默认要求使用HTTPS ，由于目前 Weibo SDK 还不支持，安装完这个插件以后将不再默认使用HTTPS					
	

##使用方法
### 微博SSO登录
```Javascript
YCWeibo.ssoLogin(function(args){
         alert(args.access_token);
         alert(args.userid);
      },function(failReason){
         console.log(failReason);
});
```
### 微博登出
```Javascript
YCWeibo.logout(function(){
	console.log('logout success');
},function(failReason){
	console.log(failReason);
});
```
### 微博网页分享
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
### 检查微博客户端是否安装了
```Javascript
YCWeibo.checkClientInstalled(function(){
	console.log('client is installed');
},function(){
	console.log('client is not installed');
});
```
#测试Demo
在安装完这个插件以后，把 cordova 工程中的代码替换为 example_www中的代码，在build以后可以进行各个功能测试，以下为运行效果图：
<div style="text-align:center"><img src="https://github.com/iVanPan/cordova_weibo/blob/master/ScreenShot.png?raw=true" alt="example" style="width:300px"></div>		

##关于微博SDK
你可以在[这里](https://github.com/sinaweibosdk)，找到最新的微博SDK，如果发现bug请开issus，同时也欢迎star 和 fork


