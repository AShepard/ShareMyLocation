package com.Title50;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CommentsActivity extends Activity {
	protected final int LAUNCH_EMAIL = 1;
	protected final int CONTINUE_EDITING = -1;
	
	protected final String COMMENTS = "COMMENTS";
	private String m_comments;
	
	private EditText et_comments;
	private Button b_continue;
	private Button b_back;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments_dialog);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,  
	            WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		
		et_comments = (EditText)findViewById(R.id.et_comments); 
		et_comments.setText("No Comments");
		
		b_continue = (Button)findViewById(R.id.b_continue); 
		b_back = (Button)findViewById(R.id.b_back); 
		
		b_continue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*
				 * save comments, go to email
				 */
				Intent return_intent = new Intent();
				m_comments = et_comments.getText().toString();;
				return_intent.putExtra(COMMENTS, m_comments);
				setResult(LAUNCH_EMAIL);
				finish();
			}
        });
		
		b_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*
				 * update users location
				 */
				setResult(CONTINUE_EDITING);
				finish();
			}
        });
	}
}