package com.Title50;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class CommentsActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,  
	            WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	}
}
