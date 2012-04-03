package com.Title50;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class AddressForm extends Activity{
	//keys for recieving extra data
	private final String ADDR_STREET = "STREET_KEY";
	private final String ADDR_CITY = "CITY_KEY";
	private final String ADDR_STATE = "STATE_KEY";
	private final String ADDR_ZIP = "ZIP_KEY";
	private final String ADDR_LONG = "LONG_KEY";
	private final String ADDR_LAT ="LAT_KEY";
	/*
	 * Strings/EditText/coordinates for address
	 */
	private double m_lat;
	private double m_long;
	
	private String m_bldg_num;
	private String m_street;
	private String m_state;
	private String m_city;
	private String m_zip;
	
	private EditText et_bldg_num;
	private EditText et_street;
	private EditText et_city;
	private EditText et_state;
	private EditText et_zip;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.address_editor);
        
        /*
         * Objects referencing the Edit Text fields 
         * to set up listeners/insert data into them
         */
        et_bldg_num = (EditText) findViewById(R.id.et_bldg_num);
        et_street = (EditText) findViewById(R.id.et_street);
        et_city = (EditText) findViewById(R.id.et_city);
        et_state = (EditText) findViewById(R.id.et_state);
        et_zip = (EditText) findViewById(R.id.et_city);
        
        
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null) {
		   // m_city = extras.getString(ADDR_CITY);
		    m_state = extras.getString(ADDR_STATE);
		    m_zip = extras.getString(ADDR_ZIP);
		}
		
		if(m_state!="") {
			et_state.setText(m_state);	
		} else {
			et_state.setText("No state entered");
		}
		
		if(m_zip!="") {
			et_zip.setText(m_zip);	
		} else {
			et_zip.setText("No zip entered");
		}
		
		
	}
}
