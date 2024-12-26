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
 * ~version~V000.01.01-V000.00.01-V000.00.00-
 */
package us.bringardner.net;

import java.io.IOException;
import java.net.Socket;


/**
 *  Creation date: (6/28/2004 12:36:02 PM)
 * This interface provides a communication vehicle for processors and server to communicate with each other
 * while the processor is running.
 * 
 * @author: Tony Bringardner
 */
public interface IRequestProcessor extends Runnable {
	/**
	 * Set the config info then start the process Creation date: (6/28/2004 12:37:20 PM)
	 */
	public void start(Socket sock, Server server)	throws IOException;

	public Server getServer();


	/**
	 * 
	 * Creation date: (6/28/2004 12:45:53 PM)
	 * 
	 * @return java.lang.StringBuffer the dialog with the client
	 */
	public java.lang.StringBuffer getDialog();


	/**
	 * 
	 * Creation date: (6/28/2004 12:45:53 PM)
	 * 
	 * @return boolean true is the processor is currently running
	 */
	public boolean isRunning();

	/**
	 * @return the Communication Socket
	 */
	public Socket getSocket();

	/**
	 * @return the system time when this process was started
	 */
	public long getStartTime();

	
	/**
	 * Send a response to the client.  The response is not processed in any way.
	 * 
	 * @param line
	 * @throws IOException
	 */
	public void respond(String line) throws IOException ;
}