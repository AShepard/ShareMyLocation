package com.Title50;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.provider.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * ----------------------------
 * This is the main class for sharing location
 * will send either GPS coordinates, address, or both with a server
 * ----------------------------
 *  This activity will capture GPS coordinates if available
 *  Call activity to fill address form
 *  Then send address to data server
 */

public class ShareMyLocationActivity extends Activity {
	//These are keys relating to email and location settings activites
	private final String ADDR_BUILDING = "BUILDING_KEY";
	private final String ADDR_STREET = "STREET_KEY";
	private final String ADDR_CITY = "CITY_KEY";
	private final String ADDR_STATE = "STATE_KEY";
	private final String ADDR_ZIP = "ZIP_KEY";
	private final String ADDR_LONG = "LONG_KEY";
	private final String ADDR_LAT ="LAT_KEY";
	
	private final String EMAIL_ADDR = "asdantius5@gmail.com";
	private static final int EMAIL_ACTIVITY_KEY = 51212;
	
	private final int SETTINGS_ACTIVITY_KEY = 13683;
	
	
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds	 
	private final Context MY_CONTEXT = this;
	
	protected LocationManager m_locationManager;
	protected Geocoder m_geocoder;
	
	protected Button b_retrieve_location;
	protected Button b_end_app; 
	protected Button b_send_data;
	
	protected View v_gps_context_menu;
	
	/*
	 * Strings/EditText/coordinates for address
	 */
	private double m_latitude;
	private double m_longitude;
	
	private String m_lat_str;
	private String m_long_str;
	
	private String m_bldg_num;
	private String m_street;
	private String m_state;
	private String m_city;
	private String m_zip;

	private TextView tv_latitude;
	private TextView tv_longitude;
	private EditText et_bldg_num;
	private EditText et_street;
	private EditText et_city;
	private EditText et_state;
	private EditText et_zip;
	
	private Button b_exit;
	private Button b_send_email;
	//location listener for GPS
	protected MyLocationListener m_location_listener;
	
	private boolean m_gps_enabled;
	private boolean m_wait_for_gps;
	/*
	 * First function called! 
	 * Will display loading screen while determining if GPS available
	 * if it is then it will get coords/address
	 * else display alert to turn on GPS or to just skip ahead 
	 */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        /*
         * Initialize address fields to null (or out of range)
         */
        m_bldg_num = "";
        m_street ="";
        m_state ="";
        m_city ="";
        m_zip ="";
        m_lat_str = "";
        m_long_str = "";
        m_latitude = -99999;
        m_longitude= -99999;
        m_gps_enabled = false;
        m_wait_for_gps= false;
        /*
        //TODO Remove buttons (new layout to come)
        b_retrieve_location = (Button) findViewById(R.id.retrieve_location_button);
        b_end_app = (Button) findViewById(R.id.end_app_button);
        b_send_data = (Button) findViewById(R.id.send_tcp_data);
        */
        m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        m_location_listener = new MyLocationListener();
        
        m_locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                m_location_listener
        );

        m_geocoder = new Geocoder(this, Locale.ENGLISH);
        
        /*
         * Determine if GPS enabled: perform getLocation/addressform/alertdialog
         */
        checkGpsStatus(true);
        
	 }  
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 switch(requestCode) {
			 case SETTINGS_ACTIVITY_KEY:
				 	checkGpsStatus(false);
				 	break;
			 case EMAIL_ACTIVITY_KEY:
				 	if(requestCode==0) {
				 		displayMessage("Email succeeded");
				 	} else {
				 		displayMessage("Email failed");
				 	}
				 	shutdownApp();
				 	break;
			 default:
				 	//UNKNOWN Activity started
				 	break;
		 }
	 }
	 
	 private void checkGpsStatus(boolean runDialog) {
		 if(m_locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
			 getCurrentLocation();
	         launchAddressForm();
		 } else {
			 if(runDialog) {
				 promptUserForGPS();
			 } else {
				 launchAddressForm();
			 }
		 }
	 }
	 
	 private void sendEmail() {
		 //TODO Need alert dialog
		 //Intent myIntent = new Intent(MY_CONTEXT, EmailActivity.class);
  	   	// startActivityForResult(myIntent, EMAIL_ACTIVITY_KEY);
		String message = "";
		message = String.format(
				"COMMENTS: \n\n\nLat: $1\nLong: $2\nBuilding: $3\nStreet: $4\nCity: $5\nState: $6\nZip: $7\n",
				m_latitude, m_longitude, m_bldg_num, m_street, m_city, m_state, m_zip);
				
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"asdantius5@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Android Email Test");
		i.putExtra(Intent.EXTRA_TEXT   , "This was sent from my phone\n" + message);
		try {
		    startActivityForResult(Intent.createChooser(i, "Send mail..."), EMAIL_ACTIVITY_KEY);
		} catch (android.content.ActivityNotFoundException ex) {
			displayMessage("There are no email clients installed.");
		}
	 }
	 private void promptUserForGPS() {
	    	/*
	    	 * create dialogue for user to turn on GPS or skip to userform
	    	 */

	        	AlertDialog.Builder builder = new AlertDialog.Builder(MY_CONTEXT);
	        	builder.setMessage("GPS is disabled. Enable GPS?")
	        	       .setCancelable(false)
	        	       .setPositiveButton("Change GPS Settings", new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	        	  /*
	        	        	   * User is directed to phone settings to turn on GPS
	        	        	   */
	        	        	   Intent myIntent = new Intent( Settings.ACTION_SECURITY_SETTINGS );
	        	        	   startActivityForResult(myIntent, SETTINGS_ACTIVITY_KEY);
	        	           }
	        	       })
	        	       .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
	        	           public void onClick(DialogInterface dialog, int id) {
	        	        	   //just dismiss dialog
	        	        	   launchAddressForm();
	        	           }
	        	           
	        	       });
	        	
	        	AlertDialog dialog = builder.create();
	        	m_wait_for_gps=true;
	        	dialog.show();   	
	        }
	
//-------------------------------------------------------------------
//Translate latitude/longitude to street address then populate form
//-------------------------------------------------------------------		
	 protected void getCurrentLocation() {

        Location location = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String message = "";
        if (location != null) {
            message = String.format(
                    "Current Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );
            //displayMessage(message);
            

            try {
            	  m_longitude= location.getLongitude();
            	  m_latitude = location.getLatitude();

            	  int maxResults = 5;
            	  List<Address> addresses = m_geocoder.getFromLocation(m_latitude, m_longitude, maxResults);

            	  int size = addresses.size();
            	  if(size != 0) {
					   Address returnedAddress = addresses.get(0);
					   StringBuilder strReturnedAddress = new StringBuilder("Address:\n");

					   for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
						   strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
					   }
					   
					   /*
					    * Populate address members
					    * Number and Street
					    * City, State, Zip

					    */
					   String temp = "";
					   //zip
					   m_zip = returnedAddress.getPostalCode();
					   //state
					   m_state = returnedAddress.getAdminArea();
					   //city
					   m_city = returnedAddress.getLocality();
					   //building num
					   m_bldg_num = returnedAddress.getSubThoroughfare();
					   //street
					   m_street = returnedAddress.getThoroughfare();
					  
					   

					  // m_street = returnedAddress.getLocality();
					  // m_city = returnedAddress.getSubLocality();
					   message = String.format("Addr: %s", strReturnedAddress.toString());
            	  }
            	  else{
            		  message = String.format("No Address returned!");
            	  }
            	 } catch (IOException e) {
	            	  e.printStackTrace();
	            	  message = String.format("Cannot get Address!");
            	 }

            	 displayMessage(message);
        	}
    }  
	//-------------------------------------------------------------------
 	//Insert values into form if they exist, allow user to modify
 	//-------------------------------------------------------------------
	protected void launchAddressForm() {
		/*
		 * Pass args to activity
		 * start activity (not for result, this will end current app)
		 */
		/* TODO: remove
		m_bldg_num = "Bldg num";
        m_street ="Street";
        m_state ="State";
        m_city ="City";
        m_zip ="Zip";
        m_latitude = -5;
        m_longitude = 12;
       */
		
		/*
		 * if below strings are null, then insert blanks
		 */
        if(m_bldg_num == null) {
        	m_bldg_num = "";
        }
        if(m_street == null) {
        	m_street = "";
        }
        if(m_city == null) {
        	m_city = "";
        }
        if(m_state == null) {
        	m_state = "";
        }
        if(m_zip == null) {
        	m_zip = "";
        }
         
         if(m_latitude<=90 && m_latitude >=-90) {
         	m_lat_str = String.format("Latitude: %1$s", m_latitude);
         } else {
         	m_lat_str = String.format("Latitude: unknown");
         }
         if(m_longitude<=90 && m_longitude >=-90) {
         	m_long_str = String.format("Longitude: %1$s", m_longitude);
         } else {
         	m_long_str = String.format("Longitude: unknown");
         }
         
        //Change layout to the address form
        setContentView(R.layout.address_editor);
	   
        /*
         * Get the EditTexts and needed TextViews from address form
         */
        tv_latitude = (TextView) findViewById(R.id.tv_latitude);
        tv_longitude = (TextView) findViewById(R.id.tv_longitude);
        
        et_bldg_num = (EditText) findViewById(R.id.et_bldg_num);
        et_street = (EditText) findViewById(R.id.et_street);
        et_city = (EditText) findViewById(R.id.et_city);
        et_state = (EditText) findViewById(R.id.et_state);
        et_zip = (EditText) findViewById(R.id.et_zip);
        
        b_exit = (Button) findViewById(R.id.b_exit);
        b_send_email = (Button) findViewById(R.id.b_send_email);
        /*
         * Fill in with address fields (if available on a per field basis)
         */
        
        tv_longitude.setText(m_long_str);
        tv_latitude.setText(m_lat_str);
        
        et_bldg_num.setText(m_bldg_num);
        et_street.setText(m_street);
        et_city.setText(m_city);
        et_state.setText(m_state);
        et_zip.setText(m_zip);
		
        b_exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				shutdownApp();
			}
        });
        
        b_send_email.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				displayMessage("Please Insert Comments only under Comments section");
				sendEmail();
			}
        });
		
	}
	
	
 //-------------------------------------------------------------------
 // Listener functions for GPS
 //-------------------------------------------------------------------
    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );
           // displayMessage(message);
        }

        public void onStatusChanged(String s, int i, Bundle b) {
        	String message = String.format(
        			"Provider status changed"
            );
        	//displayMessage(message);
        }

        public void onProviderDisabled(String s) {
        	m_gps_enabled = false;
        	String message = String.format(
        			"Provider disabled by the user. GPS turned off"
            );
        	//displayMessage(message);
        }

        
        
        public void onProviderEnabled(String s) {
        	m_gps_enabled = true;
        	String message = String.format(
        			"Provider enabled by the user. GPS turned on"
            );
        	//displayMessage(message);
        	//getCurrentLocation();
        }
	 
	}
	    
    protected void displayMessage(String message) {
		if(message.length()<=0) {
			return;
		}
		Toast.makeText(ShareMyLocationActivity.this, message,
              Toast.LENGTH_LONG).show();
	}
	    
    public void shutdownApp() {
		m_locationManager.removeUpdates(m_location_listener);

		this.finish();
    }
}