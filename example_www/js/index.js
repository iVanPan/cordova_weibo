var app = function () {
  this.checkClientInstalled = function () {
    WeiboSDK.checkClientInstalled(function () {
      alert('client is installed');
    }, function () {
      alert('client is not installed');
    });
  };

  this.ssoLogin = function () {
    WeiboSDK.ssoLogin(function (args) {
      alert('access token is ' + args.access_token);
      alert('userId is ' + args.userId);
      alert('expires_time is ' + new Date(parseInt(args.expires_time)) + ' TimeStamp is ' + args.expires_time);
    }, function (failReason) {
      alert(failReason);
    });
  };

  this.shareToWeibo = function () {
    var args = {};
    args.url = 'https://cordova.apache.org/';
    args.title = 'Apache Cordova';
    args.description = 'This is a Cordova Plugin';
    args.image = 'https://cordova.apache.org/static/img/pluggy.png';
    WeiboSDK.shareToWeibo(function () {
      alert('share success');
    }, function (failReason) {
      alert(failReason);
    }, args);
  };

  this.shareImageToWeibo = function () {
    var args = {};
    args.image = 'https://cordova.apache.org/static/img/pluggy.png';
    WeiboSDK.shareImageToWeibo(function () {
      alert('share success');
    }, function (failReason) {
      alert(failReason);
    }, args);
  };

  this.shareTextToWeibo = function () {
    var args = {};
    args.text = 'This is a Cordova Plugin';
    WeiboSDK.shareTextToWeibo(function () {
      alert('share success');
    }, function (failReason) {
      alert(failReason);
    }, args);
  };

  this.logout = function () {
    WeiboSDK.logout(function () {
      alert('logout success');
    }, function (failReason) {
      alert(failReason);
    });
  }
}
