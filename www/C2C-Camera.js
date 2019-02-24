	
	var argscheck = require('cordova/argscheck'),
		utils = require('cordova/utils'),
		exec = require('cordova/exec');

	var PLUGIN_NAME = "CameraPreview";

	var CameraPreview = function() {};
		
		CameraPreview.setOnPictureTakenHandler = function(onPictureTaken) {
			exec(onPictureTaken, onPictureTaken, PLUGIN_NAME, "setOnPictureTakenHandler", []);
		};
		
		CameraPreview.setOnPreviewTakenHandler = function(onPreviewTaken) {
			exec(onPreviewTaken, onPreviewTaken, PLUGIN_NAME, "setOnPreviewTakenHandler", []);
		};
		
		CameraPreview.startCamera = function(rect, defaultCamera, tapEnabled, dragEnabled, toBack, alpha) {
			if (typeof(alpha) === 'undefined') alpha = 1;
			exec(null, null, PLUGIN_NAME, "startCamera", [rect.x, rect.y, rect.width, rect.height, defaultCamera, !!tapEnabled, !!dragEnabled, !!toBack, alpha]);
		};
		
		CameraPreview.stopCamera = function() {
		  exec(null, null, PLUGIN_NAME, "stopCamera", []);
		};
		
		CameraPreview.takePicture = function(size) {
			exec(null, null, PLUGIN_NAME, "takePicture", [0, 0]);
		};
		
		CameraPreview.takePreview = function(size) {
			exec(null, null, PLUGIN_NAME, "takePreview", [0, 0]);
		};
		
		CameraPreview.hide = function() {
			exec(null, null, PLUGIN_NAME, "hideCamera", []);
		};
		
		CameraPreview.show = function() {
			exec(null, null, PLUGIN_NAME, "showCamera", []);
		};
		
		CameraPreview.disable = function(disable) {
		  exec(null, null, PLUGIN_NAME, "disable", [disable]);
		};
		
	module.exports = CameraPreview;
