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
package us.bringardner.net.tcpproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;

public class TunnelSocketFactory extends SocketFactory {
	private String proxyHost;
	private int proxyPort;
	
	public TunnelSocketFactory() {
		super();
	}
	
	
	/**
	 * Create a TunnelSocketFactory using the specified HTTP proxy host and port.
	 *  
	 * @param proxyHost
	 * @param proxyPort
	 */
	public TunnelSocketFactory(String proxyHost, int proxyPort) {
		super();
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}
	
	

	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	@Override
	public Socket createSocket(String host, int port) throws IOException,UnknownHostException {
		
		Tunnel t = new Tunnel("bringardner.com",443);
		
		try {
			if(!t.connect(host,port) ) {
				throw new IOException("Can't connect to "+host+":"+port);
			}
		} catch (UnrecoverableKeyException e) {
			throw new IOException(e);
		} catch (KeyManagementException e) {
			throw new IOException(e);
		} catch (CertificateException e) {
			throw new IOException(e);
		} catch (KeyStoreException e) {
			throw new IOException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		
		return t.getSocket();
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return createSocket(arg0.getHostName(), arg1);
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
		throw new IOException("Not implemented");
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,int arg3) throws IOException {
		throw new IOException("Not implemented");
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/*
		SocketFactory fact = new TunnelSocketFactory();
		CommandClient client = new CommandClient("www.cooperonline.com",8025);
		client.setSocketFactory(fact);
		if( client.connect() ) {
			System.out.println("Can;t connect");
			System.exit(0);
		}
		String greeting = client.readLine();
		System.out.println("Gretting="+greeting);
		client.executeCommand("tag CAPABILITY");
		String tmp = client.readLine();
		System.out.println("tmp="+tmp);
		tmp = client.readLine();
		System.out.println("tmp="+tmp);
		client.close();
	*/
	}

}
