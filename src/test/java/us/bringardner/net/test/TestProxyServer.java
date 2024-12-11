package us.bringardner.net.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import us.bringardner.core.ILogger.Level;
import us.bringardner.io.CRLFLineReader;
import us.bringardner.io.CRLFLineWriter;
import us.bringardner.io.ILineReader;
import us.bringardner.io.ILineWriter;
import us.bringardner.net.BaseProcReqImpl;
import us.bringardner.net.Server;
import us.bringardner.net.tcpproxy.ProxyServer;

public class TestProxyServer {

	public static class EchoRequestProcessor extends BaseProcReqImpl{

		@Override
		public void run() {
			getLogger().setLevel(Level.NONE);
			started = running=true;
			try {
				String line = readLine();
				while(line !=null ) {
					respond("Echo:"+line);
					if( line.equalsIgnoreCase("exit")) {
						stop();
						break;
					}
					line = readLine();
				}

			} catch (SocketTimeoutException e) {
				// ignore
			} catch (IOException e) {
				logError("Error in echo run", e);
				stop();
			}		
			running = false;
		}

	}


	//  ProxyServer listens on the localPort
	static String localHost = "127.0.0.1";
	static int localPort = 9092;

	// ProxyServer transfers incoming connection to remoteHost:remotePort
	static String remoteHost = "127.0.0.1";		


	static int remotePort = 9093;

	static int serverStartTimeout = 6000;
	static ProxyServer server;
	static Server echoServer;

	@BeforeAll
	public static void setUp() throws Exception {
		server = new ProxyServer(localPort,remotePort,remoteHost);		
		server.getLogger().setLevel(Level.NONE);
		server.setAcceptTimeout(200);
		server.start();
		long start = System.currentTimeMillis();
		while( !server.hasStarted() && System.currentTimeMillis()-start <= serverStartTimeout) {
			Thread.sleep(100);
		}		
		assertTrue(server.isRunning(),"Proxy Server did not start.");

		echoServer = new Server(remotePort, "Echo Server", EchoRequestProcessor.class, false) ;
		echoServer.getLogger().setLevel(Level.NONE);
		echoServer.setAcceptTimeout(200);
		echoServer.start();
		start = System.currentTimeMillis();
		while( !echoServer.isRunning() && System.currentTimeMillis()-start <= serverStartTimeout) {
			Thread.sleep(100);
		}
		assertTrue(echoServer.isRunning(),"Echo Server did not start.");

	}

	@AfterAll
	public static void tearDown() {
		if( echoServer!= null && echoServer.isRunning()) {
			echoServer.stop();
			long start = System.currentTimeMillis();
			while( echoServer.isRunning() && System.currentTimeMillis()-start <= serverStartTimeout) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			assertFalse(echoServer.isRunning(),"Proxy Server did not stop.");
		}

		if( server != null && server.isRunning()) {
			server.stop();
			long start = System.currentTimeMillis();
			while( server.isRunning() && System.currentTimeMillis()-start <= serverStartTimeout) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			boolean running = server.isRunning();
			assertFalse(
					running,
					"Proxy Server did not stop.");
		}
	}

	@Test
	public void testIpProxy() throws IOException {
		String lines [] = {
				"Test Line 01",
				"Test Line 02",
				"Exit",
		};

		server.setFormatAscii(true);
		server.setCaptureFileName("target/ProxyEchoCapture");
		try(Socket socket = new Socket(localHost, localPort)) {
			try(ILineWriter  out = new CRLFLineWriter(socket.getOutputStream())){
				try (ILineReader in = new CRLFLineReader(socket.getInputStream())){
					for (int idx = 0; idx < lines.length; idx++) {
						out.writeLine(lines[idx]);
						String response = in.readLine();
						assertEquals("Echo:"+lines[idx]	,response,"Echo response does not match");
					}
				}
			}
		}
		// check the capture logs

		String expectClientToServer = ""
				+ "\"Test Line 01\\x0d\\x0a\""
				+ "\"Test Line 02\\x0d\\x0a\""
				+ "\"Exit\\x0d\\x0a\""
				+ "";

		String expectServerToClient = "Buffer Size=19\n"
				+ "\t\"Echo:Test Line 01\\x0d\\x0a\"\n"
				+ "\n"
				+ "Buffer Size=19\n"
				+ "\t\"Echo:Test Line 02\\x0d\\x0a\"\n"
				+ "\n"
				+ "Buffer Size=9\n"
				+ "\t\"Echo:Exit\"\n"
				+ "\n"
				+ "Buffer Size=2\n"
				+ "\t\"\\x0d\\x0a\"\n"
				+ "";


		String fileName1 = "target/ProxyEchoCapturefrom__"+remoteHost+"_"+remotePort+"_to__"+localPort+".txt";
		
		
		File file = new File(fileName1).getCanonicalFile();
		assertTrue(file.exists(),fileName1+" does not exists");
		try(InputStream in = new FileInputStream(file)) {
			String actual = new String(in.readAllBytes());
			assertEquals(normalize(expectServerToClient), normalize(actual),"Client to server capture is not what we expected");
		}
		assertTrue(file.delete(),"Could not delete "+file);

		String fileName2 = "target/ProxyEchoCapturefrom__"+localPort+"_to__"+remoteHost+"_"+remotePort+".txt";

		file = new File(fileName2).getCanonicalFile();
		assertTrue(file.exists(),fileName2+" does not exists");
		try(InputStream in = new FileInputStream(file)) {
			String actual = new String(in.readAllBytes());
			assertEquals(normalize(expectClientToServer), normalize(actual),"Server to client capture is not what we expected");
		}
		
		assertTrue(file.delete(),"Could not delete "+file);
	}

	
	private Object normalize(String actual) {
		actual = actual.trim().replace("\"", "");
		StringBuilder ret = new StringBuilder();
		for(String str : actual.split("\n")) {
			str = str.trim();
			if( !str.isEmpty()) {
				if( !str.startsWith("Buffer Size")) {
					ret.append(str);
				}
			}
		}
		
		return ret.toString();
	}	

}
