package com.Title50;
//package com.javacodegeeks.android.lbs;
import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ShareMyLocationActivity extends Activity {

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds	     
	
	protected LocationManager m_locationManager;
	protected Geocoder m_geocoder;
	
	protected Button retrieveLocationButton;
	protected Button endAppButton; 
	
	protected MyLocationListener locationListener;
		 @Override
		 public void onCreate(Bundle savedInstanceState) {

	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);

	        retrieveLocationButton = (Button) findViewById(R.id.retrieve_location_button);
	        endAppButton = (Button) findViewById(R.id.end_app_button);

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
		            	  // TODO Auto-generated catch block
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
	            Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	        public void onStatusChanged(String s, int i, Bundle b) {
	        	String message = String.format(
	        			"Provider status changed"
	            );
	        	Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	        public void onProviderDisabled(String s) {
	        	String message = String.format(
	        			"Provider disabled by the user. GPS turned off"
	            );
	        	Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	        public void onProviderEnabled(String s) {
	        	String message = String.format(
	        			"Provider enabled by the user. GPS turned on"
	            );
	        	Toast.makeText(ShareMyLocationActivity.this, message, Toast.LENGTH_LONG).show();
	        }

	}

}