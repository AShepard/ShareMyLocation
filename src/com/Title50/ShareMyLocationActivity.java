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
	private final String ADDR_BUILDING = "BUILDING_KEY";
	private final String ADDR_STREET = "STREET_KEY";
	private final String ADDR_CITY = "CITY_KEY";
	private final String ADDR_STATE = "STATE_KEY";
	private final String ADDR_ZIP = "ZIP_KEY";
	private final String ADDR_LONG = "LONG_KEY";
	private final String ADDR_LAT ="LAT_KEY";
	
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds	     
	private static final int GPS_SETTINGS_ACTIVITY = 0;
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
	
	//location listener for GPS
	protected MyLocationListener m_location_listener;
	
	/*
	 * First function called! 
	 * Will display loading screen while determining if GPS available
	 * if it is then it will get coords/address
	 * else display alert to turn on GPS or to just skip ahead 
	 */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
        
        //TODO Remove buttons (new layout to come)
        b_retrieve_location = (Button) findViewById(R.id.retrieve_location_button);
        b_end_app = (Button) findViewById(R.id.end_app_button);
        b_send_data = (Button) findViewById(R.id.send_tcp_data);
        
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
         * Button on click listeners
         */
		b_retrieve_location.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					/*TODO Move
					 * Attempt to get current address/GPS location
					 */
					//showCurrentLocation();
					launchAddressForm();
					
				}
		});       

		b_end_app.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				shutdownApp();
			}
		}); 
		
		b_send_data.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				/*TODO Remove
				 * Send tcp data
				 */
				connectToServer();
				
			}
		});
    }  
	 
	//-------------------------------------------------------------------
	// Send data to server
	//-------------------------------------------------------------------
	protected void connectToServer() {
		/*TODO Remove
		 * Send tcp data
		 */
		int result=-1;
		
		gps_tcp_client tcpClient = new gps_tcp_client();
		result = tcpClient.sendData(0,0);
		
		if(result==0) {
			displayMessage("Data transfer successful!");
		} else if(result==2) {
			displayMessage("Server is not reachable!");
		} else {
			displayMessage("Could not connect to server!");
		}
		
		tcpClient.closeComm();
	}
	
//-------------------------------------------------------------------
//Translate latitude/longitude to street address then populate form
//-------------------------------------------------------------------		
    protected void showCurrentLocation() {

        Location location = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String message = "";
        if (location != null) {
            message = String.format(
                    "Current Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude()
            );
            displayMessage(message);
            

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
		m_bldg_num = "Bldg num";
        m_street ="Street";
        m_state ="State";
        m_city ="City";
        m_zip ="Zip";
       
        //Change layout to the address form
        setContentView(R.layout.address_editor);
		
       // m_latitude = -5;
       // m_longitude = 12;
        
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
		
		/*
		 * TODO: Remove below
		 
		Intent intent = new Intent(getBaseContext(), AddressForm.class);
		//Create the bundle by passing in values associated with a Key
		Bundle bundle = new Bundle();
		
		bundle.putString(ADDR_BUILDING, m_bldg_num);
		bundle.putString(ADDR_STREET, m_street);
		bundle.putString(ADDR_CITY, m_state);
		bundle.putString(ADDR_STATE, m_city);
		bundle.putString(ADDR_ZIP, m_zip);
		
		intent.putExtras(bundle);
		startActivity(intent);
		*/
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
        	String message = String.format(
        			"Provider disabled by the user. GPS turned off"
            );
        	//displayMessage(message);
        	
        	//GPS is turned off
        	promptUserForGPS();
        	
        }

        public void promptUserForGPS() {
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
        	        	   startActivity(myIntent);
        	        	   
        	           }
        	       })
        	       .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   //just dismiss dialog
        	           }
        	           
        	       });
        	
        	AlertDialog dialog = builder.create();
        	dialog.show();   	
        }
        
        public void onProviderEnabled(String s) {
        	String message = String.format(
        			"Provider enabled by the user. GPS turned on"
            );
        	//displayMessage(message);
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