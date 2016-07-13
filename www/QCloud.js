
var exec    = require('cordova/exec'),
cordova = require('cordova');

module.exports = {
	upLoadVideo:function(successCallback, errorCallback,args){
		if(args == null || args == undefined){
			args = 0;
		}
		exec(successCallback, errorCallback, "QCloud", "upLoadVideo", [args]);
	}
};

