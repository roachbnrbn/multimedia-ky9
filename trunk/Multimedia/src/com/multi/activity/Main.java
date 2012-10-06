package com.multi.activity;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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

import com.lib.opencv.custom.BaseLoaderCallbackCustom;

public class Main extends Activity {
	private static final String TAG = "Main";
	boolean autoRefresh=false;
	boolean processLikeYou=true;
	
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
				try{
					preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				}catch(NullPointerException e){
					Toast.makeText(Main.this, "Can not connect to Camera", Toast.LENGTH_SHORT).show();
					updateCamera();
					e.printStackTrace();
				}
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
    private BaseLoaderCallbackCustom mOpenCVCallBack = new BaseLoaderCallbackCustom(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				updateCamera();
			}
				break;
			case LoaderCallbackInterface.INSTALL_CANCELED:
				Log.d(TAG, "OpenCV library instalation was canceled by user");
				break;
			default: {
				Toast.makeText(Main.this, "Please install opencv Lib before!", Toast.LENGTH_SHORT).show();
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
			
			updateCamera();
			
			try{
				someOneLikeYou(bmp);
			}catch (UnsatisfiedLinkError e) {
				e.printStackTrace();
			}catch (IllegalStateException e) {
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
    }
	private double someOneLikeYou(Bitmap bmp) throws UnsatisfiedLinkError,IllegalStateException{
		if(bmp == null)
			return -1;
		// Load Library OpenCV
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack)) {
			Log.e(TAG, "Cannot connect to OpenCV Manager");
			return -1;
		}
		// Save Image to SdCard
		//Toast.makeText(this, nameBmp, Toast.LENGTH_SHORT).show();
		
		// PreProcessing - Face Detection
		bmp=faceDetection(bmp);
		
		Bitmap dst=bmp;
		// Processing - Face Recognition
		double percentMatching=faceRecognition(bmp,dst);
		
		// Preview
		if(preview.setBackground(dst)){
			Toast.makeText(this, percentMatching + " %matching", Toast.LENGTH_SHORT).show();
			return percentMatching;
		}else
			Toast.makeText(this, "can not draw", Toast.LENGTH_SHORT).show();
		return -1;
    }
	/**
	 * @param nameBmp: directory of image to recognize
	 * @param dst: bitmap matched
	 * @return percent matching.
	 * */
	private double faceRecognition(Bitmap src,Bitmap dst){
		// get file name from src to nameBmp!
		Mat matCurrent = Highgui.imread(nameBmp);
		if(processLikeYou)
			Imgproc.cvtColor(matCurrent, matCurrent, Imgproc.COLOR_RGB2GRAY);
			
		Bitmap rslt=Main.bmp;
		Utils.matToBitmap(matCurrent, rslt);
		dst=rslt;
		return 90;
	}
	/**
	 * @param bmp: image source
	 * @return bitmap face
	 * */
	private Bitmap faceDetection(Bitmap bmp){
		return bmp;
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
