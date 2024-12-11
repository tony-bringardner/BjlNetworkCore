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
 * ~version~V000.01.02-V000.01.01-V000.01.00-V000.00.01-V000.00.00-
 */
package us.bringardner.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ServerSocketFactory;

import us.bringardner.core.util.AbstractCoreServer;
/**
 * This class is intended to provide an easy to use framework for developing Internet Protocols.
 * The server will handle all of the details of listing for and managing incoming sockets (mostly done by AbstractCoreServer).
 * All the extending class need to do is provide a RequestProsessor that will provide the 
 * protocol functionality.
 * 
 */
public class Server extends AbstractCoreServer  {

	private Class<?> procClass;
	
	//  Default buffer size - 1MB
	private int bufferSize = 1024*1024;


	public Server() {
		super();
	}

	public Server(int port, String name, Class<?> procClass, boolean secure)	{
		super(port,secure);
		setName(name);
		setProcClass(procClass);		
	}

	public void stop() {
		running = false;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}


	/**
	 * 
	 * Creation date: (6/28/2004 11:13:28 AM)
	 * @return java.lang.Class
	 */
	public Class<?> getProcClass() {
		return procClass;
	}




	public void run() 	{
		//  Start the process here

		ServerSocket srv=null;
		int port = getPort();
		boolean secure = isSecure();

		try {
			ServerSocketFactory factory = getServerSocketFactory();
			if( factory == null ) {
				logError("ServerFactory not supported secure="+secure);
				return;
			}
			if( (srv = factory.createServerSocket(port)) == null) {
				logError("Can't createServerSocket port="+port);
				return;
			}
			srv.setSoTimeout(getAcceptTimeout());
		} catch (Exception ex) {
			logError("Can't bind to "+port+"!",ex);
			return;
		}



		Socket socket=null;
		String myName = getName();
		logInfo("Started "+myName+" on port "+port+" secure="+secure);

		started = running = true;	

		while(!stopping) 	{
			socket = null;
			try { 
				socket = srv.accept();
			} catch (SocketTimeoutException ex) {
				// Timed out
			} catch (Exception ex) {
				logError("Exception on accept",ex);
			}

			if( socket != null ) {
				try {
					logDebug("Connection from "+socket);
					IRequestProcessor p = getRequestProcessor();
					p.start(socket,this);
				} catch (Exception ex) {
					logError("Exception from procReq! "+getName(),ex);
					try { socket.close(); socket = null; } catch(IOException exx) {}
				}
			}
		} 
	}


	/**
	 * Override this method to implement a pool of workers.
	 * 
	 * @return an IRequestProcessor available for processing 
	 * @throws IOException 
	 */
	public IRequestProcessor getRequestProcessor()  throws IOException {
		try {
			return (IRequestProcessor)procClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException| NoSuchMethodException | SecurityException e) {
			throw new IOException(e);
		}
	}

	/**
	 * 
	 * Creation date: (6/28/2004 11:13:28 AM)
	 * @param newProcClass java.lang.Class
	 */
	public void setProcClass(Class<?> newProcClass) {
		procClass = newProcClass;
	}


}
