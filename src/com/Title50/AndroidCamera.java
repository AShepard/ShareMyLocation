package com.Title50;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

/*
 * This contains functions/members for camera side of AAB project
 */
public class AndroidCamera extends Activity {
	/*
	 * constant values: 
	 	* keys used by android to find info (eg camera  	
	 */
	private static final int CAMERA_ACTIVITY_KEY = 98345;
	
	private static final String BASE_PICTURE_NAME = "aab_picture_";
	private static final String PICTURE_EXTENSION = ".jpeg";
	private static final String HOME_DIRECTORY = "/sdcard";
	private static final String AAB_DIRECTORY = "/AAB_FILES";
	
	private String m_picture_location;
	private String m_directory;
	/*
	 * Constructor
	 */
	public AndroidCamera() {
		m_picture_location = "";
		m_directory =  HOME_DIRECTORY + AAB_DIRECTORY;
	}
	
	//activity intent key, never changes
	public final int getActivityKey() { return CAMERA_ACTIVITY_KEY; }
	
	public String getPictureLocation() { 
		if(m_picture_location!="") {
			return m_directory+"/"+m_picture_location; 
		} else {
			return "";
		}
	}
	
	
	//TODO see if need onActivityResult within this class
	public Intent takePicture() {
		
		m_picture_location = BASE_PICTURE_NAME + System.currentTimeMillis() + PICTURE_EXTENSION;
		File dir=null;
		
		dir = new File(m_directory);
		if(dir.mkdirs() == false) {
			//already created
		}
		File picture_file = new File(m_directory, m_picture_location);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture_file));
	    
	    return takePictureIntent;
	    //startActivityForResult(takePictureIntent, CAMERA_ACTIVITY_KEY);
	}

	/*
	 * Must be called when camera activity returns
	 */
	public void setCameraResult(int resultCode) {
		if(resultCode == 0) {
			//reset picture location
	 		m_picture_location = "";
		}
	}
}
