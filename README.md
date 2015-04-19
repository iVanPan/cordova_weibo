# Cordova_Weibo_Plugin
This is a Cordova Plugin for WeiboSDK(Both on android and iOS)     
# Feature
Weibo SSO Login, Weibo Logout,Weibo WebPage Share
# Install
1. ```cordova plugin add https://github.com/iVanPan/cordova_weibo.git --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```
2. Add ```<preference name="REDIRECTURI" value="YOUR_WEIBO_REDIRECTURI" />``` in your config.xml If you don't add this preference the defualt redirecturi is https://api.weibo.com/oauth2/default.html               
3. cordova build 
4.  If you are using this plugin for iOS,check the URLTypes in your Xcode project.If you don't  find URLTypes for weibosdk，manually add it.    					

#ISSUES				
1.For android,if you are sharing webpage without weibo app client,you may get error like this {"error":"userinfo error","pos":"5"}				
2.	For iOS,if you are sharing webpage without weibo app client	,the webpage sharing becomes text sharing.	

# Usage
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

#Notice      
When two cordova plugins are modifying “*-Info.plist” CFBundleURLTypes, only the first added plugin is getting the changes applied.so after installing plugin,please check the URLTypes in your Xcode project.You can find this issue [here](https://issues.apache.org/jira/browse/CB-8007)

###About Get User Info
after weibo sso Login,you can get access_token and userid,using get method to get user info directly with url https://api.weibo.com/2/users/show.json?uid=xxxx&access_token=xxxx

# LICENSE

[MIT LICENSE](https://github.com/iVanPan/cordova_weibo/blob/master/LICENSE)

