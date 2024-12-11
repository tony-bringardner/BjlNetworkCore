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
package us.bringardner.net.tcpproxy;

import java.io.IOException;
import java.net.Socket;

import us.bringardner.core.BaseThread;
import us.bringardner.core.util.AbstractCoreServer;


/**
 * This object monitors a connection between two end points and
 * cleans up when either end point terminates (becomes invalid for any 
 * reason).
 * 
 * Creation date: (8/13/2001 7:24:33 PM)
 * @author: Tony Bringardner
 */
public class ProxyConnectionThread extends BaseThread 
{
	private Socket inSock;
	private Socket outSock;
	private EndPoint in;
	private EndPoint out;
	private boolean running = false;

	private AbstractCoreServer server;

	/**
	 * Forward incoming data packets to a new location.
	 */
	public ProxyConnectionThread(AbstractCoreServer server,String from, String to, Socket i, Socket o)
			throws IOException
	{
		this.server = server;
		inSock = i;
		outSock = o;
		setName("Connection("+from+"-"+to+")");
		in = new EndPoint(inSock,server,	from,
				inSock.getInputStream(),
				outSock.getOutputStream()
				);

		out = new EndPoint(outSock,server, 	to,
				outSock.getInputStream(),
				inSock.getOutputStream()
				);

	}

	/**
	 * ForwardProcReq constructor comment.
	 */
	public ProxyConnectionThread(AbstractCoreServer server,String from, String to, Socket i, Socket o, String captureFile) throws IOException {
		this.server = server;
		inSock = i;
		outSock = o;
		String ext = ".txt";
		if( server != null ) {
			if (server instanceof ProxyServer) {
				ProxyServer ps = (ProxyServer) server;
				ext = ps.isFormatAscii() ? ".txt":".bin";	
			}			
		}

		setName("ProcReq("+from+"-"+to+")");
		in = new EndPoint(server,	from,
				inSock.getInputStream(),
				outSock.getOutputStream(),
				captureFile+"from_"+from+"_to_"+to+ext
				);

		out = new EndPoint(server,	to,
				outSock.getInputStream(),
				inSock.getOutputStream(),
				captureFile+"from_"+to+"_to_"+from+ext
				);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (8/13/2001 7:28:41 PM)
	 */
	public void run() 
	{
		in.start();
		out.start();
		started=running = true;

		server.logDebug("Connection: Waiting for end  points to terminate.");

		while(server.isRunning() &&  in.getStatus() != EndPoint.COMPLETE || out.getStatus() != EndPoint.COMPLETE ){
			try { Thread.sleep(100); } catch(Exception ex) {}
		}

		close();
		running = false;
		server.logDebug("Connection: Both end  points have terminated.");
	}

	public void close() {
		if( running) {
			try { inSock.close(); } catch(Exception e) {}
			try { outSock.close(); } catch(Exception e) {}
			inSock = null;
			outSock = null;
			if( server != null ) {
				server.connectionClosed(this);
			}
			running = false;
		}
	}

	public boolean isRunning() {
		return running;
	}
}
