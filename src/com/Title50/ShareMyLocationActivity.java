package com.Title50;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.Menu;
import android.view.MenuItem;
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
	//Activity onResult Keys
	private static final int EMAIL_ACTIVITY_KEY = 51212;
	private static final int TAKE_PICTURE_KEY = 153256;
	private static final int SETTINGS_ACTIVITY_KEY = 13683;
	
	//Option menu option keys
	private static final int EXIT_OPTION = 1;
	private static final int CLEAR_TEXT_OPTION = 2;
	private static final int OTHER_OPTION = 10;
	
	//These are keys relating to email and location settings activites
	private final String ADDR_BUILDING = "BUILDING_KEY";
	private final String ADDR_STREET = "STREET_KEY";
	private final String ADDR_CITY = "CITY_KEY";
	private final String ADDR_STATE = "STATE_KEY";
	private final String ADDR_ZIP = "ZIP_KEY";
	private final String ADDR_LONG = "LONG_KEY";
	private final String ADDR_LAT ="LAT_KEY";
	
	private Timer progress_bar_timer;
	private boolean show_progess;
	private final double GPS_WAIT_TIME = 3.00;
	private final int SECONDS_TO_MILLISECONDS = 1000;
	
	
	private String m_picture_location;
	private final String EMAIL_ADDR = "asdantius5@gmail.com";
	
	
	
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds	 
	private final Context MY_CONTEXT = this;
	
	protected LocationManager m_locationManager;
	protected Geocoder m_geocoder;
	
	
	//AddressForm buttons
	protected Button b_retrieve_location;
	protected Button b_end_app; 
	protected Button b_send_data;
	protected Button b_update_location;
	//protected Button b_exit;
	protected Button b_send_email;
	//protected Button b_clear_info;
	protected Button b_take_picture;
	
	protected View v_gps_context_menu;
	
	/*
	 * Strings/EditText/coordinates for addressForm
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
        show_progess = false;
        m_picture_location = "";
        
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
        launchAddressForm();
        
	 }  
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 switch(requestCode) {
			 case SETTINGS_ACTIVITY_KEY:
				 	//user possibly changed GPS location
				 	checkGpsStatus(false);
				 	break;
			 //TODO: Picture result
			 case TAKE_PICTURE_KEY:
				 	//check if picture recieved
				 	displayMessage("Picture result: " + resultCode);
				 	if (resultCode != 0) {
				 		//TODO: see if can get picture name from "data" parameter
				 	} else {
				 		//reset picture location
				 		m_picture_location = "";
				 	}
				 	break;
			case EMAIL_ACTIVITY_KEY:
				 	String message = "";
				 	if(resultCode==0) {
				 		message = String.format("Email failed: %1$s",resultCode);
				 	} else {
				 		message = String.format("Email succeeded: %1$s",resultCode);
				 		shutdownApp();
				 	}
				 	
				 	//displayMessage(message);
				 	
				 	break;
			 default:
				 	//UNKNOWN Activity started
				 	break;
		 }
	 }
	 
	 private void checkGpsStatus(boolean runDialog) {
		 if(m_locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
			 updateLocation(true);
		 } else {
			 if(runDialog) {
				 promptUserForGPS();
			 } else {
				 //do nothing, user already prompted
			 }
		 }
	 }
	 /*
	 * This is called if user selects to update GPS
	 */
	 private void updateLocation(boolean insertAddress) {
		/*
		 * Updates GPS location
		 * then changes address fields
		 */
		getCurrentLocation();
		fillAddressForm();
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
	        	        	   * the answer is caught in onActivityResult with SETTINGS_ACTIVITY_KEY
	        	        	   */
	        	        	   Intent myIntent = new Intent( Settings.ACTION_SECURITY_SETTINGS );
	        	        	   startActivityForResult(myIntent, SETTINGS_ACTIVITY_KEY);
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
	 
//-------------------------------------------------------------------
//Translate latitude/longitude to street address then populate form
//-------------------------------------------------------------------		
	 protected void getCurrentLocation() {

		progressBar();
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
	            	  message = String.format("Cannot get Address Please Enter Manually!");
	            	  displayMessage(message);
            	 }

            	 
        	}
    }  
	 
	private void progressBar() {
		/*
		 * have spinner come up to indicate getting GPS location
		 * wait X seconds then end
		 */
	
		/*
		 * Start timer, will change show_progess to false
		 */
		//progress_bar_timer = new Timer();;
		//progress_bar_timer.schedule(new ProgressBarTask(), wait_time);
		
		//TODO: GPS_WAIT_TIME
		ProgressDialog dialog;
		dialog = new ProgressDialog(this);
	    dialog.setMessage("Waiting For GPS Signal");
	    //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    //dialog.setMax(wait_time);
	    dialog.setCancelable(true);
	    dialog.show();
	    
	    show_progess = true;
	    
	    waitForTime();
	    //dialog.dismiss();
	}
	
	private void waitForTime() {
		int wait_time = (int)GPS_WAIT_TIME * SECONDS_TO_MILLISECONDS;
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {
	        	show_progess = false;
	            cancel();
	            
	        }
	    }, wait_time, wait_time);
		while(show_progess) {
			
		}
	}
	//-------------------------------------------------------------------
 	//Insert values into form if they exist, allow user to modify
 	//-------------------------------------------------------------------
	private void clearAddressForm() {
    	m_bldg_num = "";
    	m_street = "";
    	m_city = "";
    	m_state = "";
    	m_zip = "";
         
     	m_lat_str = String.format("");
     	m_long_str = String.format("");
     	
     	m_latitude = -99999;
     	m_longitude = -99999;
     	
     	fillAddressForm();
	}
	private void fillAddressForm() {
		/*
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
         
	     if(m_latitude<=180 && m_latitude >=-180) {
	     	m_lat_str = String.format("Latitude: %1$s", m_latitude);
	     } else {
	     	m_lat_str = String.format("Latitude: unknown");
	     }
	     if(m_longitude<=180 && m_longitude >=-180) {
	     	m_long_str = String.format("Longitude: %1$s", m_longitude);
	     } else {
	     	m_long_str = String.format("Longitude: unknown");
	     }
         
         tv_longitude.setText(m_long_str);
         tv_latitude.setText(m_lat_str);
         
         et_bldg_num.setText(m_bldg_num);
         et_street.setText(m_street);
         et_city.setText(m_city);
         et_state.setText(m_state);
         et_zip.setText(m_zip);
	}
	/*
	 * Display address form
	 */
	protected void launchAddressForm() {
		/*
		 * Check to see if GPS enabled
		 * Prompt user to change
		 * load address info, allow user to edit loc info
		 */
		
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
        
        //Buttons on page
       // b_exit = (Button) findViewById(R.id.b_exit);
        b_send_email = (Button) findViewById(R.id.b_send_email);
        b_update_location = (Button) findViewById(R.id.b_update_location);
       // b_clear_info = (Button) findViewById(R.id.b_clear_info);
        b_take_picture = (Button) findViewById(R.id.b_take_picture);
        
        //fill with default values
        fillAddressForm();
        //check to see if GPS available, prompt if not
		checkGpsStatus(true);
        
        b_update_location.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*
				 * update users location
				 */
				checkGpsStatus(true);
			}
        });
        
        b_send_email.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/*
				 * send email with location data
				 */
				continueToEmail();
			}
        });
		
        b_take_picture.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
				/*
				 * send email with location data
				 */
        		//if pic exists, then ask if they want it replaced
        		if(m_picture_location!="") {
        			changePictureDialog();
        		} else {
        			takePicture();
        		}
			}
        });
	}
/*
 * Functions for taking a picture
 */
	private void changePictureDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MY_CONTEXT);
     	builder.setMessage("A picture was already taken! Do you want to replace it?")
     	       .setCancelable(false)
     	       .setPositiveButton("Retake Picture", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	        	  /*
     	        	   * User is directed to comments section
     	        	   */
     	        	  takePicture();
     	           }
     	       })
     	       .setNegativeButton("Keep current picture", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	        	   //do nothing, dismiss dialog
     	           }
     	           
     	       });
     	
     	AlertDialog dialog = builder.create();
     	dialog.show();
	}
	private void takePicture() {
		
		String file_name = "aab_picture.jpeg";
		String home_dir = "/sdcard";
		File dir=null;
		String dir_name =  home_dir +"/AAB_FILES";
		dir = new File(dir_name);
		if(dir.mkdirs() == false) {
			//already created
		}
		File picture_file = new File(dir_name, file_name);
		
		m_picture_location = picture_file.getAbsolutePath();
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture_file));
	    startActivityForResult(takePictureIntent, TAKE_PICTURE_KEY);
	}
/*
 * Function relating to EMAIL
 */
	private void continueToEmail() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MY_CONTEXT);
     	builder.setMessage("Are you sure you want to continue?")
     	       .setCancelable(false)
     	       .setPositiveButton("Continue to send attachment via email", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	        	  /*
     	        	   * User is directed to comments section
     	        	   */
     	        	   sendEmail();
     	           }
     	       })
     	       .setNegativeButton("Edit information", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	        	   //do nothing, dismiss dialog
     	           }
     	           
     	       });
     	
     	AlertDialog dialog = builder.create();
     	dialog.show();
	 }
	
	private File createLocationFile(String file_name, String dir_name, String message) {
		 
		 String homeDir="";
		 File dir=null;
		 File file = null;
		//create file writer
		try {
			homeDir = "/sdcard";
			dir = new File (homeDir + dir_name);
			
			if(dir.mkdirs() == false) {
				//already created
			}
					
			//creates file in directory with given FileName
			//if cannot open file, make new file
			file = new File(dir, file_name);
			
			FileOutputStream os = new FileOutputStream(file, true); 
			OutputStreamWriter out = new OutputStreamWriter(os);
			out.write(message);
			out.close();
		 } catch(Exception e) {
			message = String.format("File failed");
			displayMessage(message);
			return null;
		 }
		 
		 return file;
	 }
	//TODO: figure out how to attach 2 files
	 private void sendEmail() {
		 
		/*
		 * Create attachment file
		 * then attach file to email and send
		 */
		File file = null; 
		String message = "";
		//TODO change based on date and location
		String file_name = "results1111.txt";
		String dir_name = "/AAB_FILES";
		message = String.format(
					"COMMENTS: \n\n\nLat: %1$s\nLong: %2$s\nBuilding: %3$s\nStreet: %4$s\nCity: %5$s\nState: %6$s\nZip: %7$s\n",
					m_latitude, m_longitude, m_bldg_num, m_street, m_city, m_state, m_zip);
		
		//Create attachment to send with email
		file = createLocationFile(file_name, dir_name, message);
		
		/*
		 * Set up email activity
		 */
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{EMAIL_ADDR});
		intent.putExtra(Intent.EXTRA_SUBJECT, "Android Email Test");
		
		Uri uri=null;
		String full_path="none";
		
		//attach data as attachment or text-body
		if(file.exists()) {
			full_path = String.format("file://"+file.getAbsolutePath());
			uri = Uri.parse(full_path);
			intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
		} else {
			//if unable to find file, then send data via text
			intent.putExtra(Intent.EXTRA_TEXT   , "This was sent from my phone\n" + message);
		}
		
		//attach picture if it exists
		if(m_picture_location != "") {
			full_path = String.format("file://"+m_picture_location);
			uri = Uri.parse(full_path);
			intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
		}
		
		/*
		 * Send email, or catch no email client
		 */
		try {
		    startActivityForResult(Intent.createChooser(intent, "Send mail..."), EMAIL_ACTIVITY_KEY);
		} catch (android.content.ActivityNotFoundException ex) {
			displayMessage("There are no email clients installed.");
		}
		displayMessage(full_path);
	 }
/*
 * TODO: figure out what should be added here
 * Options Menu:
 * allows user to exit
 */
	 @Override
     public boolean onCreateOptionsMenu(Menu menu){
	     menu.add(1, EXIT_OPTION, 1, "Exit");
	     menu.add(1, CLEAR_TEXT_OPTION, 2, "Clear Text Boxes");
	     return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
		     case EXIT_OPTION:
		    	 displayMessage("you clicked on item "+item.getTitle());
		    	 shutdownApp();
		         return true;
		     case CLEAR_TEXT_OPTION:
		    	 displayMessage("you clicked on item "+item.getTitle());
		    	 clearAddressForm();
		         return true;
		     case OTHER_OPTION:
		    	 displayMessage("you clicked on item "+item.getTitle() + ": NOT IMPLEMENTED");
		    	 return true;
		     default:
		    	 displayMessage("Not sure what you clicked" + item.getItemId());
		    	 return false;
	     }
	     //return super.onOptionsItemSelected(item);

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