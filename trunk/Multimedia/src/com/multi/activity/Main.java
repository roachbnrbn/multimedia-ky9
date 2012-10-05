package com.multi.activity;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class Main extends Activity {
	private static final String TAG = "Main";
	boolean autoRefresh=false;
	//Camera camera;
	static ScreenCamera preview;
	Button buttonTake,buttonPhone,buttonHelp;
	ImageView imv;
	static Bitmap bmp=null;
	static String nameBmp="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        preview = new ScreenCamera(this);
        ((FrameLayout) findViewById(R.id.frame_layout_camera)).addView(preview);
        
		buttonTake = (Button) findViewById(R.id.button_take_picture);
		buttonHelp=(Button)findViewById(R.id.button_help);
		buttonPhone=(Button)findViewById(R.id.button_phone);
		imv=(ImageView)findViewById(R.id.image_view);
		
		buttonTake.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				
			}
		});
		buttonPhone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Main.this.autoRefresh=!autoRefresh;
				Toast.makeText(Main.this, "auto refresh:"+autoRefresh, Toast.LENGTH_SHORT).show();
			}
		});
		buttonHelp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(Main.this, Help.class));
			}
		});
		Log.d(TAG, "onCreate'd");
		Animation anim=AnimationUtils.loadAnimation(this, R.anim.button_alpha);
		buttonTake.startAnimation(anim);
		buttonHelp.startAnimation(anim);
		buttonPhone.startAnimation(anim);
		
		
    }
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				setContentView(R.layout.main);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
    public void btnClicked(View v){
    	switch (v.getId()) {
		case R.id.button_clock:
			if(bmp==null){
				Toast.makeText(this, "bitmap null", Toast.LENGTH_SHORT).show();
				return;
			}
			if(imv.getVisibility()==View.VISIBLE){
				imv.setVisibility(View.INVISIBLE);
			}else{
				imv.setImageBitmap(bmp);
				imv.setVisibility(View.VISIBLE);
			}
			someOneLikeYou(bmp);
			
			updateCamera();
			break;

		default:
			break;
		}
    }
	private void someOneLikeYou(Bitmap bmp){
//		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack)) {
//			Log.e(TAG, "Cannot connect to OpenCV Manager");
//			return;
//		}
//		Toast.makeText(this, nameBmp, Toast.LENGTH_SHORT).show();
		/*
		Mat matCurrent = Highgui.imread(nameBmp);
		try{
			Imgproc.cvtColor(matCurrent, matCurrent, Imgproc.COLOR_RGB2GRAY);
			Bitmap src=Main.bmp;
			Utils.matToBitmap(matCurrent, src);
			if(preview.setBackground(src)){
				return;
			}else
				Toast.makeText(this, "can not draw", Toast.LENGTH_SHORT).show();
		}catch (IllegalStateException e) {
			e.printStackTrace();
		}catch (UnsatisfiedLinkError e){
			e.printStackTrace();
		}
		*/
    }
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if(preview.camera!=null){
    		preview.camera.setPreviewCallback(null);
    		preview.camera.stopPreview();
    		preview.camera.release();
    		preview.camera=null;
    	}
    	
    	preview=null;
    }
    @Override
    protected void onPause() {
    	super.onPause();
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	updateCamera();
    }
    private void updateCamera(){
    	if(preview.camera!=null){
    		preview.camera.setPreviewCallback(null);
    		preview.camera.stopPreview();
    		preview.camera.release();
    		preview.camera=null;
    	}
    	preview=new ScreenCamera(this);
    	
    	((FrameLayout) findViewById(R.id.frame_layout_camera)).removeAllViews();
    	((FrameLayout) findViewById(R.id.frame_layout_camera)).addView(preview);
    }
        
    ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				Bitmap bmp=BitmapFactory.decodeByteArray(data, 0, data.length);
				Main.bmp=bmp;
				// write to local sandbox file system
//				outStream = CameraDemo.this.openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), 0);	
				// Or write to sdcard
				nameBmp=String.format(getString(R.string._sdcard_multi_d_jpg), System.currentTimeMillis());
				outStream = new FileOutputStream(nameBmp);	
				outStream.write(data);
				outStream.close();
				
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(autoRefresh)
					updateCamera();
			}
		}
	};
	
}
