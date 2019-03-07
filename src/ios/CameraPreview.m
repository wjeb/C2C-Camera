#import <AssetsLibrary/AssetsLibrary.h>
#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVInvokedUrlCommand.h>

#import "CameraPreview.h"
#import <UIKit/UIKit.h>

@implementation CameraPreview

- (void) startCamera:(CDVInvokedUrlCommand*)command {
		
		CDVPluginResult *pluginResult;

        if (self.sessionManager != nil) {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera already started!"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                return;
        }
		
		if (command.arguments.count > 3) {
                CGFloat x = (CGFloat)[command.arguments[0] floatValue] + self.webView.frame.origin.x;
                CGFloat y = (CGFloat)[command.arguments[1] floatValue] + self.webView.frame.origin.y;
                CGFloat width = (CGFloat)[command.arguments[2] floatValue];
                CGFloat height = (CGFloat)[command.arguments[3] floatValue];
                NSString *defaultCamera = command.arguments[4];
                BOOL tapToTakePicture = (BOOL)[command.arguments[5] boolValue];
                BOOL dragEnabled = (BOOL)[command.arguments[6] boolValue];
                BOOL toBack = (BOOL)[command.arguments[7] boolValue];
                // Create the session manager
                self.sessionManager = [[CameraSessionManager alloc] init];
				
                //render controller setup
                self.cameraRenderController = [[CameraRenderController alloc] init];
                self.cameraRenderController.dragEnabled = dragEnabled;
                self.cameraRenderController.tapToTakePicture = tapToTakePicture;
                self.cameraRenderController.sessionManager = self.sessionManager;
                self.cameraRenderController.view.frame = CGRectMake(x, y, width, height);
                self.cameraRenderController.delegate = self;
				
                [self.viewController addChildViewController:self.cameraRenderController];
                //display the camera bellow the webview
				
                if (toBack) {
                        //make transparent
                        self.webView.opaque = NO;
                        self.webView.backgroundColor = [UIColor clearColor];
                        [self.viewController.view insertSubview:self.cameraRenderController.view atIndex:0];
                }
                else{
                        self.cameraRenderController.view.alpha = (CGFloat)[command.arguments[8] floatValue];

                        [self.viewController.view addSubview:self.cameraRenderController.view];
                }

                // Setup session
                self.sessionManager.delegate = self.cameraRenderController;
                [self.sessionManager setupSession:defaultCamera];

                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
				
				
        } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid number of parameters"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		
}

- (void) stopCamera:(CDVInvokedUrlCommand*)command {
        NSLog(@"stopCamera");
        CDVPluginResult *pluginResult;

        if(self.sessionManager != nil) {
                [self.cameraRenderController.view removeFromSuperview];
                [self.cameraRenderController removeFromParentViewController];
                self.cameraRenderController = nil;

                [self.sessionManager.session stopRunning];
                self.sessionManager = nil;

                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
        else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera not started"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) hideCamera:(CDVInvokedUrlCommand*)command {
        NSLog(@"hideCamera");
        CDVPluginResult *pluginResult;

        if (self.cameraRenderController != nil) {
                [self.cameraRenderController.view setHidden:YES];
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera not started"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) showCamera:(CDVInvokedUrlCommand*)command {
	
	NSLog(@"showCamera");
	CDVPluginResult *pluginResult;

	if (self.cameraRenderController != nil) {
			[self.cameraRenderController.view setHidden:NO];
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
	} else {
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera not started"];
	}

	[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	
}

- (void) takePicture:(CDVInvokedUrlCommand*)command {
        NSLog(@"takePicture");
        CDVPluginResult *pluginResult;

        if (self.cameraRenderController != NULL) {
				
				NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
				
				for (AVCaptureDevice *device in devices) {
					if ([device hasFlash] == YES) {
						[device lockForConfiguration:nil];
						[device setFlashMode:AVCaptureFlashModeOn];
						[device unlockForConfiguration];
					}
				}
				
                CGFloat maxW = (CGFloat)[command.arguments[0] floatValue];
                CGFloat maxH = (CGFloat)[command.arguments[1] floatValue];
				
				[self invokeTakePicture:maxW withHeight:maxH];
				
        } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera not started"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
}

- (void) takePreview:(CDVInvokedUrlCommand*)command {
        NSLog(@"takePreview");
        CDVPluginResult *pluginResult;

        if (self.cameraRenderController != NULL) {
			
			NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
			
			for (AVCaptureDevice *device in devices) {
				if ([device hasFlash] == YES) {
					[device lockForConfiguration:nil];
					[device setFlashMode:AVCaptureFlashModeOff];
					[device unlockForConfiguration];
				}
			}
			
            CGFloat maxW = (CGFloat)[command.arguments[0] floatValue];
            CGFloat maxH = (CGFloat)[command.arguments[1] floatValue];
			
            //[self invokeTakePreview:maxW withHeight:maxH];
			[self invokeTakePreview:800 withHeight:800 maxQuality:75];
			
        } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Camera not started"];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
}


-(void) setOnPictureTakenHandler:(CDVInvokedUrlCommand*)command {
        NSLog(@"setOnPictureTakenHandler");
        self.onPictureTakenHandlerId = command.callbackId;
}

-(void) setOnPreviewTakenHandler:(CDVInvokedUrlCommand*)command {
        NSLog(@"setOnPreviewTakenHandler");
        self.onPreviewTakenHandlerId = command.callbackId;
}

- (void) invokeTakePicture {
        [self invokeTakePicture:0.0 withHeight:0.0];
}

- (void) invokeTakePreview {
        [self invokeTakePreview:0.0 withHeight:0.0];
}

- (void) invokeTakePicture:(CGFloat) maxWidth withHeight:(CGFloat) maxHeight {
        AVCaptureConnection *connection = [self.sessionManager.stillImageOutput connectionWithMediaType:AVMediaTypeVideo];
        [self.sessionManager.stillImageOutput captureStillImageAsynchronouslyFromConnection:connection completionHandler:^(CMSampleBufferRef sampleBuffer, NSError *error) {

                 NSLog(@"Done creating still image");

                 if (error) {
                         NSLog(@"%@", error);
                 } else {
						
						NSData *jpegData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:sampleBuffer];
						UIImage *takenImage  = [UIImage imageWithData:jpegData];
						
						CGImageRef takenCGImage = takenImage.CGImage;
						
						CGFloat imageWidth = CGImageGetHeight(takenCGImage);
						CGFloat imageHeight = CGImageGetWidth(takenCGImage);
						
						CGFloat screenWidth = [[UIScreen mainScreen] bounds].size.width;
						CGFloat screenHeight = [[UIScreen mainScreen] bounds].size.height;
						
						CGFloat displayW = screenWidth / screenHeight;
						CGFloat displayH = screenHeight / screenWidth;
						
						CGFloat picW = imageWidth / imageHeight;
						CGFloat picH = imageHeight / imageWidth;
						
						CGFloat resultWidth = 0;
						CGFloat resultHeight = 0;
						
						/*
						NSString *alertMessage2 = [NSString stringWithFormat: @"Display size: %f x %f", screenWidth, screenHeight];
						UIAlertView *alert2 = [[UIAlertView alloc] initWithTitle:@"UIAlertView" message:alertMessage2 delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
						[alert2 show];
						#comment
						*/
						
						if(displayW<=picW){
							resultHeight = imageHeight;
							resultWidth = round(resultHeight * displayW);
						}else{
							resultWidth = imageWidth;
							resultHeight = round(resultWidth * displayH);
						}
						
						CGFloat leftMargin = round((imageWidth - resultWidth) / 2);
						CGFloat topMargin = round((imageHeight - resultHeight) / 2);
						
						CGFloat widthPercent = 30;
						CGFloat boxSideSize = round(resultWidth / 100 * widthPercent);
						
						leftMargin = leftMargin + round((resultWidth - boxSideSize) / 2);
						topMargin = topMargin + round((resultHeight - boxSideSize) / 2);
						
						// Send original
							
							CGRect cropRect = CGRectMake(topMargin, leftMargin, boxSideSize, boxSideSize);
							
							CGImageRef cropCGImage = CGImageCreateWithImageInRect(takenCGImage, cropRect);
							takenImage = [UIImage imageWithCGImage:cropCGImage scale:1 orientation:takenImage.imageOrientation];
							
							NSData *imageData = UIImageJPEGRepresentation(takenImage, 1.0);
							NSString *originalPictureInBase64 = [imageData base64Encoding];
							
							CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:originalPictureInBase64];
							
							[pluginResult setKeepCallbackAsBool:true];
							[self.commandDelegate sendPluginResult:pluginResult callbackId:self.onPictureTakenHandlerId];
							
							
						
						
                 }
         }];
}

- (void) invokeTakePreview:(CGFloat) maxWidth maxWidth:(CGFloat) maxHeight maxHeight:(CGFloat) maxQuality maxQuality:(CGFloat)  {
        
		//NSString *alertMessage3 = [NSString stringWithFormat: @"Callback invokeTakePreview Started"];
		//UIAlertView *alert3 = [[UIAlertView alloc] initWithTitle:@"UIAlertView" message:alertMessage3 delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
		//[alert3 show];
		
		AVCaptureConnection *connection = [self.sessionManager.stillImageOutput connectionWithMediaType:AVMediaTypeVideo];
        [self.sessionManager.stillImageOutput captureStillImageAsynchronouslyFromConnection:connection completionHandler:^(CMSampleBufferRef sampleBuffer, NSError *error) {

                 NSLog(@"Done creating still image");

                 if (error) {
                         NSLog(@"%@", error);
                 } else {
						
						NSData *jpegData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:sampleBuffer];
						UIImage *takenImage  = [UIImage imageWithData:jpegData];
						
						CGImageRef takenCGImage = takenImage.CGImage;
						
						CGFloat imageWidth = CGImageGetHeight(takenCGImage);
						CGFloat imageHeight = CGImageGetWidth(takenCGImage);
						
						CGFloat screenWidth = [[UIScreen mainScreen] bounds].size.width;
						CGFloat screenHeight = [[UIScreen mainScreen] bounds].size.height;
						
						CGFloat displayW = screenWidth / screenHeight;
						CGFloat displayH = screenHeight / screenWidth;
						
						CGFloat picW = imageWidth / imageHeight;
						CGFloat picH = imageHeight / imageWidth;
						
						CGFloat resultWidth = 0;
						CGFloat resultHeight = 0;
						
						//NSString *alertMessage2 = [NSString stringWithFormat: @"Display size: %f x %f", screenWidth, screenHeight];
						//UIAlertView *alert2 = [[UIAlertView alloc] initWithTitle:@"UIAlertView" message:alertMessage2 delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
						//[alert2 show];
						
						if(displayW<=picW){
							resultHeight = imageHeight;
							resultWidth = round(resultHeight * displayW);
						}else{
							resultWidth = imageWidth;
							resultHeight = round(resultWidth * displayH);
						}
						
						CGFloat leftMargin = round((imageWidth - resultWidth) / 2);
						CGFloat topMargin = round((imageHeight - resultHeight) / 2);
						
						CGFloat widthPercent = 30;
						CGFloat boxSideSize = round(resultWidth / 100 * widthPercent);
						
						leftMargin = leftMargin + round((resultWidth - boxSideSize) / 2);
						topMargin = topMargin + round((resultHeight - boxSideSize) / 2);
						
						// Send original
							
							CGRect cropRect = CGRectMake(topMargin, leftMargin, boxSideSize, boxSideSize);
							
							CGImageRef cropCGImage = CGImageCreateWithImageInRect(takenCGImage, cropRect);
							takenImage = [UIImage imageWithCGImage:cropCGImage scale:1 orientation:takenImage.imageOrientation];
							
							NSString *alertMessage2 = [NSString stringWithFormat: @"Preview size: %f x %f", takenWidth, takenHeight];
							UIAlertView *alert2 = [[UIAlertView alloc] initWithTitle:@"UIAlertView" message:alertMessage2 delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"OK", nil];
							[alert2 show];
							
							//------------ Resizing ------------|
								
								CGFloat scaleHeight = maxWidth/takenImage.size.height;
								CGFloat scaleWidth = maxHeight/takenImage.size.width;
								
								CGFloat scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
								
								CIFilter *resizeFilter = [CIFilter filterWithName:@"CILanczosScaleTransform"];
									
									[resizeFilter setValue:[[CIImage alloc] initWithCGImage:[takenImage CGImage]] forKey:kCIInputImageKey];
									[resizeFilter setValue:[NSNumber numberWithFloat:1.0f] forKey:@"inputAspectRatio"];
									[resizeFilter setValue:[NSNumber numberWithFloat:scale] forKey:@"inputScale"];
									
								takenImage = [resizeFilter outputImage];
								
							//------------ Resizing ------------|
							
							NSData *imageData = UIImageJPEGRepresentation(takenImage, 0.75);
							NSString *originalPictureInBase64 = [imageData base64Encoding];
							
							CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:originalPictureInBase64];
							
							[pluginResult setKeepCallbackAsBool:true];
							[self.commandDelegate sendPluginResult:pluginResult callbackId:self.onPreviewTakenHandlerId];
							
							
						
						
                 }
         }];
}

@end
