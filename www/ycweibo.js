
var exec    = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	ssoLogin:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "YCWeibo", "ssoLogin", []);
	},
	logout:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "YCWeibo", "logout", []);
	}

};

