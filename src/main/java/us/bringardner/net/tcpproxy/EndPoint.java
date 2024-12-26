/**
 * <PRE>
 * 
 * Copyright Tony Bringarder 1998, 2025 <A href="http://bringardner.com/tony">Tony Bringardner</A>
 * 
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       <A href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</A>
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  </PRE>
 *   
 *   
 *	@author Tony Bringardner   
 *
 *
 * ~version~V000.01.02-V000.02.00-V000.01.01-V000.00.01-V000.00.00-
 */
package us.bringardner.net.tcpproxy;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import us.bringardner.core.util.AbstractCoreServer;



/**
 * Receive incoming TCP packets and forward them to another location
 * Creation date: (8/13/2001 6:58:03 PM)
 * @author: Tony Bringardner
 */
public class EndPoint extends Thread {
	public static final int NOT_STARTED = 0;
	public static final int RUNNING = 1;
	public static final int COMPLETE = 2;

	private InputStream in;
	private OutputStream out;
	private OutputStream capture;
	private AbstractCoreServer server;
	private int status = NOT_STARTED;
	private String desc;
	private int latency;
	@SuppressWarnings("unused")
	private Socket socket;

	/**
	 * EndPoint constructor comment.
	 */
	public EndPoint(Socket socket,AbstractCoreServer server,String whoami,InputStream i, OutputStream o) {
		this.socket = socket;
		this.server = server;
		desc = whoami;
		in = i;
		out = o;
		setName("EndPoint ("+whoami+")");
		if( server!=null) {
			server.logDebug(whoami+" open");
		}
	}

	/**
	 * EndPoint constructor comment.
	 */
	public EndPoint(AbstractCoreServer server,String whoami,InputStream i, OutputStream o, String captureFile) throws FileNotFoundException {
		this(null,server,whoami,i,o);

		if( captureFile != null && captureFile.length()>0) {
			//  This is used by EndPoint to do a binary capture
			capture = new FileOutputStream(captureFile);
		}
	}
	
	/**
	 * EndPoint constructor comment.
	 */
	public EndPoint(AbstractCoreServer server,String whoami,InputStream i, OutputStream o, String captureFile, int latency) throws FileNotFoundException {
		this(server,whoami,i,o,captureFile);
		this.latency = latency;
	}

	/**
	 * Close the end point (both in and out will be closed)
	 */
	public void close() {
		try {
			in.close();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 
	 * Creation date: (8/13/2001 7:29:56 PM)
	 * @return int
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * 
	 * Creation date: (8/13/2001 7:00:55 PM)
	 */
	public void run() {
		status = RUNNING;
		log(desc+" running");
		StringBuffer logBuffer = new StringBuffer();
		try {
			//String line = null;
			if( server != null ) {
				server.logDebug(desc+" Waiting for input id="+desc);
			}
			int cnt = 0;
			byte [] data = new byte[2048];
			
			while(isRunning() ) {
			
				try {
				cnt=in.read(data);
				if( cnt == -1) {				
					break;
				}
				} catch (SocketTimeoutException e) {
					// ignore				
				} catch (Exception e) {
					break;
				}
				
				if( latency > 0 ) {
					try {
						Thread.sleep(latency);
					} catch (Exception e) {
					}
				}
				
				try {
				out.write(data,0,cnt);
				out.flush();
				} catch(SocketTimeoutException e) {
					// ignore
				} catch (Exception e) {
					break;
				}
				logBuffer.append("EndPoint "+desc+" transfer "+(new String(data,0,cnt))+"\n");
				
				if( capture != null ) {
					if( isFormatAscii()) {
						capture.write(format(data,0,cnt));
					} else {
						capture.write(data,0,cnt);
					}
					capture.flush();
				}
			}
			
		} catch(Exception ex) {
			
			if( server!=null ) {
				server.logError(desc, ex);
			} else {
				System.err.println(desc+":"+ex);	
			}
		}

		try { in.close(); } catch(Exception ex) {}
		try { out.close(); } catch(Exception ex) {}
		if( capture != null ) {
			try { capture.close(); } catch(Exception ex) {}
		}
		in = null;
		out = null;

		status = COMPLETE;
		if( server != null ) {
			if (server instanceof ProxyServer) {
				ProxyServer ps = (ProxyServer) server;
				ps.incrementCloseCount();	
			}
			
		}
		log(desc+" close");
		
	}

	private boolean isRunning() {
		if( server != null ) {
			return server.isRunning();
		} else {
			return true;
		}
	}

	private boolean isFormatAscii() {
		boolean ret = true;
		if( server != null ) {
				if (server instanceof ProxyServer) {
					ProxyServer ps = (ProxyServer) server;
					ret = ps.isFormatAscii();	
				}
		}
		
		return ret;
	}

	private void log(String msg) {
		if( server != null ) {
			server.logDebug(msg);
		} else {
			System.out.println(msg);
		}
		
	}

	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}

	private byte[] format(byte[] data, int i, int cnt) {
		String prefix = "\n\t\"";
		StringBuffer ret = new StringBuffer("Buffer Size="+cnt+prefix);
		StringBuffer line = new StringBuffer();
		int sz = 0;
		for (int idx = i; idx < cnt; idx++) {
			
			if(data[idx] < 31 || data[idx] > 126 ) {
				line.append("\\x");
				sz+=2;
				String hex = Integer.toHexString(data[idx]);
				if( hex.length() < 2) {
					line.append('0');
					sz++;
				}
				line.append(hex);
				sz+= hex.length();
			} else {
				line.append((char)data[idx]);
				sz++;
			}
			if( sz > 56) {
				line.append("\"");
				ret.append(line);
				ret.append(prefix);
				line.setLength(0);
				sz = 0;
			}
		}
		
		if( line.length()>0) {
			ret.append(line);
		}
		ret.append("\"\n\n");
		return ret.toString().getBytes();
	}
}
