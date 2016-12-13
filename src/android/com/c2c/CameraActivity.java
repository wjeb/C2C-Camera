package com.c2c;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.graphics.SurfaceTexture;

import org.apache.cordova.LOG;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends Fragment {

	public interface CameraPreviewListener {
		public void onPictureTaken(String originalPictureInBase64);
	}

	private CameraPreviewListener eventListener;
	private static final String TAG = "CameraActivity";
	
	public FrameLayout mainLayout;
	public FrameLayout frameContainerLayout;

	private Preview mPreview;
	private boolean canTakePicture = true;

	private View view;
	private Camera.Parameters cameraParameters = null;
	private Camera mCamera;
	private int numberOfCameras;
	private int cameraCurrentlyLocked = -1;

	// The first rear facing camera
	private int defaultCameraId;
	public String defaultCamera;
	
	private boolean isAutoFocused;
	
	public int width;
	public int height;
	public int x;
	public int y;
	
	public int lastPixelColor;

	public void setEventListener(CameraPreviewListener listener) {
		Log.d(TAG, "LOL: setEventListener");
		eventListener = listener;
	}

	private String appResourcesPackage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
	    appResourcesPackage = getActivity().getPackageName();
		
	    // Inflate the layout for this fragment
	    view = inflater.inflate(getResources().getIdentifier("camera_activity", "layout", appResourcesPackage), container, false);
	    createCameraPreview();
		
		Log.d(TAG, "LOL: onCreateView return");
		
	    return view;
		
    }

	@Override
	public void onCreate(Bundle savedInstanceState){
		
		Log.d(TAG, "LOL: onCreate");
        super.onCreate(savedInstanceState);
		
	}

	public void setRect(int x, int y, int width, int height){
		
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
	}

	private void createCameraPreview(){
		
		Log.d(TAG, "LOL: createCameraPreview function");
		
		if(mPreview == null){
			
			setDefaultCameraId();
			
			//set box position and size
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
			layoutParams.setMargins(x, y, 0, 0);
			frameContainerLayout = (FrameLayout) view.findViewById(getResources().getIdentifier("frame_container", "id", appResourcesPackage));
			frameContainerLayout.setLayoutParams(layoutParams);
			
			Log.d(TAG, "LOL: createCameraPreview 1");
			
			//video view
			mPreview = new Preview(getActivity());
			mainLayout = (FrameLayout) view.findViewById(getResources().getIdentifier("video_view", "id", appResourcesPackage));
			mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
			mainLayout.addView(mPreview);
			mainLayout.setEnabled(false);
			
        }
		
		Log.d(TAG, "LOL: createCameraPreview 2-3");
		
    }
	
    private void setDefaultCameraId(){
		
		Log.d(TAG, "LOL: setDefaultCameraId");
		
		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();
		Log.d(TAG, "LOL: Total number of cameras = "+numberOfCameras);
		
		int camId = defaultCamera.equals("front")?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_BACK;
		
		// Find the ID of the default camera
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for(int i=0;i<numberOfCameras;i++){
			
			Log.d(TAG, "LOL: camera number = " + i);
			Camera.getCameraInfo(i, cameraInfo);
			
			if(cameraInfo.facing == camId){
				
				Log.d(TAG, "LOL: setting default camera = " + camId);
				defaultCameraId = camId;
				
				break;
				
			}
			
		}
		
	}

    @Override
    public void onResume(){
		
		Log.d(TAG, "LOL: onResume");
		
		super.onResume();
		
		Log.d(TAG, "LOL: onResume 1");
		
		if(cameraCurrentlyLocked!=-1){
			
			mCamera = Camera.open(cameraCurrentlyLocked);
			Log.d(TAG, "LOL: onResume 1 - 1");
			
		}else{
			
			Log.d(TAG, "LOL: onResume 1 - 2");
			Log.d(TAG, "LOL: defaultCameraId = " + defaultCameraId);
			
			try{
				mCamera = Camera.open(defaultCameraId);
			}
			catch (Exception e){
				Log.d(TAG, "LOL: Cant start camera exception...");
				return ;
			}
			
			Log.d(TAG, "LOL: onResume 1 - 3");
			
			cameraCurrentlyLocked = defaultCameraId;
			Log.d(TAG, "LOL: onResume 1 - 4");
			
		}
		
		Log.d(TAG, "LOL: onResume 2");
		
		if(cameraParameters != null){
			mCamera.setParameters(cameraParameters);
		}
		
		Log.d(TAG, "LOL: onResume 3");
		
    	if(mPreview.mPreviewSize == null){
			
			mPreview.setCamera(mCamera, cameraCurrentlyLocked);
			
		}else{
			
			mPreview.switchCamera(mCamera, cameraCurrentlyLocked);
			mCamera.startPreview();
			
		}
		
    }
	
    @Override
    public void onPause(){
		
		Log.d(TAG, "LOL: onPause");
        super.onPause();
		
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if(mCamera != null){
            mPreview.setCamera(null, -1);
            mCamera.release();
            mCamera = null;
        }
		
    }

    public Camera getCamera(){
		return mCamera;
    }

	private Camera.AutoFocusCallback CameraAutoFocusCallback = new Camera.AutoFocusCallback(){
		
		public void onAutoFocus(boolean success, Camera camera) {
			
			if(success){
				
				//mCamera.takePicture(null, null, CameraJPEGCallback);
				mCamera.cancelAutoFocus();
				
			}else{
				
				eventListener.onPictureTaken("");
				
			}
			
		}
		
	};
	
	Camera.PictureCallback CameraJPEGCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			
			final Bitmap pic = BitmapFactory.decodeByteArray(data, 0, data.length);
				
				final Matrix matrix = new Matrix();
					
					if(cameraCurrentlyLocked == Camera.CameraInfo.CAMERA_FACING_FRONT) {
						Log.d(TAG, "LOL: mirror y axis");
						matrix.preScale(-1.0f, 1.0f);
					}
					
				matrix.postRotate(mPreview.getDisplayOrientation());
				
			
			Bitmap portraitPicture = Bitmap.createBitmap(pic, 0, 0, (int)(pic.getWidth()), (int)(pic.getHeight()), matrix, false);
				
				double displayW = (double) width/height;
				double displayH = (double) height/width;
				
				double picW = (double) portraitPicture.getWidth()/portraitPicture.getHeight();
				double picH = (double) portraitPicture.getHeight()/portraitPicture.getWidth();
				
				double picWidth = 0;
				double picHeight = 0;
				
				if(displayW<=picW){
					picHeight = portraitPicture.getHeight();
					picWidth = Math.round(picHeight * displayW);
				}else{
					picWidth = portraitPicture.getWidth();
					picHeight = Math.round(picWidth * displayH);
				}
				
				double leftMargin = Math.round( ((double)portraitPicture.getWidth() - picWidth) / 2 );
				double topMargin = Math.round( ((double)portraitPicture.getHeight() - picHeight) / 2 );
				
				//eventListener.onPictureTaken("alert:Display: "+width+"x"+height+"\nPicture: "+portraitPicture.getWidth()+"x"+portraitPicture.getHeight()+"\nResult: "+picWidth+"x"+picHeight);
				
				int widthPercent = 30;
				int boxSideSize = (int) Math.round(picWidth / 100 * widthPercent);
				
				leftMargin = leftMargin + ((picWidth - boxSideSize) / 2);
				topMargin = topMargin + ((picHeight - boxSideSize) / 2);
				
				picWidth = boxSideSize;
				picHeight = boxSideSize;
				
			
			Bitmap centralSquare = Bitmap.createBitmap(portraitPicture, (int)leftMargin, (int)topMargin, (int)picWidth, (int)picHeight);
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				centralSquare.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				
				byte[] attachmentBytes = stream.toByteArray();
				String originalPictureInBase64 = Base64.encodeToString(attachmentBytes, Base64.DEFAULT);
				
			eventListener.onPictureTaken(originalPictureInBase64);
			
			camera.startPreview();
			
			canTakePicture = true;
			
		}
	};


	public void takePicture(){
		
		if(mPreview != null){
			
			mCamera.takePicture(null, null, CameraJPEGCallback);
			//mCamera.autoFocus(CameraAutoFocusCallback);
			
			if(!canTakePicture){
				return;
			}else{
				canTakePicture = false;
			}
			
		}else{
			
			canTakePicture = true;
			
		}
		
	}

    @Override
    public void onDestroy() {
		
		Log.d(TAG, "LOL: onDestroy");
        super.onDestroy();
		
		if(mCamera != null){
            mPreview.setCamera(null, -1);
            mCamera.release();
            mCamera = null;
        }
		
    }
	
}


class Preview extends RelativeLayout implements SurfaceHolder.Callback {
    
	private final String TAG = "Preview";

    CustomSurfaceView mSurfaceView;
    SurfaceHolder mHolder;
	
    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;
	
    Camera mCamera;
	
    int cameraId;
    int displayOrientation;

    Preview(Context context){
		
        super(context);
		
        mSurfaceView = new CustomSurfaceView(context);
        addView(mSurfaceView);
		
        requestLayout();
		
        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
    }

    public void setCamera(Camera camera, int cameraId){
		
		Log.d(TAG, "LOL: setCamera");
        mCamera = camera;
		
        this.cameraId = cameraId;
		
        if(mCamera != null){
			
			Camera.Parameters params = mCamera.getParameters();
				
				List<String> mFocusModes = params.getSupportedFocusModes();
					
					if(mFocusModes.contains("continuous-picture")){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
					}else if(mFocusModes.contains("continuous-video")){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					}else if(mFocusModes.contains("auto")){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
					}
					
					/*
					if(mFocusModes.contains("auto")){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
					}else if(mFocusModes.contains("continuous-picture")){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
					}else if(mFocusModes.contains("continuous-video")){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					} 
					*/
					
				
				List<String> supportedFlashModes = params.getSupportedFlashModes();
					
					if(supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
						params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					}else if(supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
						params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
					}
					
				
				List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
					
					//-------------- Searching best resolution ----------------|
						
						int maxWidth = 0;
						int maxHeight = 0;
							
							for(Camera.Size size : supportedSizes){
								if((int)size.width>=1600 && (int)size.width<=3000){
									if((int)size.width > maxWidth){
										maxWidth = (int)size.width;
										maxHeight = (int)size.height;
									}
								}
							}
							
						
						if( maxWidth < 1600 || maxWidth > 3000 ){
							maxWidth = 1600;
							maxHeight = 1200;
						}
						
						params.setPictureSize(maxWidth, maxHeight);
						
					//-------------- Searching best resolution ----------------|
					
				
			mCamera.setParameters(params);
			
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			
			setCameraDisplayOrientation();
		  
        }
		
    }

    public int getDisplayOrientation(){
    	return displayOrientation;
    }
	
    private void setCameraDisplayOrientation(){
		
		Log.d(TAG, "LOL: setCameraDisplayOrientation");
		
        Camera.CameraInfo info = new Camera.CameraInfo();
		
        int rotation = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
		
        DisplayMetrics dm=new DisplayMetrics();
		
        Camera.getCameraInfo(cameraId, info);
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
		
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees=0;
                break;
            case Surface.ROTATION_90:
                degrees=90;
                break;
            case Surface.ROTATION_180:
                degrees=180;
                break;
            case Surface.ROTATION_270:
                degrees=270;
                break;
        }
		
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        	displayOrientation=(info.orientation + degrees) % 360;
        	displayOrientation=(360 - displayOrientation) % 360;
        }else{
        	displayOrientation=(info.orientation - degrees + 360) % 360;
        }
		
        Log.d(TAG, "LOL: screen is rotated " + degrees + "deg from natural");
        Log.d(TAG, (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? "front" : "back") + " camera is oriented -" + info.orientation + "deg from natural");
        Log.d(TAG, "LOL: need to rotate preview " + displayOrientation + "deg");
		
        mCamera.setDisplayOrientation(displayOrientation);
		
    }

    public void switchCamera(Camera camera, int cameraId){
		
		Log.d(TAG, "LOL: switchCamera 2");
		
        setCamera(camera, cameraId);
        
		try{
            
			camera.setPreviewDisplay(mHolder);
			
	        Camera.Parameters parameters = camera.getParameters();
				
				parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
				
			camera.setParameters(parameters);
        }
        catch (IOException exception) {
            Log.e(TAG, exception.getMessage());
        }
		
        //requestLayout();
		
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		Log.d(TAG, "LOL: onMesure");
		Log.d(TAG, "LOL: On mesure function...");
		
		// We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
		
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        
		setMeasuredDimension(width, height);
		
        if(mSupportedPreviewSizes != null){
			
			if(mPreviewSize == null){
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
			}
			
        }
		
    }
	
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b){
		
		Log.d(TAG, "LOL: onLayout");
		
        if(changed && getChildCount()>0){
			
            final View child = getChildAt(0);
			
            int width = r - l;
            int height = b - t;
			
            int previewWidth = width;
            int previewHeight = height;
			
            if(mPreviewSize != null){
				
				Log.d(TAG, "LOL: mPreviewSize.width = " + mPreviewSize.width + " mPreviewSize.height = " + mPreviewSize.height);
                
				previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
				
                if(displayOrientation == 90 || displayOrientation == 270) {
                    previewWidth = mPreviewSize.height;
                    previewHeight = mPreviewSize.width;
                }
				
	            LOG.d(TAG, "previewWidth:" + previewWidth + " previewHeight:" + previewHeight);
				
            }
			
            int nW;
            int nH;
            int top;
            int left;
			
            float scale = 1.0f;
			
            // Center the child SurfaceView within the parent.
            if (width * previewHeight < height * previewWidth) {
                
				Log.d(TAG, "center horizontally");
                
				int scaledChildWidth = (int)((previewWidth * height / previewHeight) * scale);
                
				nW = (width + scaledChildWidth) / 2;
                nH = (int)(height * scale);
                
				top = 0;
                left = (width - scaledChildWidth) / 2;
				
            } else {
                
				Log.d(TAG, "center vertically");
                
				int scaledChildHeight = (int)((previewHeight * width / previewWidth) * scale);
                
				nW = (int)(width * scale);
                nH = (height + scaledChildHeight) / 2;
                
				top = (height - scaledChildHeight) / 2;
                left = 0;
				
            }
			
            child.layout(left, top, nW, nH);
			
            Log.d("layout", "left:" + left);
            Log.d("layout", "top:" + top);
            Log.d("layout", "right:" + nW);
            Log.d("layout", "bottom:" + nH);
			
        }
		
    }

    public void surfaceCreated(SurfaceHolder holder) {
		
		Log.d(TAG, "LOL: surfaceCreated");
		
        // The Surface has been created, acquire the camera and tell it where to draw.
        try {
           
			if (mCamera != null) {
                mSurfaceView.setWillNotDraw(false);
                mCamera.setPreviewDisplay(holder);
            }
			
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
		
    }

    public void surfaceDestroyed(SurfaceHolder holder){
		
		Log.d(TAG, "LOL: surfaceDestroyed");
        
		// Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            mCamera.stopPreview();
        }
		
    }
	
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h){
		
		Log.d(TAG, "LOL: getOptimalPreviewSize function...");
		Log.d(TAG, "LOL: Looking for w = " + w + " h = " + h);
		
		final double ASPECT_TOLERANCE = 0.3;
        double targetRatio = (double) w / h;
		
        if(displayOrientation == 90 || displayOrientation == 270) {
            targetRatio = (double) h / w;
        }
		
        if(sizes == null){
			return null;
		}
		
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
		
        int targetHeight = h;
		
        // Try to find an size match aspect ratio and size
        for(Camera.Size size : sizes){
			
            double ratio = (double) size.width / size.height;
			
            if(Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE){
				continue;
			}
			
            if(Math.abs(size.height - targetHeight) < minDiff){
				
				Log.d(TAG, "LOL: Setting size: w: " + size.width + " h: " + size.height);
                
				optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
				
            }
			
        }
		
        // Cannot find the one match the aspect ratio, ignore the requirement
        if(optimalSize == null) {
			
			Log.d(TAG, "LOL: Optimalsize is null so far...");
			
            minDiff = Double.MAX_VALUE;
            for(Camera.Size size : sizes){
                
				if(Math.abs(size.height - targetHeight) < minDiff){
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
				
            }
			
			optimalSize = sizes.get(0);
			
        }
		
        Log.d(TAG, "LOL: optimal preview size: w: " + optimalSize.width + " h: " + optimalSize.height);
        
		return optimalSize;
		
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
		
		Log.d(TAG, "LOL: surfaceChanged");
	    if(mCamera != null) {
			
		    // Now that the size is known, set up the camera parameters and begin
		    // the preview.
		    
			Camera.Parameters parameters = mCamera.getParameters();
				
				parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
				requestLayout();
				
			mCamera.setParameters(parameters);
			
		    mCamera.startPreview();
			
	    }
		
    }
	
    public byte[] getFramePicture(byte[] data, Camera camera){
		
		Log.d(TAG, "LOL: getFramePicture");
		
        Camera.Parameters parameters = camera.getParameters();
		
        int format = parameters.getPreviewFormat();
		
        //YUV formats require conversion
        if(format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {
            
			int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;
			
            // Get the YuV image
            YuvImage yuvImage = new YuvImage(data, format, w, h, null);
            
			// Convert YuV to Jpeg
            Rect rect = new Rect(0, 0, w, h);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(rect, 100, outputStream);
            
			return outputStream.toByteArray();
			
        }
		
        return data;
		
    }
	
    public void setOneShotPreviewCallback(Camera.PreviewCallback callback){
		
		Log.d(TAG, "LOL: setOneShotPreviewCallback");
		
        if(mCamera != null){
            mCamera.setOneShotPreviewCallback(callback);
        }
		
    }
	
}

class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    
	private final String TAG = "CustomSurfaceView";

    CustomSurfaceView(Context context){
        super(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
	
}
