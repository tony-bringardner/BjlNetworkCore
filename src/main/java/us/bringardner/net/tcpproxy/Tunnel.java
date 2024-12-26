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
 * ~version~V000.01.02-V000.00.01-V000.00.00-
 */
package us.bringardner.net.tcpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import us.bringardner.core.ILogger;
import us.bringardner.core.SecureBaseObject;
import us.bringardner.io.CRLFLineReader;
import us.bringardner.io.CRLFLineWriter;

public class Tunnel extends SecureBaseObject {

	private String proxyHost;
	private int proxyPort=-1;
	private String host;
	private int port=-1;
	private Socket socket;
	private InputStream in;
	private OutputStream out;




	public static String downloadString(URL url,ILogger logger) {


		String ret = null;
		URLConnection con = null;

		try {
			con = url.openConnection();
			con.setConnectTimeout(6000);
			con.setReadTimeout(6000);		

			con.connect();
			InputStream in = con.getInputStream();


			if (con instanceof HttpURLConnection) {
				HttpURLConnection http = (HttpURLConnection) con;
				int code = http.getResponseCode();
				logger.debug("\tDownload response code = "+code);
				if( code == 200 ) {
					int len = con.getContentLength();
					logger.debug("Downloading content len= "+len);
					if( len < 0 ) {
						len = 1024*1024*5;
					}

					byte [] data = new byte[len];
					int got = 0;
					while( got < len) {
						int gotNow = in.read(data, got, (len-got));							
						if( gotNow < 0 ) {
							break;
						} else {
							got += gotNow;
						}
					}
					ret = new String(data,0,got);
				} else {
					logger.debug("Response code "+code+" indicated a problem. Use local if possible.");
				}
			} else {
				// error
			}


		} catch (Throwable e) {
		} 




		return ret;
	}


	public Tunnel() {

	}

	public Tunnel(String host, int port) {
		setHost(host);
		setPort(port);
	}

	public Tunnel(String proxyHost, int proxyPort, String host, int port) {
		setProxyHost(proxyHost);
		setProxyPort(proxyPort);
		setHost(host);
		setPort(port);		
	}

	public void writeLine(String line) throws IOException {
		logDebug("write:'"+line+"'");
	}

	public String readLine(InputStream in) throws IOException {

		StringBuffer bf = new StringBuffer();
		int i = 0;
		int lst = (int)'\0';
		boolean done = false;
		int cnt = 0;

		while (!done && (i = in.read()) != -1) {
			if (i == '\n') {
				done = true;
			} else {
				cnt++;
				bf.append((char)i);
				lst = i;
			}
		}

		String ret = null;
		if (cnt > 0) {
			if( lst == '\r' ){
				cnt --;
			}
			ret = bf.substring(0,cnt);
		}

		return ret;
	}

	

	public boolean connect(String host, int port) throws UnrecoverableKeyException, KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {
		boolean ret = connect();
		try {
			if( ret ) {
				System.out.println("Connected!!!.");
				OutputStream out = getOutputStream();
				InputStream in = getInputStream();


				out.write(("GET /Proxy?userid=tony@bringardner.com&passwd=0000&host="+host+"&port="+port+" HTTP/1.0\r\n").getBytes());
				out.write(("Host: bringardner.com\r\n").getBytes());				
				out.write("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; MS-RTC LM 8)\r\n".getBytes());
				out.write("Pragma: no-cache\r\n".getBytes());
				out.write("\r\n".getBytes());
				out.flush();


				// read the http stuff
				String line = readLine(in);
				String [] parts = line.split(" ");
				int resCode = Integer.parseInt(parts[1]);
				System.out.println("resCode="+resCode);
				int sz = -1;
				while(line.length() > 0 ) {
					line = readLine(in);
					if( line.startsWith("Content-Length")) {
						parts = line.split(":");
						sz = Integer.parseInt(parts[1].trim());
					}
				}

				if( sz > 0 ) {
					//  eat the response data
					byte [] data = new byte[sz];
					int got = in.read(data);
					int tot = got;
					while(got > 0 && tot < sz) {
						if( (got = in.read(data)) > 0 ){
							tot+=got;
						}
					}
					String tmp = new String(data);
					System.out.println("response data = "+tmp);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			ret = false;
		}



		return ret;
	}


	public boolean connect() throws IOException, UnrecoverableKeyException, KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
		boolean ret = false;

		String host = getHost()+":"+getPort();

		OutputStream out = getOutputStream();
		InputStream in = getInputStream();
		logDebug("Clear Connection to "+host);

		out.write(("CONNECT "+host+" HTTP/1.0\r\n").getBytes());
		out.write("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; MS-RTC LM 8)\r\n".getBytes());
		out.write(("Host: "+host+"\r\n").getBytes());
		out.write("Content-Length: 0\r\n".getBytes());
		out.write("Proxy-Connection: Keep-Alive\r\n".getBytes());
		out.write("Pragma: no-cache\r\n".getBytes());
		out.write("X-NovINet: v1.2\r\n".getBytes());
		out.write("\r\n".getBytes());

		byte [] data = new byte[1024];
		int cnt = in.read(data);
		logDebug("init read cnt="+cnt);
		if( cnt > 0 ) {
			String res = new String(data,0,cnt);
			logDebug("init read res="+res);
			String [] parts = res.split(" ");
			if( parts.length > 1) {
				int resCode = Integer.parseInt(parts[1]);
				logDebug("init read resCode="+resCode);
				if( resCode == 200 ) {
					logDebug("Attempting SSL connect");

					SSLSocketFactory factory = getSSLContext().getSocketFactory();

					this.socket = factory.createSocket(socket, getHost(), getPort(), true);
					this.in = this.socket.getInputStream();
					this.out = this.socket.getOutputStream();

					ret = true;
					logDebug("Converted to SSL");
				}
			}
		}

		return ret;
	}

	public void close() {
		if( out != null ) {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
		if( in != null ) {
			try {
				in.close();
			} catch (IOException e) {
			}
		}

		if( socket != null ) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	public InputStream getInputStream() throws  IOException {
		if( in == null ) {
			in = getSocket().getInputStream();
		}
		return in;
	}

	public OutputStream getOutputStream() throws  IOException {
		if( out == null ) {
			out = getSocket().getOutputStream();
		}

		return out;
	}

	public Socket getSocket() throws IOException {
		if( socket == null ) {
			socket = new Socket(getProxyHost(),getProxyPort());
		}

		return socket;
	}

	public String getProxyHost() {
		return proxyHost;
	}


	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}


	public int getProxyPort() {
		if(proxyPort < 0 ) {
			synchronized (this) {
				if(proxyPort < 0 ) {
					getProxyHost();
				}
			}
		}

		return proxyPort;
	}


	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	@Override
	public TrustManager[] getTrustManagers() {
		TrustManager mgr = new  X509TrustManager () {

			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
				/*
				System.out.println("checkServerTrusted");
				System.out.println("sz="+arg0.length);
				for (X509Certificate cert : arg0) {
					System.out.println("x="+cert.getSubjectDN());

				}
				*/
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};

		return new TrustManager[] {mgr};
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws Exception {
	
	
		String host = "www.cooperonline.com";
	
		TunnelSocketFactory factory = new TunnelSocketFactory();
		
		Socket t = factory.createSocket(host,9000);
	
	
		CRLFLineReader crIn = null;
		CRLFLineWriter crOut = null;
		
		try {
				System.out.println("Connected!!!.");
				crIn = new CRLFLineReader(t.getInputStream());
				crOut = new CRLFLineWriter(t.getOutputStream());
	
				crOut.writeLine("admin list");
				crOut.flush();
	
				String line = crIn.readLine();
				if( !line.startsWith("300")) {
					throw new IOException("Bad response to admin list line="+line);
				}
	
				while( (line=crIn.readLine()) != null && !line.startsWith("200 ")) {
					System.out.println(line);
				}
				if( line == null ) {
					System.out.println("Premature end. Line=null");
				} else {
					if( line.startsWith("200 ")) {
						System.out.println("End was good");
					}
				}
	
				crOut.writeLine("quit");
				
		} catch( Exception e) {
			System.out.println("e="+e);
			e.printStackTrace();
		} finally {
			if( crIn != null ) { crIn.close();}
			if( crOut != null ) { crOut.close();}
			t.close();
		}
	
	}

	/**
	 * @param args
	 * @throws Exception 
	 */

	@SuppressWarnings("resource")
	public static void mainz(String[] args) throws Exception {


		String host = "www.cooperonline.com";

		Tunnel t = new Tunnel("bringardner.com",443);

		boolean ok = t.connect(host,9000);
		
		try {
			if( !ok ) {
				System.out.println("Can't connect.");
			} else {
				System.out.println("Connected!!!.");
				CRLFLineReader crIn = new CRLFLineReader(t.getInputStream());
				CRLFLineWriter crOut = new CRLFLineWriter(t.getOutputStream());

				crOut.writeLine("admin list");
				crOut.flush();

				String line = crIn.readLine();
				if( !line.startsWith("300")) {
					throw new IOException("Bad response to admin list line="+line);
				}

				while( (line=crIn.readLine()) != null && !line.startsWith("200 ")) {
					System.out.println(line);
				}
				if( line == null ) {
					System.out.println("Premature end. Line=null");
				} else {
					if( line.startsWith("200 ")) {
						System.out.println("End was good");
					}
				}

				crOut.writeLine("quit");
			}
		} catch( Exception e) {
			System.out.println("e="+e);
			e.printStackTrace();
		} finally {
			t.close();
		}

	}

	/**
	 * @param args
	 * @throws Exception 
	 */

	public static void mainx(String[] args) throws Exception {


		String host = "www.cooperonline.com";
		//host = "tony.oh.bringardner.com";
		//host = "174.129.3.81";

		TunnelSocketFactory factory = new TunnelSocketFactory();

		Socket socket = factory.createSocket(host,443);
		OutputStream out = null;
		InputStream in = null;
		CRLFLineReader crIn = null;
		CRLFLineWriter crOut = null;
		
		try {

			System.out.println("Connected!!!.");
			out = socket.getOutputStream();
			in = socket.getInputStream();

			out.write(("GET /Proxy?useridd=tony@bringardner.com&passwd=0000&host=bringardner.com&port=9000 HTTP/1.0\r\n").getBytes());
			out.write("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; MS-RTC LM 8)\r\n".getBytes());
			out.write(("Host: "+host+"\r\n").getBytes());
			out.write("Pragma: no-cache\r\n".getBytes());
			out.write("\r\n".getBytes());
			out.flush();

			while(in.available()>0) {
				byte data [] = new byte[in.available()];
				in.read(data);
				String tmp = new String(data);
				System.out.println(tmp);
			}

			crIn = new CRLFLineReader(in);
			crOut = new CRLFLineWriter(out);
			//crOut.setAutoFlush(true);

			//crOut.writeLine("174.129.3.81:9100");

			//crOut.writeLine("admin list");

			String line = crIn.readLine();
			if( !line.startsWith("300")) {
				throw new IOException("Bad response to admin list line="+line);
			}

			while( (line=crIn.readLine()) != null && !line.startsWith("200 ")) {
				System.out.println(line);
			}

			if( line == null ) {
				System.out.println("Premature end. Line=null");
			} else {
				if( line.startsWith("200 ")) {
					System.out.println("End was good");
				}
			}

			crOut.writeLine("quit");
		} catch( Exception e) {
			System.out.println("e="+e);
			e.printStackTrace();
		} finally {
			if( crIn != null ) {try {crIn.close();} catch (Exception e2) {}}
			if( crOut != null ) {try {crOut.close();} catch (Exception e2) {}}
			if( in != null ) {try {in.close();} catch (Exception e2) {}}
			if( out != null ) {try {out.close();} catch (Exception e2) {}}
			socket.close();
		}

	}

	/**
	 * @param args
	 * @throws Exception 
	 */

	public static void mainy(String[] args) throws Exception {

		TrustManager mgr = new  X509TrustManager () {

			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
				//  Trust everything (Testing only)
			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
				// Trust everything (Testing only)
			}

			public X509Certificate[] getAcceptedIssuers() {
				//  Trust everything (Testing only)				
				return null;
			}

		};

		String host = "www.cooperonline.com";
		//host = "tony.oh.bringardner.com";
		//host = "174.129.3.81";

		Tunnel tunnel = new Tunnel(host,443);
		tunnel.setTrustManagers(new TrustManager[] {mgr});

		boolean ok = tunnel.connect();
		CRLFLineReader crIn = null;
		CRLFLineWriter crOut = null;
		OutputStream out = null;
		InputStream in = null;
		
		try {
			if( !ok ) {
				System.out.println("Can't connect.");
			} else {
				System.out.println("Connected!!!.");
				out = tunnel.getOutputStream();
				in = tunnel.getInputStream();


				//out.write("GET /Proxy?userid=tony@bringardner.com&passwd=000&host=174.129.3.81&port=9100 HTTP/1.1\n\r".getBytes());
				out.write("GET /Proxy?userid=tony@bringardner.com&passwd=0000&host=bringardner.com&port=9000 HTTP/1.0\r\n".getBytes());
				//out.write("GET / HTTP/1.1\r\n".getBytes());
				out.write(("Host: "+host+"\r\n").getBytes());				
				out.write("User-Agent: Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6; .NET CLR 2.0.50727; .NET CLR 1.1.4322; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; MS-RTC LM 8)\r\n".getBytes());
				//out.write("Connection: Keep-Alive\r\n".getBytes());
				out.write("Pragma: no-cache\r\n".getBytes());
				out.write("\r\n".getBytes());
				//out.write("admin list\r\n".getBytes());
				out.flush();

				byte [] data = new byte[1024];
				int cnt = in.read(data);
				System.out.println("read cnt="+cnt);
				if( cnt > 0 ) {
					String res = new String(data,0,cnt);
					System.out.println("init read res="+res);
					String [] parts = res.split(" ");
					if( parts.length > 1) {
						int resCode = Integer.parseInt(parts[1]);
						System.out.println("init read resCode="+resCode);
						if( resCode == 200 ) {
							System.out.println("Good");
						} else {
							System.out.println("bad");

							cnt = in.read(data);
							System.out.println("Second read cnt="+cnt);
							while( cnt > 0 ) {
								res = new String(data,0,cnt);
								System.out.println("... res="+res);
								cnt = in.read(data);
							}
							throw new IOException("Bad response code res="+res);
						}

					}
				}



				crIn = new CRLFLineReader(in);
				crOut = new CRLFLineWriter(out);

				String line = crIn.readLine();
				//crOut.writeLine("174.129.3.81:9100");
				System.out.println("Connect line = "+line);

				crOut.writeLine("admin list");
				crOut.flush();

				line = crIn.readLine();
				if( !line.startsWith("300")) {
					throw new IOException("Bad response to admin list line="+line);
				}

				while( (line=crIn.readLine()) != null && !line.startsWith("200 ")) {
					System.out.println(line);
				}
				if( line == null ) {
					System.out.println("PRemature end. Line=null");
				} else {
					if( line.startsWith("200 ")) {
						System.out.println("End was good");
					}
				}

				crOut.writeLine("quit");
			}
		} catch( Exception e) {
			System.out.println("e="+e);
			e.printStackTrace();
		} finally {
			if( crIn != null ) {try {crIn.close();} catch (Exception e2) {}}
			if( crOut != null ) {try {crOut.close();} catch (Exception e2) {}}
			if( in != null ) {try {in.close();} catch (Exception e2) {}}
			if( out != null ) {try {out.close();} catch (Exception e2) {}}
			tunnel.close();
		}

	}

}
