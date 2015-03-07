# Cordova_Weibo_Plugin
This is a Cordova Plugin for WeiboSDK(Both on android and iOS)     
# Feature
Weibo SSO Login, Weibo Logout
# Install
1. ```cordova plugin add https://github.com/iVanPan/cordova_weibo.git --variable WEIBO_APP_ID=YOUR_WEIBO_APPID```
2. Add ```<preference name="WEIBO_APP_ID" value="YOUR_WEIBO_APP_ID" />``` in your config.xml     
3. Add ```<preference name="REDIRECTURI" value="YOUR_WEIBO_REDIRECTURI" />``` in your config.xml                
4. cordova build      

# Usage
### Weibo SSO Login
```Javascript
YCWeibo.ssoLogin(function(args){
         alert(args.access_token);
         alert(args.userid);
      },function(){
         console.log('login error');
});
```
### Weibo Logout
```Javascript
YCWeibo.logout(function(){
	console.log('logout success');
},function(){
	console.log('logout error');
});
```



###About Get User Info
after weibo sso Login,you can get access_token and userid,using get method to get user info directly with url https://api.weibo.com/2/users/show.json?uid=xxxx&access_token=xxxx

# LICENSE

[MIT LICENSE](https://github.com/iVanPan/cordova_weibo/blob/master/LICENSE)

