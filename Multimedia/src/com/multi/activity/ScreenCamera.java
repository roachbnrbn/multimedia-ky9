package com.multi.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class ScreenCamera extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "ScreenCamera";

	SurfaceHolder mHolder;
	public Camera camera;
	public Canvas canvas=null;

	public ScreenCamera(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		try{
			camera.setDisplayOrientation(90);
			Camera.Parameters parameters = camera.getParameters();
			try{
				if(parameters.getMaxNumDetectedFaces()>0){
					camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
						
						@Override
						public void onFaceDetection(Face[] faces, Camera camera) {
							if(faces!=null){
								for(Face iFace: faces){
									int id=iFace.id;
									int score=iFace.score;
									Point leftEye=iFace.leftEye;
									Point mouth=iFace.mouth;
									Point rightEye=iFace.rightEye;
									Rect rect=iFace.rect;
									
									Log.e("Face Detection", id+score+leftEye.x+leftEye.y+mouth.x+rightEye.x+rect.bottom+"");
								}
							}
						}
					});
				}
			}catch(NoSuchMethodError e){
				e.printStackTrace();
			}
			parameters.setPreviewSize(w, h);
			camera.setParameters(parameters);
			camera.startPreview();
		}catch(NullPointerException e){
		}catch (Exception e) {
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			camera = Camera.open();
			camera.setPreviewDisplay(holder);

			camera.setPreviewCallback(new PreviewCallback() {

				public void onPreviewFrame(byte[] data, Camera arg1) {
					ScreenCamera.this.invalidate();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			stopPreviewAndFreeCamera(camera);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		stopPreviewAndFreeCamera(camera);
	}
	private void stopPreviewAndFreeCamera(Camera camera) {

	    if (camera != null) {
	        /*
	          Call stopPreview() to stop updating the preview surface.
	        */
	    	camera.stopPreview();
	    
	        /*
	          Important: Call release() to release the camera for use by other applications. 
	          Applications should release the camera immediately in onPause() (and re-open() it in
	          onResume()).
	        */
//	    	camera.release();
	    
	    	camera = null;
	    }
	}
	public static void setCameraDisplayOrientation(Activity activity,int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		this.canvas=canvas;
		mHolder.lockCanvas();
		
		mHolder.unlockCanvasAndPost(canvas);
	};
	
	public boolean setBackground(Bitmap bmp){
		if(bmp==null){
			Toast.makeText(this.getContext(), "Bmp null", Toast.LENGTH_SHORT).show();
			return false;
		}
		canvas=getHolder().lockCanvas(null);
		
		if(canvas ==null){
			canvas=new Canvas(bmp);
		}
		synchronized (mHolder) {
			Rect src=new Rect(0,0,bmp.getWidth(),bmp.getHeight());
			Rect dst=new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
			Paint p=new Paint();
			canvas.drawBitmap(bmp, src, dst,p );
		}
		mHolder.unlockCanvasAndPost(canvas);
		return true;
	}
}
