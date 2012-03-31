package com.Title50;
//package com.javacodegeeks.android.lbs;
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
import android.widget.TextView;
import android.widget.Toast;

public class ShareMyLocationActivity extends Activity {

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds	     
	private static final int GPS_SETTINGS_ACTIVITY = 0;
	
	protected LocationManager m_locationManager;
	protected Geocoder m_geocoder;
	
	protected Button retrieveLocationButton;
	protected Button endAppButton; 
	
	protected Button sendTCPButton;
	
	protected View gpsContextMenu;
	
	final Context myContext = this;
	
	protected MyLocationListener locationListener;
		 @Override
		 public void onCreate(Bundle savedInstanceState) {

	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);

	        retrieveLocationButton = (Button) findViewById(R.id.retrieve_location_button);
	        endAppButton = (Button) findViewById(R.id.end_app_button);
	        sendTCPButton = (Button) findViewById(R.id.send_tcp_data);
	        
 	        m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

	        locationListener = new MyLocationListener();
	        
	        m_locationManager.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER,
	                MINIMUM_TIME_BETWEEN_UPDATES,
	                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
	                locationListener
	        );

	        m_geocoder = new Geocoder(this, Locale.ENGLISH);

	        /*
	         * Button on click listeners
	         */
			retrieveLocationButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						showCurrentLocation();

					}
			});       

			endAppButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					shutdownApp();
				}
			}); 
			
			sendTCPButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					/*
					 * Send tcp data
					 */
					connectToServer();
					
				}
			});
	    }   

		protected void connectToServer() {
			/*
			 * Send tcp data
			 */
			int result=-1;
			
			gps_tcp_client tcpClient = new gps_tcp_client();
			result = tcpClient.sendData(0,0);
			
			if(result==0) {
				displayMessage("Data transfer successful!");
			} else {
				displayMessage("Could not connect to server!");
			}
			
			tcpClient.closeComm();
		}
		protected void displayMessage(String message) {
			if(message.length()<=0) {
				return;
			}
			Toast.makeText(ShareMyLocationActivity.this, message,
                  Toast.LENGTH_LONG).show();
		}
	    protected void showCurrentLocation() {

	        Location location = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	        String message = "";
	        if (location != null) {
	            message = String.format(
	                    "Current Location \n Longitude: %1$s \n Latitude: %2$s",
	                    location.getLongitude(), location.getLatitude()
	            );
	            Toast.makeText(ShareMyLocationActivity.this, message,
	                    Toast.LENGTH_LONG).show();

	            try {
	            	  double longitude= location.getLongitude();
	            	  double latitude = location.getLatitude();

	            	  int maxResults = 5;
	            	  List<Address> addresses = m_geocoder.getFromLocation(latitude, longitude, maxResults);

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

	            	 Toast.makeText(ShareMyLocationActivity.this, message,
	 	                    Toast.LENGTH_LONG).show();
	        	}

	    }  


	    public void shutdownApp() {
			m_locationManager.removeUpdates(locationListener);

			this.finish();
	    }

	    private class MyLocationListener implements LocationListener {

	        public void onLocationChanged(Location location) {
	            String message = String.format(
	                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
	                    location.getLongitude(), location.getLatitude()
	            );
	            //Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	        public void onStatusChanged(String s, int i, Bundle b) {
	        	String message = String.format(
	        			"Provider status changed"
	            );
	        	//Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	        public void onProviderDisabled(String s) {
	        	String message = String.format(
	        			"Provider disabled by the user. GPS turned off"
	            );
	        	Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        	
	        	//GPS is turned off
	        	promptUserForGPS();
	        	
	        }
    
	        public void promptUserForGPS() {
	        	//TODO: User prompted to turn on GPS
	        	
	        	/*
	        	 * create dialogue for user to turn on GPS or skip to userform
	        	 */
	        	
	        	
	        	AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
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
	        	//Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	}

}