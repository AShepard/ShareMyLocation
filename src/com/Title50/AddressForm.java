package com.Title50;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class AddressForm extends Activity{
	
	/*
	 * Strings/EditText/coordinates for address
	 */
	private double m_lat;
	private double m_long;
	
	private String m_bldg_num;
	private String m_street_name;
	private String m_state;
	private String m_city;
	private String m_zip;
	
	private EditText et_bldg_num;
	private EditText et_street_name;
	private EditText et_state;
	private EditText et_city;
	private EditText et_zip;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
		
	}
}
