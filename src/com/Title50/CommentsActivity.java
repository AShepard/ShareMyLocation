package com.Title50;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
		
		final Bundle extras = getIntent().getExtras();
		
		if(extras!=null) {
			m_comments =  (String) extras.getSerializable(COMMENTS);
	    } else {
	    	m_comments = "No Comments";
	    }
		
		et_comments = (EditText)findViewById(R.id.et_comments); 
		et_comments.setText(m_comments);
		
		b_continue = (Button)findViewById(R.id.b_continue); 
		b_back = (Button)findViewById(R.id.b_back); 
		
		b_continue.setOnClickListener(new OnClickListener() {

			//@Override
			public void onClick(View arg0) {
				/*
				 * send email
				 */
				
				endComments(LAUNCH_EMAIL);
			}
        });
		
		b_back.setOnClickListener(new OnClickListener() {

			//@Override
			public void onClick(View arg0) {
				/*
				 * continue editing
				 */
				endComments(CONTINUE_EDITING);
			}
        });
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    {
	    	endComments(CONTINUE_EDITING);
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public void endComments(int result_code) {
		/*
		 * save comments
		 * return with given result code
		 */
		
		Intent return_intent = new Intent();
		m_comments = et_comments.getText().toString();;
		return_intent.putExtra(COMMENTS, m_comments);
		setResult(result_code, return_intent);
		finish();
	}
}
