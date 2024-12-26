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
 * ~version~V000.01.02-V000.01.01-V000.00.01-V000.00.00-
 */
package us.bringardner.net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import us.bringardner.core.BaseThread;
import us.bringardner.io.CRLFLineReader;
import us.bringardner.io.CRLFLineWriter;
import us.bringardner.io.ILineReader;
import us.bringardner.io.ILineWriter;
import us.bringardner.io.LFLineReader;
import us.bringardner.io.LFLineWriter;
import us.bringardner.io.TelnetInputStream;
import us.bringardner.io.TelnetOutputStream;

/*
 * Basic class for building protocol request processors.
 */
public abstract class BaseProcReqImpl extends BaseThread implements		IRequestProcessor {

	private Socket socket = null;
	
	//  Allows the channel to be switched from clear to encrypted.
	private SSLSocket sslSocket;

	private Server server;

	private StringBuffer dialog = new StringBuffer();

	private boolean done = false;

	private long startTime = System.currentTimeMillis();

	private boolean debug = true;

	private ILineWriter out;

	private ILineReader in;

	//  Most Internet protocols use lines terminated by CRLF
	private boolean useCRLF = true;

	public BaseProcReqImpl() {
		
	}

	public BaseProcReqImpl(Socket s, boolean secure) {
		this();
		socket = s;
		setSecure(secure);
	}

	/**
	 * 
	 * Creation date: (6/28/2004 12:45:53 PM)
	 * 
	 * @return java.lang.StringBuffer the dialog with the client
	 */
	public java.lang.StringBuffer getDialog() {
		return dialog;
	}

	/**
	 * Get the input reader Creation date: (6/28/2004 12:50:40 PM)
	 * 
	 * @return us.bringardner.io.LineReader
	 */
	public us.bringardner.io.ILineReader getIn() {
		return in;
	}

	/**
	 * Get the Writer Creation date: (6/28/2004 12:50:40 PM)
	 * 
	 * @return us.bringardner.io.LineWriter
	 */
	public us.bringardner.io.ILineWriter getOut() {
		return out;
	}

	/**
	 * 
	 * Creation date: (6/28/2004 12:45:53 PM)
	 * 
	 * @return booelan true is the processor is currently running
	 */
	public boolean getRunning() {
		return running;
	}

	/**
	 * @return the Communication Socket 
	 */
	public Socket getSocket() {
		Socket ret = socket;
		
		if( sslSocket != null ){
			ret = sslSocket;
		}
		
		return ret;
	}

	/**
	 * @return the system time when this process was started
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:45:53
	 * PM)
	 * 
	 * @return boolean
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:45:53
	 * PM)
	 * 
	 * @return boolean
	 */
	public boolean isDone() {
		return done;
	}

	
	public String readLine() throws IOException {

		String ret = in.readLine();
		dialog.append("Read:" + ret + "\n");
		if (isDebug()) {
			logDebug("Read:" + ret + "\n");
		}

		return ret;
	}

	public void respond(String line) throws IOException {

		dialog.append("Write:" + line + "\n");

		out.writeLine(line);
		out.flush();
		if (isDebug()) {
			logDebug("Write:" + line + "\n");
		}

	}

	public abstract void run();

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:45:53 PM)
	 * 
	 * @param newDebug
	 *            boolean
	 */
	public void setDebug(boolean newDebug) {
		debug = newDebug;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:45:53
	 * PM)
	 * 
	 * @param newDialog
	 *            java.lang.StringBuffer
	 */
	public void setDialog(java.lang.StringBuffer newDialog) {
		dialog = newDialog;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:45:53
	 * PM)
	 * 
	 * @param newDone
	 *            boolean
	 */
	public void setDone(boolean newDone) {
		done = newDone;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:50:40
	 * PM)
	 * 
	 * @param newIn
	 *            us.bringardner.io.LineReader
	 */
	public void setIn(us.bringardner.io.ILineReader newIn) {
		in = newIn;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:50:40
	 * PM)
	 * 
	 * @param newOut
	 *            us.bringardner.io.LineWriter
	 */
	public void setOut(us.bringardner.io.ILineWriter newOut) {
		out = newOut;
	}

	/**
	 * Insert the method's description here. Creation date: (6/28/2004 12:45:53
	 * PM)
	 * 
	 * @param newRunning
	 *            booelan
	 */
	public void setRunning(boolean newRunning) {
		running = newRunning;
	}

	private void configureStreams() throws IOException {
		Socket socket = getSocket();
		
		if (useCRLF) {
			in = new CRLFLineReader(socket.getInputStream());
			out = new CRLFLineWriter(socket.getOutputStream());
		} else {
			in = new LFLineReader(socket.getInputStream());
			out = new LFLineWriter(socket.getOutputStream());
		}

	}
	public void setSocket(Socket s) throws IOException {
		socket = s;
		configureStreams();
	}
	
	public void echoOff() throws IOException {
		configureStreams();
	}
	
	public void echoOn() throws IOException {
		Socket socket = getSocket();
		
		TelnetOutputStream to = new TelnetOutputStream(socket.getOutputStream());
		TelnetInputStream  ti = new TelnetInputStream(socket.getInputStream(),to);
		in = new LFLineReader(ti);
		out = new LFLineWriter(to);
		
		
	}

	/**
	 * 
	 * @param newStartTime long
	 */
	public void setStartTime(long newStartTime) {
		startTime = newStartTime;
	}

	/*
	 * Set the config info then start the process Creation date: (6/28/2004
	 * 12:37:20 PM)
	 */
	public void start(Socket sock, Server server) throws IOException {
		if( !isRunning() ) {
			setSocket(sock);
			setServer(server);		
			start();
		}
	}

	/*
	 * Set the config info then start the process Creation date: (6/28/2004
	 * 12:37:20 PM)
	 */
	public void stop() {
		running = false;

	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public boolean isUseCRLF() {
		return useCRLF;
	}

	public void setUseCRLF(boolean useCRLF) {
		this.useCRLF = useCRLF;
	}
	
	public void negotiateSecureSocket(String sslOrTsl) throws KeyManagementException, CertificateException, FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, IOException {
		if( sslSocket != null ) {
			sslSocket.close();
			sslSocket = null;
		}

		if( sslOrTsl == null ) {
			return;
		}
		
		if( isSecure()) {
			throw new IllegalStateException("Can not negotiate a secure channel from a secure channel.");
		}
		setAlgorithm(sslOrTsl);
		SSLContext ctx=getSSLContext();
		SSLSocketFactory factory = ctx.getSocketFactory();
		
		sslSocket = (SSLSocket)factory.createSocket(getSocket(),socket.getInetAddress().getHostAddress(), socket.getPort(), false);
		//sslSocket.startHandshake();
		sslSocket.setUseClientMode(false);
		configureStreams();
		
	}
	
}