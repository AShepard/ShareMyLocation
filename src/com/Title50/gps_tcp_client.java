package com.Title50;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class gps_tcp_client {
	//TODO: insert real values
	private static final int m_port_number = 1234;
	private static final String m_server_name = "server";
	
	private Socket m_socket;
	private PrintWriter m_server_put;
    private BufferedReader m_server_get;
    
	public gps_tcp_client() {
		m_socket = null;
		m_server_put = null;
		m_server_get = null;
	}
	
	public int sendData(double lat, double longitude) {
	/*
	 * Called by Android activity, passing args to send to server
	 * Establishes connection to server on given port
	 * sends 
	 * Returns 0 if data sent to server
	 */
		
		/*
		* Establish connection to server
		*/
		
		try{
			m_socket = new Socket(m_server_name, m_port_number);
			m_server_put = new PrintWriter(m_socket.getOutputStream(), true);
			m_server_get = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
			
		} catch(UnknownHostException e) {
			return 1;
		} catch (IOException e) {
			return 1;
		}
		
		/*
		 * Send Data
		 */
		
		
		/*
		 * Receive confirmation that data was received by server
		 */
		
		return 0;
	}
}
