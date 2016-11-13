	
	var argscheck = require('cordova/argscheck'),
		utils = require('cordova/utils'),
		exec = require('cordova/exec');
	
	var PLUGIN_NAME = "C2CCamera";

	var C2CCamera = function() {};
		
		C2CCamera.setOnPictureTakenHandler = function(onPictureTaken) {
			exec(onPictureTaken, onPictureTaken, PLUGIN_NAME, "setOnPictureTakenHandler", []);
		};
		
		C2CCamera.startCamera = function(rect, defaultCamera, tapEnabled, dragEnabled, toBack, alpha) {
			if (typeof(alpha) === 'undefined') alpha = 1;
			exec(null, null, PLUGIN_NAME, "startCamera", [rect.x, rect.y, rect.width, rect.height, defaultCamera, !!tapEnabled, !!dragEnabled, !!toBack, alpha]);
		};
		
		C2CCamera.stopCamera = function() {
			exec(null, null, PLUGIN_NAME, "stopCamera", []);
		};
		
		C2CCamera.takePicture = function(size) {
			exec(null, null, PLUGIN_NAME, "takePicture", []);
		};
		
		C2CCamera.hide = function() {
			exec(null, null, PLUGIN_NAME, "hideCamera", []);
		};
		
		C2CCamera.show = function() {
			exec(null, null, PLUGIN_NAME, "showCamera", []);
		};
		
		C2CCamera.disable = function(disable) {
			exec(null, null, PLUGIN_NAME, "disable", [disable]);
		};
		
	module.exports = C2CCamera;
	
