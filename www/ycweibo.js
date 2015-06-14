
var exec    = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	ssoLogin:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "YCWeibo", "ssoLogin", []);
	},
	logout:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "YCWeibo", "logout", []);
	},
	shareToWeibo:function(successCallback, errorCallback,args){
		if(args == null || args == undefined){
			args = {};
		}
		if(args.url == null || args.url == undefined){
			args.url = "";
		}
		if(args.title == null || args.title == undefined){
			args.title = "";
		}
		if(args.description == null || args.description == undefined){
			args.description = "";
		}
		if(args.imageUrl == null || args.imageUrl == undefined){
			args.imageUrl = "";
		}
		if(args.defaultText == null || args.defaultText == undefined){
			args.defaultText = "";
		}
		exec(successCallback, errorCallback, "YCWeibo", "shareToWeibo", [args]);
	},
	checkClientInstalled:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "YCWeibo", "checkClientInstalled", []);
	}

};

