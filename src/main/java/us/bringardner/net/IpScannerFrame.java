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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class IpScannerFrame extends JFrame {

	/**
	 * I find myself asking what's running out there..  This class MIGHT help answer that question
	 */
	private static final long serialVersionUID = 1L;

	class Address {
		int[] addr ;
		String name;
		
		public Address(String name) throws IOException {
			this.name = name;
			InetAddress tmp = InetAddress.getByName(name);
			String str = tmp.toString();
			str = str.substring(str.lastIndexOf('/')+1);
			String [] parts = str.split("[.]");
			if( parts.length != 4) {
				throw new IOException("Bad address count = "+parts.length);
			}
			
			addr = new int[4];
			for (int idx = 0; idx < parts.length; idx++) {
				addr[idx] = Integer.parseInt(parts[idx]);
			}			
		}
		
		public void increment() {
			if( (++addr[3]) > 255 ) {
				addr[3]=1;
				if( (++addr[2]) > 255 ) {
					addr[2]=1;
					if( (++addr[1]) > 255 ) {
						addr[1]=1;
						if( (++addr[0]) > 255 ) {
							throw new IllegalArgumentException("Can't increment past addr[0] past 255");
						}
					}
				}
			}
		}
		
		public String toString() {
			return ""+addr[0]+"."+addr[1]+"."+addr[2]+"."+addr[3];
		}
		
		public InetAddress getInetAddress() throws IOException{
			return InetAddress.getByName(toString());
		}
		
		public boolean equals(Object obj) {
			boolean ret = false;
			if (obj instanceof Address) {
				Address a = (Address) obj;
				
				return addr[0]==a.addr[0]&&addr[1]==a.addr[1]&&addr[2]==a.addr[2]&&addr[3]==a.addr[3];
			}
			return ret;
		}

		public Address copy() throws IOException {			
			return new Address(name);
		};
	}
	
	class Scanner implements Runnable {
		Address startIp;
		Address endIp;
		int startPort;
		int endPort;
		boolean running = false;

		public Scanner (String startIp,String endIp,int startPort,int endPort) throws IOException {
			this.startIp=new Address(startIp);
			this.endIp = new Address(endIp);
			this.startPort=startPort;
			this.endPort=endPort;			
		}



		@Override
		public void run() {
			running = true;
			log("Enter run");
			SwingUtilities.invokeLater(()-> actionButton.setText("Stop"));
			log("Scanner "+startIp+":"+startPort+"->"+endIp+":"+endPort);
			Thread.yield();
			
			Address addr=null;
			try {
				addr = startIp.copy();
			} catch (IOException e) {
				// Not implemented
				e.printStackTrace();
			}

			while( running && !addr.equals(endIp)) {
				for(int port = startPort; port <= endPort; port++ ) {
					test(addr,port);
				}
				addr.increment();
			}


			running = false;//  Just in case
			SwingUtilities.invokeLater(()-> actionButton.setText("Start"));
			log("Exit run");
		}

		private void log(final String msg) {
			SwingUtilities.invokeLater(()-> editorPane.append(msg+"\n"));
		}



		
		private void test(Address  addr, int port) {
			Socket socket = new Socket();

			try {
				InetAddress ia = addr.getInetAddress();

				socket.setSoTimeout(timeout);
				log("Connecting "+ia+":"+port);
				socket.connect(new InetSocketAddress(ia, port),timeout);
				log("Connected "+ia+":"+port);
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				log("Have Streams "+ia+":"+port+" availble = "+in.available());
				out.write("GET / HTTP/1.0\r\n\r\n".getBytes());
				log("After write "+ia+":"+port+" availble = "+in.available());
				if( in.available()>0) {
					byte [] data = new byte[in.available()];
					@SuppressWarnings("unused")
					int got = in.read(data);
					String str = new String(data);
					log("Input:"+str);
				}
			} catch (IOException e) {
				log(e.toString());
				if (!(e instanceof SocketTimeoutException)) {
					//e.printStackTrace();	
					
				}
				
			} finally {
				try {
					socket.close();
				} catch (Exception e2) {
				}
			}

		}

		
	}

	private JPanel contentPane;
	private final JPanel northPanel = new JPanel();
	private JTextField startIpTextField;
	private JTextField endIpTextField;
	private JTextField startPortTextField;
	private JTextField endPortTextField;
	private JButton actionButton;
	private JTextField timeoutTextField;
	private int timeout=6000;
	private Scanner scanner;
	private JTextArea editorPane;
	private JButton btnNewButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IpScannerFrame frame = new IpScannerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public IpScannerFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 800);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);

		editorPane = new JTextArea();
		scrollPane.setViewportView(editorPane);
		FlowLayout flowLayout = (FlowLayout) northPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		contentPane.add(northPanel, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "IP Range", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		northPanel.add(panel);

		startIpTextField = new JTextField();
		panel.add(startIpTextField);
		startIpTextField.setText("192.168.1.140");
		startIpTextField.setColumns(10);

		endIpTextField = new JTextField();
		panel.add(endIpTextField);
		endIpTextField.setText("192.168.1.200");
		endIpTextField.setColumns(10);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Port Range", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		northPanel.add(panel_1);

		startPortTextField = new JTextField();
		startPortTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(startPortTextField);
		startPortTextField.setText("80");
		startPortTextField.setColumns(5);

		endPortTextField = new JTextField();
		endPortTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(endPortTextField);
		endPortTextField.setText("80");
		endPortTextField.setColumns(5);

		timeoutTextField = new JTextField();
		timeoutTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionSetTimeout();
			}
		});

		timeoutTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				actionSetTimeout();
			}
		});
		timeoutTextField.setBorder(new TitledBorder(null, "Timeout (ms)", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		timeoutTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		timeoutTextField.setText(""+timeout);
		northPanel.add(timeoutTextField);
		timeoutTextField.setColumns(8);

		actionButton = new JButton("Start");
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionAction();
			}
		});
		northPanel.add(actionButton);
		
		btnNewButton = new JButton("Clear");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				actionClear();
			}
		});
		northPanel.add(btnNewButton);
	}


	protected void actionClear() {
		editorPane.setText("");		
	}

	/*
	 * 48155276
	 * 
	 * Ci....ndY
	 * CiSadie15ndY
	 * 
	 */
	protected void actionSetTimeout() {
		timeout = Integer.parseInt(timeoutTextField.getText());		
	}

	protected void actionAction() {
		if( actionButton.getText().equals("Start")) {
			if( scanner != null && scanner.running) {
				scanner.running = false;
			}

			try {
				scanner = new Scanner(startIpTextField.getText(),endIpTextField.getText(),Integer.parseInt(startPortTextField.getText()),Integer.parseInt(endPortTextField.getText()));
			} catch (NumberFormatException | IOException e) {
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				
			}
			new Thread(scanner).start();	
		} else {
			scanner.running = false;			
		}		
	}

}
