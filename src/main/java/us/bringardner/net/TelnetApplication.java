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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.bringardner.io.CRLFLineReader;
import us.bringardner.io.CRLFLineWriter;
import us.bringardner.io.ILineReader;
import us.bringardner.io.ILineWriter;
import us.bringardner.io.LFLineReader;
import us.bringardner.io.LFLineWriter;

public class TelnetApplication {

	private static final String CONNECT = "Connect";
	private static final String DISCONNECT = "Close Connection";

	private static class WellKnownPort {
		public WellKnownPort(String line) {
			String parts [] = line.split("[~]");
			port = Integer.parseInt(parts[1]);
			name = parts[0];
			description = parts[2];
		}

		int port;
		String name;
		@SuppressWarnings("unused")
		String description;
	}

	private static Map<String,WellKnownPort> wellKnownPorts = new TreeMap<String, TelnetApplication.WellKnownPort>();
	private static String wellKNownPortames[];

	static {
		String lines[] = ("ftp~20~"
				+ "File Transfer Protocol (FTP) Command Control\n"
				+ "ssh~22~"
				+ "Secure Shell (SSH)\n"
				+ "telnet~20~"
				+ "Telnet - Remote login service, unencrypted text messages\n"
				+ "smtp~25~"
				+ "Simple Mail Transfer Protocol (SMTP) E-mail Routing\n"
				+ "dns~53~"
				+ "Domain Name System (DNS) service\n"
				+ "http~80~"
				+ "Hypertext Transfer Protocol (HTTP) used in World Wide Web\n"
				+ "pop3~110~"
				+ "Post Office Protocol (POP3) used by e-mail clients to retrieve e-mail from a server\n"
				+ "nntp~119~"
				+ "Network News Transfer Protocol (NNTP)\n"
				+ "ntp~123~"
				+ "Network Time Protocol (NTP)\n"
				+ "imap~143~"
				+ "Internet Message Access Protocol (IMAP) Management of Digital Mail\n"
				+ "snmp~161~"
				+ "Simple Network Management Protocol (SNMP)\n"
				+ "irc~194~"
				+ "Internet Relay Chat (IRC)\n"
				+ "https~44~"
				+ "HTTP Secure (HTTPS) HTTP over TLS/SSL\n"
				+ "JDns Admin~9999~dmin port"
				+ "smtp~25~"
				+ "Simple Mail Transfer Protocol (SMTP) E-mail Routing"
				).split("\n");

		List<String> names = new ArrayList<>();
		for(String line : lines) {
			us.bringardner.net.TelnetApplication.WellKnownPort p = new WellKnownPort(line);
			wellKnownPorts.put(p.name, p);
			names.add(p.name);
		}
		Collections.sort(names);
		wellKNownPortames = names.toArray(new String[names.size()]);

	}

	
	class ResponseReader implements Runnable  {
		boolean shouldStop=false;
		boolean running = false;
		boolean pause;
		boolean isPaused=false;
		String lastLine="";

		public void run() {
			running = true;
			int errCnt = 0;
			while(!shouldStop) {
				if( pause ) {
					isPaused = true;
					while(pause) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
						}
					}
					isPaused = false;
				}


				try {
					//boolean ok = socket.getInetAddress().isReachable(100);
					//System.out.println("ok = "+ok);
					String  line = in.readLine();
					long time = System.currentTimeMillis()-lastCommandStart;
					if( line == null ) {
						shouldStop = true;
					} else { 
						lastLine = line;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								responseTextArea.append(line+"\n");	
								statusLabel.setText("Time in ms="+time);
							}
						});
					}
				} catch(java.net.SocketTimeoutException e) {
					//  ignore these
				} catch (IOException e) {
					if( !shouldStop) {
						//e.printStackTrace();
						if( ++errCnt > 4) {
							errCnt = 0;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						}
					}
				}
			}
			
			actionDisConnect();
			System.out.println("Exit response reader running="+running);
			running = false;
			
		}

		public void stop() {
			shouldStop = true;
		}

		public void pause() {
			pause = true;

		}

		public void restart() {
			pause = false;
		}
	}

	private ResponseReader responseReader;
	private JFrame frame;
	private JTextField hostNameTextField;

	private JTextField commandTextField;
	private long lastCommandStart=0;
	private Socket socket;
	private ILineReader in;
	private ILineWriter out;
	int previousPosition=0;
	private List<String> previousCommands = new ArrayList<>();
	private JButton controlButton;
	private JButton sendCommandButton;
	private JComboBox<String> lineFeedTypeComboBox;
	private JTextArea responseTextArea;
	private JLabel statusLabel;
	private JTextField timeoutTextField;
	private JButton btnNewButton;
	private JComboBox<String> portComboBox;
	private JButton btnStartTls;
	private JCheckBox secureCheckbox;
	private JToggleButton autoFlushToggleButton;

	TrustManager[] trustAllCerts = new TrustManager[] { 
		    new X509TrustManager() {     
		        public void checkClientTrusted( X509Certificate[] certs, String authType) {
		        	
		        }
		        
		        public void checkServerTrusted(X509Certificate[] certs, String authType) {
		        	
		        }
		        
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
				
		    } 
		}; 
	private JCheckBox trustAllCheckBox;
	private JButton base64Button;
	
	/**
	 * Launch the application.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TelnetApplication window = new TelnetApplication();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TelnetApplication() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1240, 618);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel configurationPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) configurationPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frame.getContentPane().add(configurationPanel, BorderLayout.NORTH);

		hostNameTextField = new JTextField();
		hostNameTextField.setText("bringardner.us");
		hostNameTextField.setBorder(new TitledBorder(null, "HostName", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		configurationPanel.add(hostNameTextField);
		hostNameTextField.setColumns(20);

		controlButton = new JButton("Connect");
		controlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionControl();
			}
		});

		portComboBox = new JComboBox<String>();
		portComboBox.setBorder(new TitledBorder(null, "Port", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		portComboBox.setEditable(true);
		portComboBox.setModel(new DefaultComboBoxModel<String>(wellKNownPortames));
		configurationPanel.add(portComboBox);

		timeoutTextField = new JTextField();
		timeoutTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		timeoutTextField.setText("5000");
		timeoutTextField.setBorder(new TitledBorder(null, "Timeout(Milis)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		configurationPanel.add(timeoutTextField);
		timeoutTextField.setColumns(10);
		
		secureCheckbox = new JCheckBox("Secure");
		configurationPanel.add(secureCheckbox);
		
		trustAllCheckBox = new JCheckBox("Trust All");
		trustAllCheckBox.setSelected(true);
		configurationPanel.add(trustAllCheckBox);
		configurationPanel.add(controlButton);

		btnStartTls = new JButton("Start TLS");
		btnStartTls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionStartTLS();
			}
		});
		configurationPanel.add(btnStartTls);

		JPanel statusPanel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) statusPanel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);

		statusLabel = new JLabel("Status");
		statusPanel.add(statusLabel);

		JPanel centerPanel = new JPanel();
		frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		centerPanel.add(panel, BorderLayout.NORTH);

		commandTextField = new JTextField();
		commandTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionSendCommand();
			}
		});
		commandTextField.setEnabled(false);
		commandTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getExtendedKeyCode();
				if( previousCommands.size()>0) {
					if( code == 38) {
						//up
						if(previousPosition> 0 ) {
							commandTextField.setText(previousCommands.get(--previousPosition));
						}
					} else if( code == 40) {
						int sz = previousCommands.size();
						if(previousPosition< (sz-1) ) {
							commandTextField.setText(previousCommands.get(++previousPosition));
						} else {
							commandTextField.setText("");
						}
					}
				}
			}
		});
		panel.add(commandTextField);
		commandTextField.setColumns(50);

		sendCommandButton = new JButton("Send Command");
		sendCommandButton.setEnabled(false);
		sendCommandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionSendCommand();
			}
		});
		panel.add(sendCommandButton);
		
		autoFlushToggleButton = new JToggleButton("Auto Flush");
		autoFlushToggleButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				actionAutoFlushStateChanged();
			}
		});
		autoFlushToggleButton.setSelected(true);
		panel.add(autoFlushToggleButton);

		lineFeedTypeComboBox = new JComboBox<String>();
		lineFeedTypeComboBox.setToolTipText("End of line type");
		lineFeedTypeComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"CRLF", "LF"}));
		lineFeedTypeComboBox.setSelectedIndex(0);
		panel.add(lineFeedTypeComboBox);

		btnNewButton = new JButton("Clear");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionClear();
			}
		});
		panel.add(btnNewButton);
		
		base64Button = new JButton("Base64");
		base64Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionBase64();
			}
		});
		panel.add(base64Button);

		JScrollPane scrollPane = new JScrollPane();
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		responseTextArea = new JTextArea();
		responseTextArea.setColumns(80);
		scrollPane.setViewportView(responseTextArea);
	}

	protected void actionBase64() {
		String val = commandTextField.getText();
		String tmp = Base64.getEncoder().encodeToString(val.getBytes());
		commandTextField.setText(tmp);
		
	}

	protected void actionAutoFlushStateChanged() {
		if( out != null ) {
			boolean on = autoFlushToggleButton.isSelected();
			if( on && !out.isAutoFlush()) {
				try {
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(frame, e.toString(), "IO error", JOptionPane.ERROR_MESSAGE);
				}
			}
			out.setAutoFlush(on);
		}
		
	}

	protected void actionStartTLS() {

		Cursor cursor = frame.getCursor();
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		try {
			responseReader.pause();

			int cnt = 0;
			while(!responseReader.isPaused) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
				if( ++cnt > 1000) {
					System.out.println("Taking too long");
					break;
				}
			}
			lastCommandStart = System.currentTimeMillis();
			
			out.writeLine("STARTTLS");
			String line = in.readLine();
			
			if(line.startsWith("2")) {
				SSLSocketFactory factory = getSSLSocketFactory();
				SSLSocket sslSocket = (SSLSocket)factory.createSocket(socket,null, socket.getPort(), true);
				sslSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
					
					@Override
					public void handshakeCompleted(HandshakeCompletedEvent event) {
						System.out.println("Handshake complete");
						
						try {
							in = new CRLFLineReader(sslSocket.getInputStream());
							out = new CRLFLineWriter(sslSocket.getOutputStream());
							out.setAutoFlush(autoFlushToggleButton.isSelected());							
							socket = sslSocket;
							btnStartTls.setEnabled(false);
							
						} catch (IOException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(frame, e.toString(), "Handshake error", JOptionPane.ERROR_MESSAGE);
						}
						responseReader.restart();
						frame.setCursor(cursor);
						
					}
				});
				
				sslSocket.startHandshake();
				
			} else {
				responseReader.restart();
				frame.setCursor(cursor);
				JOptionPane.showMessageDialog(frame, "Unexpected response from server="+responseReader.lastLine, "Logic error", JOptionPane.ERROR_MESSAGE);
			}
		
		} catch (Exception e) {
			responseReader.restart();
			frame.setCursor(cursor);
			JOptionPane.showMessageDialog(frame, e.toString(), "Logic error", JOptionPane.ERROR_MESSAGE);
		}
		

	}

	protected void actionClear() {
		responseTextArea.setText("");		
	}

	protected void actionControl() {

		if( controlButton.getText().equals(CONNECT)) {
			actionConnect();
		} else if( controlButton.getText().equals(DISCONNECT)) {
			actionDisConnect();
		} else {
			JOptionPane.showMessageDialog(frame, "I did not expect "+controlButton.getText()+" as button text", "Logic error", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void actionDisConnect() {
		closeIfNeeded();
		commandTextField.setEnabled(false);
		sendCommandButton.setEnabled(false);
		secureCheckbox.setEnabled(true);
		hostNameTextField.setEnabled(true);
		portComboBox.setEnabled(true);
		controlButton.setText(CONNECT);
		statusLabel.setText("");
		frame.setTitle("");

	}

	private void actionConnect() {
		closeIfNeeded();


		String host = hostNameTextField.getText();
		String tmp = (String) portComboBox.getSelectedItem();
		int port = -1;
		if( wellKnownPorts.containsKey(tmp)) {
			port = wellKnownPorts.get(tmp).port;
		} else {
			try {
				port = Integer.parseInt(tmp);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Please enter a valid port", "", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		if( port <1 || port > 65535) {
			JOptionPane.showMessageDialog(frame, "Ports must be between 1 and 65535", "", JOptionPane.ERROR_MESSAGE);
			return;
		}

		tmp = timeoutTextField.getText();
		int timeout = -1;
		try {
			timeout = Integer.parseInt(tmp);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Please enter a time out", "", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if( timeout <1 ) {
			JOptionPane.showMessageDialog(frame, "Timout must be > 0 ", "", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Socket sock =null;
		ILineReader tin = null;
		ILineWriter tout = null;
		
		try {
			boolean secure = secureCheckbox.isSelected();
			if( secure ) {
				SSLSocketFactory factory = getSSLSocketFactory();
				sock= factory.createSocket();
			} else {
				sock = new Socket();
			}
			sock.connect(new InetSocketAddress(host, port), timeout);
			sock.setSoTimeout(timeout);
			//sock.setSoLinger(true, timeout);
			if (sock instanceof SSLSocket) {
				SSLSocket ssl = (SSLSocket) sock;
				
				ssl.addHandshakeCompletedListener(new HandshakeCompletedListener() {
					
					@Override
					public void handshakeCompleted(HandshakeCompletedEvent event) {
						System.out.println("Handshake complete");
						
					}
				});
				ssl.startHandshake();
			}
			
			String type = lineFeedTypeComboBox.getSelectedItem().toString();
			if( "CRLF".equals(type) ) {
				tin = new CRLFLineReader(sock.getInputStream());
				tout = new CRLFLineWriter(sock.getOutputStream());
			} else {
				tin = new LFLineReader(sock.getInputStream());
				tout = new LFLineWriter(sock.getOutputStream());
			}
			tout.setAutoFlush(autoFlushToggleButton.isSelected());
			in = tin;
			out = tout;
			socket = sock;
			responseReader = new ResponseReader();
			new Thread(responseReader).start();
			commandTextField.setEnabled(true);
			sendCommandButton.setEnabled(true);
			hostNameTextField.setEnabled(false);
			portComboBox.setEnabled(false);
			btnStartTls.setEnabled(true);
			controlButton.setText(DISCONNECT);
			statusLabel.setText("Connected to "+host+":"+port);
			
			frame.setTitle("Connected to "+host+":"+port+" "+(secure?"secure":""));
			secureCheckbox.setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.toString(), "Could not connect", JOptionPane.ERROR_MESSAGE);			
		}

	}

	private SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		if( trustAllCheckBox.isSelected() ) {
			SSLContext sc = SSLContext.getInstance("SSL"); 
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    factory = sc.getSocketFactory();
		}
		return factory;
	}

	private void closeIfNeeded() {
		if( responseReader != null ) {
			try {
				responseReader.stop();
			} catch (Exception e) {
			}
			responseReader = null;
		}
		if( in != null ) {
			try {
				in.close();
			} catch (Exception e) {
			}
			in = null;
		}
		if( out != null ) {
			try {
				out.close();
			} catch (Exception e) {
			}
			out = null;
		}

		if( socket != null ) {
			try {
				socket.close();
			} catch (Exception e) {
			}
			socket = null;
		}

	}

	protected void actionSendCommand() {
		String val = commandTextField.getText().trim();
		if( !val.isEmpty()) {
			previousCommands.add(val);
			previousPosition = previousCommands.size();
			try {
				responseTextArea.append("--> ");
				responseTextArea.append(val);
				responseTextArea.append("\n");
				lastCommandStart = System.currentTimeMillis();
				out.writeLine(val);
				commandTextField.setText("");
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, e.getMessage(), "Error writing to server", JOptionPane.ERROR_MESSAGE);
			}
		}		
	}
}
