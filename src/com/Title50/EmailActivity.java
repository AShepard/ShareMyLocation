package com.Title50;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class EmailActivity extends Activity {
	private Button b_send;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_test);
        
        b_send = (Button)findViewById(R.id.b_send_email);
        
        b_send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
		        i.setType("text/plain");
		        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"asdantius5@gmail.com"});
		        i.putExtra(Intent.EXTRA_SUBJECT, "Android Email Test");
		        i.putExtra(Intent.EXTRA_TEXT   , "This was sent from my phone");
		        try {
		            startActivity(Intent.createChooser(i, "Send mail..."));
		        } catch (android.content.ActivityNotFoundException ex) {
		            Toast.makeText(EmailActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		        }
			}
        });       
        
    }
}