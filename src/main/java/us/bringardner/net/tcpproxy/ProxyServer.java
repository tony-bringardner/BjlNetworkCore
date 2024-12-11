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

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.net.SocketFactory;
import javax.swing.JTextArea;

import us.bringardner.core.util.AbstractCoreServer;

/**
 * @author Tony Bringardner
 * 
 * This Server will listen for incoming connections on 
 * specified port and connect the incoming connections to another location
 * using the ForwardProcReq 
 *
 * @author tony
 *
 */
public class ProxyServer extends AbstractCoreServer {

	private class HostConfig {
		public String host;
		public int port;

		public HostConfig(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public String toString() {
			return host+":"+port;
		}
	}


	public static final String PROPERTY_DYNAMIC = "dynamic";
	public static final String PROPERTY_REMOTE_HOST = "remoteHost";
	public static final String PROPERTY_LOCAL_PORT = "localPort";
	public static final String PROPERTY_REMOTE_PORT = "remotePort";
	public static final String PROPERTY_TUNNEL = "tunnel";
	public static final String PROPERTY_CAPTURE = "capture";
	public static final String PROPERTY_LATENCY = "latency";


	private SocketFactory socketFactory = SocketFactory.getDefault();

	private boolean formatAscii = true;

	/**
	 * Supports setting the forward host:port dynamically
	 */
	private Boolean supportsDynamicConfig;

	/**
	 * The latency can be used to simulate network latency during testing.
	 */
	private int latency = 0;
	/**
	 * The host name of the remote end-point.
	 */
	private String remoteHost;	
	/**
	 * The port of the remote end-point.
	 */
	private int remotePort=-1;
	/**
	 * OutputStream of the capture log file (if enabled)
	 */
	private  OutputStream capture;
	private String captureFileName;
	/**
	 * The text area for monitoring activity (may be null); 
	 */
	private JTextArea text = null;



	/**
	 * How many incoming connections have we seen.
	 */
	private int connectionCount;
	/**
	 * How many incoming connection have been closed (current connection count = connectionCount - closeCount) 
	 */
	private int closeCount;


	public ProxyServer() {
		super();
	}

	public ProxyServer(int localPort) {
		super(localPort);
	}

	public ProxyServer(int localPort, int remotePort, String remoteHost)	{
		super(localPort, false);
		setRemotePort(remotePort);
		setRemoteHost(remoteHost);
		setName("ProxyServer:"+localPort);
	}

	public ProxyServer(int localPort, int remotePort, String remoteHost,String captureFile)	throws FileNotFoundException	{
		this(localPort,remotePort,remoteHost);

		if( captureFile != null && captureFile.length()>0) {
			capture = new FileOutputStream(captureFile);
			//  This is used by SocketForward to do a binary capture
			captureFileName = captureFile;
		}
	}



	/**
	 * @return the amount of time in milliseconds to delay before forwarding data. 
	 */
	public int getLatency() {
		return latency;
	}

	/**
	 * Set the amount of time in milliseconds to delay before forwarding data.
	 * 
	 * @param latency
	 */
	public void setLatency(int latency) {
		this.latency = latency;
	}

	/**
	 * Set the outputStream to use to capture data.
	 *  
	 * @param capture
	 */
	public void setCapture(OutputStream capture) {
		this.capture = capture;
	}

	/**
	 * @return the SocketFactory that should be used when creating remote connections.
	 */
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	/**
	 * Set the SocketFactory that should be used when creating remote connections.
	 * For Example, you can tunnel though fs.setSocketFactory(new TunnelSocketFactory());
	 * @param socketFactory
	 */
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	/**
	 * @return true if this proxy supports dynamic redirection
	 */
	public boolean isSupportsDynamicConfig() {
		return supportsDynamicConfig == null ? false:supportsDynamicConfig;
	}

	/**
	 * @param supportsDynamicConfig true if this proxy should supports dynamic redirection
	 */
	public void setSupportsDynamicConfig(boolean supportsDynamicConfig) {
		this.supportsDynamicConfig = supportsDynamicConfig;
	}

	public boolean isFormatAscii() {
		return formatAscii;
	}

	public void setFormatAscii(boolean formatAscii) {
		this.formatAscii = formatAscii;
	}


	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getCaptureFileName() {
		return captureFileName;
	}

	public void setCaptureFileName(String captureFileName) {
		this.captureFileName = captureFileName;
	}

	public JTextArea getText() {
		return text;
	}

	public void setText(JTextArea text) {
		this.text = text;
	}

	/**
	 * Write something into the capture file, if there is one
	 * @param msg
	 */
	public  void log(String msg) {
		msg = msg+"\n";
		if( capture != null ) {
			try {
				capture.write(msg.getBytes());
				capture.flush();	
			} catch(IOException ex) {
				logDebug("Exception in capture ex="+ex);
				ex.printStackTrace();
			}
		}
		if( text != null ) {
			text.append(msg);
		}

		logDebug(msg+"\n");

	}


	@Override
	protected void init() {
		super.init();
		if( supportsDynamicConfig==null) {
			String tmp = getProperty(PROPERTY_DYNAMIC,"false");
			if( tmp != null ) {
				setSupportsDynamicConfig(tmp.toLowerCase().trim().startsWith("t"));
			}
		}
		// don't change if it was set in constructor
		if( remoteHost == null ) {
			String tmp = getProperty(PROPERTY_REMOTE_HOST);
			if( tmp != null ) {
				setRemoteHost(tmp);
			}
		}

		if( remotePort < 0 ) {
			String tmp = getProperty(PROPERTY_REMOTE_PORT);
			if( tmp != null ) {
				setRemotePort(Integer.parseInt(tmp));
			}
		}

		String tmp = getProperty(PROPERTY_TUNNEL);
		if( tmp != null ) {
			if( tmp.toLowerCase().trim().startsWith("t") ) {
				setSocketFactory(new TunnelSocketFactory());
			}
		}


		if( captureFileName == null ) {
			tmp = getProperty(PROPERTY_CAPTURE);
			if( tmp != null ) {
				setCaptureFileName(tmp);
			}
		}

		if( latency<=0) {
			tmp = getProperty(PROPERTY_LATENCY);
			if( tmp != null ) {
				setLatency(Integer.parseInt(tmp));
			}
		}
	}

	public int getConenctionCount() {
		return connectionCount;
	}

	public void incrementCloseCount() {
		closeCount++;
	}

	public int getCloseCount() {
		return closeCount;
	}

	public void run() {
		//  Start the process here

		int localPort = getPort();
		ServerSocket srv= null;
		try {
			srv= getServerSocket();
		} catch (Exception ex) {
			logError("Can't bind to "+localPort+"!",ex);
			return;
		}

		log("Server Bound to port "+localPort+" dynamic="+isSupportsDynamicConfig()+" secure="+isSecure());
		started = running = true;
		Socket inSocket=null;
		Socket remoteSocket=null;
		ProxyConnectionThread con = null;
		String from = "_"+localPort;


		while(!stopping ) 	{
			try {

				inSocket = srv.accept();

				if( inSocket != null ) {
					connectionCount++;
					log(""+(new Date())+" "+localPort+" Connection from "+inSocket);
					configure(inSocket);

					HostConfig config = getHostConfig(inSocket);

					if( config == null ) {
						logError("No HostConfig for incomingSocket localPort="+localPort);
						continue;
					} 

					logDebug("Forwarding to "+config);
					remoteSocket = getRemoteSocket(config);
					configure(inSocket);
					String to = "_"+config.host+"_"+config.port;
					if( captureFileName != null ) {
						con = new ProxyConnectionThread(this,from, to, inSocket,remoteSocket,captureFileName);
					} else {
						con = new ProxyConnectionThread(this,from, to, inSocket,remoteSocket);
					}

					con.start();

				}
			} catch (SocketTimeoutException ex) {
				// ignore
			} catch (Exception ex) {
				logError("Error processing connection.",ex);
				try { inSocket.close(); inSocket = null; } catch(IOException exx) {}
			}
		} // while !done
		if( srv != null ) {
			try {
				srv.close();
				srv = null;
			} catch (IOException e) {
			}
		}
		logDebug("Server unbound from port "+localPort);
		if( capture != null ) {
			try {
				capture.flush();
				capture.close();
			} catch (IOException e) {
			}
			capture = null;
		}

		running = false;		
		log("Server Stopped. running = "+running+" isRunning="+isRunning());

	}// end of run



	private Socket getRemoteSocket(HostConfig config) throws UnknownHostException, IOException {
		Socket ret = getSocketFactory().createSocket(config.host, config.port);
		return ret;
	}

	/**
	 * Determine the appropriate remote end-point for this incoming Socket.
	 * If dynamic redirection is enabled, this method will assume remote end point will be specified 
	 * in the first line of input from the incoming Socket (hostname[:port]\r\n). 
	 * 
	 * 
	 * @param socket
	 * @return A HostConfig representing the remote system designated for this incoming Socket. 
	 * @throws IOException
	 */
	private HostConfig getHostConfig(Socket socket) throws IOException {
		HostConfig ret = null;
		if( isSupportsDynamicConfig() ) {
			//  We don't want to close this because we want the underlying stream to remain open			
			@SuppressWarnings("resource")
			
			InputStream in = socket.getInputStream();
			StringBuilder buf = new StringBuilder();
			int i = in.read();
			while(i>=0 && i != '\n') {
				buf.append((char)i);
				i = in.read();
				if( i < 0 ) {
					throw new EOFException();
				}
			}
			
			
			String line = buf.toString().trim();
			if( !line.isEmpty()) {
				String p[] = line.split(":");
				String host = p[0];
				if( host.length() <=0 ) {
					host = getRemoteHost();
				}
				int port = -1;
				if( p.length > 1) {
					try {
						port = Integer.parseInt(p[1]);
					} catch (Exception e) {
					}
				}
				if( port <= 0 ) {
					port = getRemotePort();
				}
				ret = new HostConfig(host,port);
			}			
		} else {
			ret = new HostConfig(getRemoteHost(),getRemotePort());
		}

		return ret;
	}


	public boolean isRunning() {
		boolean ret = running;
		return ret;
	}

	/**
	 * This server acts as a tcpip proxy.  
	 * It listens for connections on a configured port. 
	 * Once a connection is received, it also connects to some other
	 * configured server:port and transfers all incoming and outgoing
	 * tcpip traffic between those two end points.
	 * 
	 * Creation date: (8/13/2001 7:41:38 PM)
	 */

	@Override
	public void start() {
		init();
		super.start();
	}
}
