package com.multi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Logo extends Activity {
	public static final String TAG="class: Logo";
	
	Handler h = new Handler();
	int count = 0;
	int loop = 0;
	int wait = 0;
	boolean isClicked = false;
	ImageView layoutLogo;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logo);
				
		layoutLogo = (ImageView) findViewById(R.id.layout_logo);
		
		Thread t = new Thread() {
			public void run() {
				h.post(new Runnable() {

					
					public void run() {
						layoutLogo.startAnimation(AnimationUtils.loadAnimation(Logo.this, R.anim.button_alpha));
					}
				});
				try {
					while (!isClicked && wait < 4000) {
						Thread.sleep(100);
						wait += 100;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					h.post(new Runnable() {

						
						public void run() {
							Logo.this.finish();
							startActivity();
						}
					});

					isClicked = false;
				}
			};
		};
		t.start();
		/*
		 * //PROGRESS DIALOG ProgressDialog progressDialog; progressDialog = new
		 * ProgressDialog(this);
		 * progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		 * progressDialog.setMessage("Loading...");
		 * progressDialog.setCancelable(false);
		 */
	}

	public void logo_clicked(View arg0) {
		isClicked = true;
	}

	private void startActivity() {
		startActivity(new Intent(this,Main.class));
		this.finish();
	}
}
