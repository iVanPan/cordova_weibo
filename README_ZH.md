# Cordova_Weibo_Plugin
这是一个微博的Cordova插件(设用于Android和iOS) .For English,Please check [here](https://github.com/iVanPan/cordova_weibo)    
同时我还写了一个QQ的Cordova插件有兴趣的话可以查看[这里](https://github.com/iVanPan/Cordova_QQ)
# 功能
微博SSO 登录，微博登出，微博网页分享
# 安装
1. 命令行运行 ```cordova plugin add https://github.com/iVanPan/cordova_weibo.git --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```
2. 在你的config.xml文件中添加 ```<preference name="REDIRECTURI" value="YOUR_WEIBO_REDIRECTURI" />``` 如果你不添加这个选项，那么默认的 redirecturi 是 https://api.weibo.com/oauth2/default.html               
3. 命令行运行cordova build 
4.  如果你在你的cordova iOS工程上面使用了这个插件,请打开Xcode检查WeiboSDK的URLTypes是否正确添加了.如果没有，请自行手动添加.     					

#重要实现
1. 这个插件要求cordova-android 的版本 >=4.0,推荐使用 cordova  5.0.0 或更高的版本，因为从cordova 5.0 开始cordova-android 4.0 是默认使用的android版本
2.   请在cordova的deviceready事件触发以后再调用本插件!!!!!  				

#问题				
1.在Android平台不使用客户端进行分享的时候可能会遇到如下错误{"error":"userinfo error","pos":"5"}				
2.	在iOS平台不使用客户端分享进行分享的时候，网页分享会变成文字分享.	

# 使用方法
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

#注意事项     
cordova目前有一个bug，如果你有多个插件要修改iOS工程中的 “*-Info.plist” CFBundleURLTypes, 只有第一个安装的插件才会生效.所以安装完插件请务必在你的Xcode工程里面检查一下URLTypes。 关于这个bug的详情你可以在 [这里](https://issues.apache.org/jira/browse/CB-8007)找到


# LICENSE

[MIT LICENSE](https://github.com/iVanPan/cordova_weibo/blob/master/LICENSE)

