
var exec    = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	ssoLogin:function(successCallback, errorCallback,args){
		if(args == null || args == undefined){
			args = 0;
		}
		exec(successCallback, errorCallback, "QCloud", "ssoLogin", [args]);
	},
	logout:function(successCallback, errorCallback){
		exec(successCallback, errorCallback, "YCQQ", "logout", []);
	}
};

