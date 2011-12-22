/**
 * Copyright (c) 2004, Adam Buckley All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of Adam Buckley nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package pt.sapo.mobile.android.connect.ntp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DecimalFormat;

import pt.sapo.mobile.android.connect.system.Log;

import android.text.format.DateUtils;

/**
 * NtpClient - an NTP client for Android. This will connect to an NTP server and determines the delta between
 * the system clock and the NTP time. The local clock offset calculation is implemented according to the SNTP
 * algorithm specified in RFC 2030.
 * 
 * @author Adam Buckley
 * @author Rui Roque (Adaptation and minor changes for Android)
 */
public class NTPClient {

	/**
	 * Log tag for this class.
	 */
	private static final String TAG = "NTPClient";
	
	/**
	 * The Public NTP Server.
	 */
	private static final String SERVER_NAME = "europe.pool.ntp.org";
		
	/**
	 * The delta time between the NTP server time and the local device time.
	 */
	private Long deltaTime;

	/**
	 * The NTP Public Server time.
	 */
	private Long serverTime;
	
	/**
	 * The number of milliseconds in the server Window.
	 */
	private static final long MILLIS_IN_SERVER_WINDOW = DateUtils.MINUTE_IN_MILLIS * 5;
	
	/**
	 * Timeout for the socket connection.
	 */
	private static final int TIMEOUT_MILLIS = (int) (DateUtils.SECOND_IN_MILLIS * 6);
	
	/**
	 * Constructor.
	 * Connects to the NTP Public Server and calculates the Delta Time and the Server Time.
	 * 
	 * @author Adam Buckley
	 * @author Rui Roque (Adaptation and minor changes for Android)
	 */
	public NTPClient() {
		// Send request
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			
			// Set the timeout so that we don't get blocked forever waiting for the package.
			socket.setSoTimeout(TIMEOUT_MILLIS);
			
			InetAddress address = InetAddress.getByName(SERVER_NAME);
			byte[] buf = new NtpMessage().toByteArray();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);

			// Set the transmit timestamp *just* before sending the packet
			NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);

			socket.send(packet);

			// Get response
			Log.d(TAG, "NTPClient() - NTP request sent, waiting for response...");
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			// Immediately record the incoming timestamp
			double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

			// Process response
			NtpMessage msg = new NtpMessage(packet.getData());

			// Corrected, according to RFC2030 errata
			double roundTripDelay = (destinationTimestamp - msg.originateTimestamp) - (msg.transmitTimestamp - msg.receiveTimestamp);
			double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;

			// Display response
			Log.d(TAG, "NTPClient() - NTP server: " + SERVER_NAME);
			Log.d(TAG, "NTPClient() - " + msg.toString());
			Log.d(TAG, "NTPClient() - Dest. timestamp:    " + NtpMessage.timestampToString(destinationTimestamp));
			Log.d(TAG, "NTPClient() - Round-trip delay:   " + new DecimalFormat("0.00").format(roundTripDelay * 1000) + " ms");
			Log.d(TAG, "NTPClient() - Local clock offset: " + new DecimalFormat("0.00").format(localClockOffset * 1000) + " ms");
			
			double utcReceiveTimestamp = msg.receiveTimestamp - (2208988800.0);
			long longReceiveTimestamp = (long) (utcReceiveTimestamp * 1000.0);
			
			deltaTime = (long) (localClockOffset * 1000);
			serverTime = longReceiveTimestamp;
			
		} catch (InterruptedIOException e) {
			Log.e(TAG, "NTPClient() - InterruptedIOException", e);
		} catch (SocketException e) {
			Log.e(TAG, "NTPClient() - SocketException", e);
		} catch (IOException e) {
			Log.e(TAG, "NTPClient() - IOException", e);
		} finally {
			if (socket != null) {
				socket.close();	
			}
		}
		
	}
	
	/**
	 * Determines if the NTP Client was successful initialized.
	 * 
	 * @return True if the NTP Client is OK, false otherwise.
	 * @author Rui Roque
	 */
	public boolean isStatusOk() {
		return (deltaTime != null && serverTime != null);
	}
	
	/**
	 * Gets the Delta Time.
	 * 
	 * @return The delta time between the NTP server time and the local device time.
	 * @author Rui Roque
	 */
	public Long getDeltaTime() {
		return deltaTime;
	}
	
	/**
	 * Gets the NTP Public Server time.
	 * 
	 * @return The NTP Public Server time.
	 * @author Rui Roque
	 */
	public Long getServerTime() {
		return serverTime;
	}
	
	/**
	 * Determines if the delta is sufficient to pass by the OAuth server time window.
	 * 
	 * @return True if the delta in the device is OK to use OAuth. False, otherwise.
	 * @author Rui Roque
	 */
	public boolean isTimeWithinAcceptableOffset() {
		return (Math.abs(deltaTime) <= MILLIS_IN_SERVER_WINDOW); 
	}
	
}