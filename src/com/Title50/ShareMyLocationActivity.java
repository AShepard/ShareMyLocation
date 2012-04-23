package com.Title50;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Address;
import android.location.Geocoder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
	private static final int CAMERA_ACTIVITY_KEY = 98345;
	private static final int SETTINGS_ACTIVITY_KEY = 13683;
	private static final int COMMENTS_ACTIVITY_KEY = 76849;
	
	//thumbnail
	private static int THUMBNAIL_HEIGHT = 48;
	private static int THUMBNAIL_WIDTH = 66;
	
	//putExtra keys for intents
	protected final String COMMENTS = "COMMENTS";
	
	//Option menu option keys
	private static final int EXIT_OPTION = 1;
	private static final int CLEAR_TEXT_OPTION = 2;
	private static final int OTHER_OPTION = 10;
	
	//These are keys relating to email and location settings activites
	
	/*
	private static final String ADDR_BUILDING = "BUILDING_KEY";
	private static final String ADDR_STREET = "STREET_KEY";
	private static final String ADDR_CITY = "CITY_KEY";
	private static final String ADDR_STATE = "STATE_KEY";
	private static final String ADDR_ZIP = "ZIP_KEY";
	private static final String ADDR_LONG = "LONG_KEY";
	private static final String ADDR_LAT ="LAT_KEY";
	*/
	
	//private Timer progress_bar_timer;
	private final double GPS_WAIT_TIME = 1.50;
	private final int SECONDS_TO_MILLISECONDS = 1000;
	
	private String m_loc_file_location;
	
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
	private String m_type;
	
	private String m_lat_str_col_name;
	private String m_long_str_col_name;
	
	private String m_address_col_name;
	private String m_state_col_name;
	private String m_city_col_name;
	private String m_zip_col_name;
	private String m_comments_col_name;
	private String m_date_col_name;
	private String m_type_col_name;
	
	private String m_comments;

	private TextView tv_latitude;
	private TextView tv_longitude;
	private EditText et_bldg_num;
	private EditText et_street;
	private EditText et_city;
	private EditText et_state;
	private EditText et_zip;
	
	private ImageView iv_user_pic;
	//TODO
	ProgressDialog dialog;
	private Timer m_timer;
/*
 * Objects used
 */
	protected MyLocationListener m_location_listener;

	AndroidCamera m_camera_tool;
	AndroidEmailTool m_email_tool;
	
	/*
	 * First function called! 
	 * Will display loading screen while determining if GPS available
	 * if it is then it will get coords/address
	 * else display alert to turn on GPS or to just skip ahead 
	 */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 
        super.onCreate(savedInstanceState);
        /*
         * Initialize address fields to null (or out of range)
         */
        
        /*
         * Determine if GPS enabled: perform getLocation/addressform/alertdialog
         */
        initialize();
        launchAddressForm();
        
	 }  
	 
	 @Override
	 public void onConfigurationChanged(Configuration newConfig) {
	   super.onConfigurationChanged(newConfig);
	   setContentView(R.layout.address_editor);
	   
	   initialize();
	   fillAddressForm();
	 }
	 
	 protected void initialize() {
			//Change layout to the address form
	        setContentView(R.layout.address_editor);
		   
	        m_bldg_num = "";
	        m_street ="";
	        m_state ="CA";
	        m_city ="Goleta";
	        m_zip ="93117";
	        m_lat_str = "";
	        m_long_str = "";
	        m_latitude = -99999;
	        m_longitude= -99999;
	        
	        //TODO implement type of location
	        m_type="";
	        
	        /*
	         * CSV column names
	         */
	        m_lat_str_col_name = "lat";
	    	m_long_str_col_name = "lng";
	    	m_date_col_name = "date";
	    	m_address_col_name = "address";
	    	m_city_col_name = "city";
	    	m_type_col_name = "type";
	    	m_state_col_name = "state";
	    	m_zip_col_name = "zip";
	    	m_comments_col_name = "Detail of Location";
	        
	        m_comments = "No Comments";
	        
	        m_loc_file_location = "";
	        
	        m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

	        m_location_listener = new MyLocationListener();
	        
	        m_locationManager.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER,
	                MINIMUM_TIME_BETWEEN_UPDATES,
	                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
	                m_location_listener
	        );

	        m_geocoder = new Geocoder(this, Locale.ENGLISH);
	        
	        m_camera_tool = new AndroidCamera();
	        m_email_tool = new AndroidEmailTool();
	        /*`
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
	        b_send_email = (Button) findViewById(R.id.b_send_email);
	        b_update_location = (Button) findViewById(R.id.b_update_location);
	        b_take_picture = (Button) findViewById(R.id.b_take_picture);
	        
	        iv_user_pic = (ImageView) findViewById(R.id.iv_user_pic);
		}
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 switch(requestCode) {
			 case SETTINGS_ACTIVITY_KEY:
				 	//user possibly changed GPS location
				 	updateLocation();
				 	
				 	break;
			 case CAMERA_ACTIVITY_KEY:
				 	//check if picture received
				 	m_camera_tool.setCameraResult(resultCode);
				 	
				 	ImageController image_control = new ImageController();
				 	Bitmap m_bmap = null;
				 	
				 	String path = m_camera_tool.getPictureLocation();
				 	BitmapFactory m_bmap_factory = new BitmapFactory();
				 	m_bmap = m_bmap_factory.decodeFile(path);
				 	
				 	Float width  = new Float(m_bmap.getWidth());
					Float height = new Float(m_bmap.getHeight());
					Float ratio = width/height;
					
					m_bmap = Bitmap.createScaledBitmap(m_bmap, (int)(THUMBNAIL_HEIGHT*ratio), THUMBNAIL_WIDTH, false);

					int padding = (THUMBNAIL_WIDTH - m_bmap.getWidth())/2;
					
					iv_user_pic.setPadding(padding, 0, padding, 0);
					iv_user_pic.setImageBitmap(m_bmap);
					
				 	
				 	
				 	
				 	break;
			case EMAIL_ACTIVITY_KEY:
				 	String message = "";
				 	if(resultCode==RESULT_CANCELED) {
				 		message = String.format("Email failed: %1$s",resultCode);
				 		
				 	} else {
				 		message = String.format("Email succeeded: %1$s",resultCode);
				 		shutdownApp();
				 	}
				 	
				 	displayMessage(message);
				 	
				 	break;
			case COMMENTS_ACTIVITY_KEY:
					//store comments
					if(data!=null) {
			 			m_comments = data.getExtras().getString(COMMENTS);
			 		}
					
					if(resultCode==1) {
						//displayMessage("Time to send email..." + m_comments);
						sendEmail();
					} else {
						//displayMessage("Continue Editing" + m_comments);
					}
			default:
				 	//UNKNOWN Activity started
				 	break;
		 }
	 }
	 
	 private void checkGpsStatus(boolean runDialog) {
		 if(m_locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
			 updateLocation();
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
	 private void updateLocation() {
		/*
		 * Updates GPS location
		 * then changes address fields
		 */
     	//m_timer.cancel();
		getCurrentLocation();
		fillAddressForm();
		//dialog.dismiss();
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

		//TODO need to fix
		//progressBar();
		//waitForTime();
		 
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
					    */
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
					  
					   message = String.format("Addr: %s", strReturnedAddress.toString());
            	  }
            	  else{
            		  message = String.format("Error: No Address returned!");
            	  }
        	 } catch (IOException e) {
            	  e.printStackTrace();
            	  message = String.format("Cannot get Address Please Enter Manually!");
            	  displayMessage(message);
        	 } 	 
    	} else {
    		displayMessage("Error getting Location");
    	}
    }  
	 
	//TODO: Fix
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
		
		
		//ProgressDialog dialog;
		dialog = new ProgressDialog(this);
	    dialog.setMessage("Waiting For GPS Signal");
	    //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    //dialog.setMax(wait_time);
	    dialog.setCancelable(true);
	    dialog.show();
	   
	    waitForTime();
	}
	
	private void waitForTime() {
		int wait_time = (int)GPS_WAIT_TIME * SECONDS_TO_MILLISECONDS;
		m_timer = new Timer();
		m_timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {
	        	updateLocation();
	        }
	        
	    }, wait_time, wait_time);
	
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
	     	m_lat_str = String.format("%1$s", m_latitude);
	     } else {
	     	m_lat_str = String.format("unknown");
	     }
	     if(m_longitude<=180 && m_longitude >=-180) {
	     	m_long_str = String.format("%1$s", m_longitude);
	     } else {
	     	m_long_str = String.format("unknown");
	     }
         
         tv_longitude.setText("Longitude: "+m_long_str);
         tv_latitude.setText("Latitude: "+m_lat_str);
         
         et_bldg_num.setText(m_bldg_num);
         et_street.setText(m_street);
         et_city.setText(m_city);
         et_state.setText(m_state);
         et_zip.setText(m_zip);
	}
	
	protected void storeAddressFields() {
		/*
		 * For each address field
		 */
		
		m_long_str = tv_longitude.getText().toString();
		m_lat_str = tv_latitude.getText().toString();
        
		m_bldg_num = et_bldg_num.getText().toString();
		m_street = et_street.getText().toString();
		m_city = et_city.getText().toString();
		m_state = et_state.getText().toString();
		m_zip = et_zip.getText().toString();
        
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
        		if(m_camera_tool.getPictureLocation()!="") {
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
		
		startActivityForResult(m_camera_tool.takePicture(), m_camera_tool.getActivityKey());
	}
/*
 * Function relating to EMAIL
 */
	private void continueToEmail() {
		Intent comment_intent = new Intent( "com.Title50.COMMENTS");
		comment_intent.putExtra(COMMENTS, m_comments);
 	    startActivityForResult(comment_intent, COMMENTS_ACTIVITY_KEY);

		
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
	
	private String generateCsvFile()
	{
		/*
		 * Create the CSV formatted output file
		 */
		//TODO fill in appends
		String message = "";
		
		/*
		 * Table column names
		 */
		message = message.concat(m_long_str_col_name);
		message = message.concat(", ");
		message = message.concat(m_lat_str_col_name);
		message = message.concat(", ");
		message = message.concat(m_date_col_name);
		message = message.concat(", ");
		message = message.concat(m_address_col_name);
		message = message.concat(", ");
		message = message.concat(m_city_col_name);
		message = message.concat(", ");
		message = message.concat(m_type_col_name);
		message = message.concat(", ");
		message = message.concat(m_state_col_name);
		message = message.concat(", ");
		message = message.concat(m_zip_col_name);
		message = message.concat(", ");
		message = message.concat(m_comments_col_name);
		message = message.concat(", ");
		message = message.concat("\n");
	    
		/*
	     * Table values 
	     */
		message = message.concat(m_long_str);
		message = message.concat(", ");
		message = message.concat(m_lat_str);
		message = message.concat(", ");
		message = message.concat("date");
		message = message.concat(", ");
		message = message.concat(m_bldg_num + " " + m_street);
		message = message.concat(", ");
		message = message.concat(m_city);
		message = message.concat(", ");
		message = message.concat(m_type);
		message = message.concat(", ");
		message = message.concat(m_state);
		message = message.concat(", ");
		message = message.concat(m_zip);
		message = message.concat(", ");
		message = message.concat(m_comments);
		
		
		return message;
	 }
	
	 private void sendEmail() {
		 
		/*
		 * Create attachment files
		 * then attach files to email and send
		 */
		File file = null; 
		String message = "";
		
		/*
		 * Update the comments field members from the textbox
		 */
		storeAddressFields();
		
		
		//TODO change based on date
		String file_name = "graffiti_" + System.currentTimeMillis() +".csv";
		String dir_name = "/Graffiti Files";
		/*
		 message = String.format(
					"COMMENTS: \n\n\nLat: %1$s\nLong: %2$s\nBuilding: %3$s\nStreet: %4$s\nCity: %5$s\nState: %6$s\nZip: %7$s\n",
					m_latitude, m_longitude, m_bldg_num, m_street, m_city, m_state, m_zip);
		*/
		//Create attachment to send with email
		
		message = generateCsvFile();
		if(message=="") {
			message = "Error";
			displayMessage("ERROR");
		}
		file = createLocationFile(file_name, dir_name, message);
		
		m_loc_file_location = file.getAbsolutePath();
		String pic_attachment = m_camera_tool.getPictureLocation();
		/*
		 * Set up email activity
		 */
		
		Intent email_intent = m_email_tool.sendEmail(m_loc_file_location, pic_attachment);
		
		/*
		 * Send email, or catch no email client
		 */
		try {
		    startActivityForResult(Intent.createChooser(email_intent, "Send mail..."), EMAIL_ACTIVITY_KEY);
		} catch (android.content.ActivityNotFoundException ex) {
			displayMessage("There are no email clients installed.");
		}
		//displayMessage(full_path);
	 }
/*
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
		    	 //displayMessage("you clicked on item "+item.getTitle());
		    	 shutdownApp();
		         return true;
		     case CLEAR_TEXT_OPTION:
		    	 //displayMessage("you clicked on item "+item.getTitle());
		    	 clearAddressForm();
		         return true;
		     case OTHER_OPTION:
		    	 //displayMessage("you clicked on item "+item.getTitle() + ": NOT IMPLEMENTED");
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
            //displayMessage(message);
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
/*
 * functions called when app is ending	
 */
    private void deleteTempFiles() {
    	File file = null;
    	boolean deleted = false;
    	
    	String pic_file_name = m_camera_tool.getPictureLocation();
    	if(pic_file_name!="") {
    		file = new File(pic_file_name);
    		deleted = file.delete();
    		if(deleted==false) {
    			displayMessage("Could not delete pic");
    		}
    	}
    	
    	if(m_loc_file_location!="") {
    		file = new File(m_loc_file_location);
    		deleted = file.delete();
    		if(deleted==false) {
    			displayMessage("Could not delete text file");
    		}
    	}
    }
    public void shutdownApp() {
    	deleteTempFiles();
		m_locationManager.removeUpdates(m_location_listener);

		this.finish();
    }
}