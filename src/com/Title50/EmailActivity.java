package com.Title50;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class EmailActivity extends Activity {
	//Keys for string values from calling activity
	private final String ADDR_BUILDING = "BUILDING_KEY";
	private final String ADDR_STREET = "STREET_KEY";
	private final String ADDR_CITY = "CITY_KEY";
	private final String ADDR_STATE = "STATE_KEY";
	private final String ADDR_ZIP = "ZIP_KEY";
	private final String ADDR_LONG = "LONG_KEY";
	private final String ADDR_LAT ="LAT_KEY";
	/*
	 * Strings/EditText/coordinates for address
	 */
	private String m_lat;
	private String m_long;
	
	private String m_bldg_num;
	private String m_street;
	private String m_state;
	private String m_city;
	private String m_zip;
	
	private Button b_send;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_test);
        
        Bundle extras = getIntent().getExtras(); 
        
        //TODO Move this to ShareMyLoc
        //  will then just send a message to this activity

        b_send = (Button)findViewById(R.id.b_send_email);
        
        b_send.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String message = "";
				message = String.format(
						"Lat: $1\nLong: $2\nBuilding: $3\nStreet: $4\nCity: $5\nState: $6\nZip: $7\n",
						m_lat, m_long, m_bldg_num, m_street, m_city, m_state, m_zip);
						
				Intent i = new Intent(Intent.ACTION_SEND);
		        i.setType("text/plain");
		        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"asdantius5@gmail.com"});
		        i.putExtra(Intent.EXTRA_SUBJECT, "Android Email Test");
		        i.putExtra(Intent.EXTRA_TEXT   , "This was sent from my phone\n" + message);
		        try {
		            startActivity(Intent.createChooser(i, "Send mail..."));
		        } catch (android.content.ActivityNotFoundException ex) {
		            Toast.makeText(EmailActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		        }
			}
        });       
        
    }
}