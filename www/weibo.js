var cordova = require('cordova');
module.exports = {
	checkClientInstalled:function(successCallback, errorCallback){
		cordova.exec(successCallback, errorCallback, "WeiboSDK", "checkClientInstalled", []);
	},
	ssoLogin:function(successCallback, errorCallback){
		cordova.exec(successCallback, errorCallback, "WeiboSDK", "ssoLogin", []);
	},
	logout:function(successCallback, errorCallback){
		cordova.exec(successCallback, errorCallback, "WeiboSDK", "logout", []);
	},
	shareToWeibo:function(successCallback, errorCallback,args){
		if(args == undefined){
			args = {};
		}
		cordova.exec(successCallback, errorCallback, "WeiboSDK", "shareToWeibo", [args]);
	}
};
