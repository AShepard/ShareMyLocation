package com.Title50;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class AndroidEmailTool extends Activity{
	private final String EMAIL_ADDR = "aabGIS2012@gmail.com";
	
	private String m_text_file;
	private String m_picture_file;
	
	
	public AndroidEmailTool() {
		m_text_file = "";
		m_picture_file = "";
	}
	
	public AndroidEmailTool(String text_attachment, String picture_attachment) {
		m_text_file = text_attachment;
		m_picture_file = picture_attachment;
	}
	
	public void setPictureFile(String picture_attachment) { m_picture_file = picture_attachment; }
	public void setTextFile(String text_attachment) { m_text_file = text_attachment; }
	
	public Intent sendEmail(String text_attachment, String picture_attachment) {
		m_text_file = text_attachment;
		m_picture_file = picture_attachment;
		
		return sendEmail();
	}
	
	public Intent sendEmail() {
		/*
		 * Set up email activity
		 */
		Intent email_intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		email_intent.setType("text/plain");
		email_intent.putExtra(Intent.EXTRA_EMAIL  , new String[]{EMAIL_ADDR});
		email_intent.putExtra(Intent.EXTRA_SUBJECT, "Android Email Test");
		
		Uri uri=null;
		String full_path="none";
		
		ArrayList<Uri> uri_list = new ArrayList<Uri>();
		//attach data as attachment or text-body
		if(m_text_file!= "") {
			full_path = String.format("file://"+m_text_file);
			uri = Uri.parse(full_path);
			uri_list.add(uri);
			//intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
		} else {
			//if unable to find file, then send data via text
			email_intent.putExtra(Intent.EXTRA_TEXT   , "This was sent from my phone\n");
		}
		
		//attach picture if it exists
		if(m_picture_file != "") {
			full_path = String.format("file://"+m_picture_file);
			uri = Uri.parse(full_path);
			uri_list.add(uri);
			//intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
		}
		
		if(uri_list.size()>0) {
			email_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri_list);
		}
		
		
		return email_intent;
	}
}
