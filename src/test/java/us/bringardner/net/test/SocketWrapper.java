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
 * ~version~V000.00.01-V000.00.00-
 */
/**
 * This is just a wrapper around a socket to help in debugging issues;
 */
package us.bringardner.net.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * This is just a wrapper around a standard socket to help in debugging. 
 * Only for use in test environment!!!
 */
public class SocketWrapper extends Socket {
	private Socket target;	
	
	public SocketWrapper(Socket target) {
		this.target = target;
	}
	
	public void setSoTimeout(int arg0) throws SocketException{
		target.setSoTimeout(arg0);
	}
	public InetAddress getInetAddress(){
		return target.getInetAddress();
	}
	public boolean isClosed(){
		return target.isClosed();
	}
	public String toString(){
		return target.toString();
	}
	public void connect(SocketAddress arg0) throws IOException{
		target.connect(arg0);
	}
	public void connect(SocketAddress arg0,int arg1) throws IOException{
		target.connect(arg0,arg1);
	}
	public void close() throws IOException{
		target.close();
	}
	public  InputStream getInputStream() throws IOException{
		return target.getInputStream();
	}
	public int getPort(){
		return target.getPort();
	}
	public  SocketChannel getChannel(){
		return target.getChannel();
	}
	public  OutputStream getOutputStream() throws IOException{
		return target.getOutputStream();
	}
	public void bind(SocketAddress arg0) throws IOException{
		target.bind(arg0);
	}
	public InetAddress getLocalAddress(){
		return target.getLocalAddress();
	}
	public int getLocalPort(){
		return target.getLocalPort();
	}
	public SocketAddress getRemoteSocketAddress(){
		return target.getRemoteSocketAddress();
	}
	public SocketAddress getLocalSocketAddress(){
		return target.getLocalSocketAddress();
	}
	public void setTcpNoDelay(boolean arg0) throws SocketException{
		target.setTcpNoDelay(arg0);
	}
	public boolean getTcpNoDelay() throws SocketException{
		return target.getTcpNoDelay();
	}
	public void setSoLinger(boolean arg0,int arg1) throws SocketException{
		target.setSoLinger(arg0,arg1);
	}
	public int getSoLinger() throws SocketException{
		return target.getSoLinger();
	}
	public void sendUrgentData(int arg0) throws IOException{
		target.sendUrgentData(arg0);
	}
	public void setOOBInline(boolean arg0) throws SocketException{
		target.setOOBInline(arg0);
	}
	public boolean getOOBInline() throws SocketException{
		return target.getOOBInline();
	}
	public int getSoTimeout() throws SocketException{
		return target.getSoTimeout();
	}
	public void setSendBufferSize(int arg0) throws SocketException{
		target.setSendBufferSize(arg0);
	}
	public int getSendBufferSize() throws SocketException{
		return target.getSendBufferSize();
	}
	public void setReceiveBufferSize(int arg0) throws SocketException{
		target.setReceiveBufferSize(arg0);
	}
	public int getReceiveBufferSize() throws SocketException{
		return target.getReceiveBufferSize();
	}
	public void setKeepAlive(boolean arg0) throws SocketException{
		target.setKeepAlive(arg0);
	}
	public boolean getKeepAlive() throws SocketException{
		return target.getKeepAlive();
	}
	public void setTrafficClass(int arg0) throws SocketException{
		target.setTrafficClass(arg0);
	}
	public int getTrafficClass() throws SocketException{
		return target.getTrafficClass();
	}
	public void setReuseAddress(boolean arg0) throws SocketException{
		target.setReuseAddress(arg0);
	}
	public boolean getReuseAddress() throws SocketException{
		return target.getReuseAddress();
	}
	public void shutdownInput() throws IOException{
		target.shutdownInput();
	}
	public void shutdownOutput() throws IOException{
		target.shutdownOutput();
	}
	public boolean isConnected(){
		return target.isConnected();
	}
	public boolean isBound(){
		return target.isBound();
	}
	public boolean isInputShutdown(){
		return target.isInputShutdown();
	}
	public boolean isOutputShutdown(){
		return target.isOutputShutdown();
	}
	
	public void setPerformancePreferences(int arg0,int arg1,int arg2){
		target.setPerformancePreferences(arg0,arg1,arg2);
	}


	
}
