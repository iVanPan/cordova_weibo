var app = function () {
  this.checkClientInstalled = function () {
    YCWeibo.checkClientInstalled(function(){
      alert('client is installed');
    },function(){
      alert('client is not installed');
    });
  };
  this.ssoLogin = function () {
    YCWeibo.ssoLogin(function(args){
     alert("access token is "+args.access_token);
     alert("userid is "+args.userid);
      alert("expires_time is "+ new Date(parseInt(args.expires_time)) + " TimeStamp is " +args.expires_time);
   },function(failReason){
     alert(failReason);
   });
  };
  this.shareToWeibo = function () {
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
  };
  this.logout = function () {
    YCWeibo.logout(function(){
      alert('logout success');
    },function(failReason){
      alert(failReason);
    });
  }
}
