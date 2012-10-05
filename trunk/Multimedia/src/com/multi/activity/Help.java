package com.multi.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class Help extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.help);
		
	}
	public void btnClicked(View v){
		switch (v.getId()) {

		default:
			break;
		}
	}
}
