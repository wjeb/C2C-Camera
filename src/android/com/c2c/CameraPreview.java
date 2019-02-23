package com.c2c;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.R;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class CameraPreview extends CordovaPlugin implements CameraActivity.CameraPreviewListener {

	private final String TAG = "CameraPreview";
	private final String setOnPictureTakenHandlerAction = "setOnPictureTakenHandler";
	private final String startCameraAction = "startCamera";
	private final String stopCameraAction = "stopCamera";
	private final String takePictureAction = "takePicture";
	private final String showCameraAction = "showCamera";
	private final String hideCameraAction = "hideCamera";

	private CameraActivity fragment;
	private CallbackContext takePictureCallbackContext;
	
	//public static BarcodeView barcodeView;
	//public static BarcodeCallback callback;
	
	private int containerViewId = 1;
	public CameraPreview(){
		super();
		Log.d(TAG, "LOL: Constructing");
	}

	private void alertView( String message ) {
		
		AlertDialog.Builder alertPopup = new AlertDialog.Builder(cordova.getActivity());
		//alertPopup.setIcon(R.drawable.ic_android_cat);
		alertPopup.setTitle("Alert");
		alertPopup.setMessage(message);
		alertPopup.setNegativeButton("ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
		AlertDialog alert = alertPopup.create();
		alert.show();
		
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		
		if (setOnPictureTakenHandlerAction.equals(action)){
			return setOnPictureTakenHandler(args, callbackContext);
		}
		else if (startCameraAction.equals(action)){
			return startCamera(args, callbackContext);
		}
		else if (takePictureAction.equals(action)){
			return takePicture(args, callbackContext);
		}
		else if (stopCameraAction.equals(action)){
			return stopCamera(args, callbackContext);
		}
		else if (hideCameraAction.equals(action)){
			return hideCamera(args, callbackContext);
		}
		else if (showCameraAction.equals(action)){
			return showCamera(args, callbackContext);
		}
		
		return false;
		
	}

	@Override
	private BarcodeCallback callback = new BarcodeCallback() {
        
		@Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
				
				alertView(result.getText());
				
               // TextView tvMessage = (TextView) findViewById(R.id.tvMessage);
				//if (tvMessage != null) {
                //        tvMessage.setText(R.string.registered_yay);
                //   }
                //   barcodeView.decodeSingle(callback);
				//   
               // }
                //else {
               //     if (tvMessage != null) {
                //        tvMessage.setText(R.string.error_registering);
				//		
                //    }
                //    barcodeView.decodeSingle(callback);
                //}
				
            }
        }
		
    };


	private boolean startCamera(final JSONArray args, CallbackContext callbackContext) {
		
		Log.d(TAG, "LOL: startCamera function");
		
		alertView("Preview: StartCamera");
		
		if(fragment != null){
			return false;
		}
		
		fragment = new CameraActivity();
		fragment.setEventListener(this);
		
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				try {
					
					DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
					
					int x = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, args.getInt(0), metrics);
					int y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, args.getInt(1), metrics);
					int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, args.getInt(2), metrics);
					int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, args.getInt(3), metrics);
					
					String defaultCamera = args.getString(4);
					
					fragment.defaultCamera = defaultCamera;
					fragment.setRect(x, y, width, height);
					
					// create or update the layout params for the container view
					FrameLayout containerView = (FrameLayout)cordova.getActivity().findViewById(containerViewId);
					if(containerView == null){
						
						containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
						containerView.setId(containerViewId);
						
						FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
						cordova.getActivity().addContentView(containerView, containerLayoutParams);
						
					}
					
					//display camera bellow the webview
					webView.getView().setBackgroundColor(0x00000000);
					((ViewGroup)webView.getView()).bringToFront();
					
					//add the fragment to the container
					FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.add(containerView.getId(), fragment);
					fragmentTransaction.commit();
					
					//barcodeView = (BarcodeView) webView.getView().findViewById(containerViewId);
					//barcodeView = (BarcodeView) containerView;
					//barcodeView.decodeContinuous(callback);
					
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		
		return true;
	}
	
	private boolean takePicture(final JSONArray args, CallbackContext callbackContext) {
		
		Log.d("warn","Picture taken...");
		
		if(fragment == null){
			return false;
		}
		
		alertView("Preview: takePicture");
		
		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
		pluginResult.setKeepCallback(true);
		
		callbackContext.sendPluginResult(pluginResult);
		
		try {
			fragment.takePicture();
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void onPictureTaken(String originalPictureInBase64){
		
		alertView("Preview: onPictureTaken");
		
		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, originalPictureInBase64);
		pluginResult.setKeepCallback(true);
		
		takePictureCallbackContext.sendPluginResult(pluginResult);
		
	}

	private boolean stopCamera(final JSONArray args, CallbackContext callbackContext) {
		
		if(fragment == null){
			return false;
		}
		
		Camera camera = fragment.getCamera();
		if (camera == null){
			return true;
		}
		
		FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.remove(fragment);
		fragmentTransaction.commit();
		fragment = null;
		
		return true;
		
	}

	private boolean showCamera(final JSONArray args, CallbackContext callbackContext) {
		
		if(fragment == null){
			return false;
		}
		
		FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.show(fragment);
		fragmentTransaction.commit();
		
		return true;
	}
	
	private boolean hideCamera(final JSONArray args, CallbackContext callbackContext) {
		
		if(fragment == null) {
			return false;
		}
		
		FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.hide(fragment);
		fragmentTransaction.commit();
		
		return true;
	}
	
	private boolean setOnPictureTakenHandler(JSONArray args, CallbackContext callbackContext) {
		
		Log.d(TAG, "setOnPictureTakenHandler");
		
		takePictureCallbackContext = callbackContext;
		return true;
		
	}
	
}
