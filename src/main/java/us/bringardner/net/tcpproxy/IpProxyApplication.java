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
/*
 * IpProxyApplication.java
 *
 * 
 */

package us.bringardner.net.tcpproxy;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocketFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author  Tony Bringardner
 */
public class IpProxyApplication extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	private ProxyServer server;
	private File file;

	/** Creates new form IpProxyApplication */
	public IpProxyApplication() {
		setLocationRelativeTo(null);
		buttonGroup = new javax.swing.ButtonGroup();
		detailPanel = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		browseButton = new javax.swing.JButton();
		fileNameFld = new javax.swing.JTextField();
		buttonPanel = new javax.swing.JPanel();
		asciiButton = new javax.swing.JRadioButton();
		binaryButton = new javax.swing.JRadioButton();
		jPanel5 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		inPortFld = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jPanel3 = new javax.swing.JPanel();
		hostFld = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		outPortFld = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		latencySpinner = new javax.swing.JSpinner();
		jPanel4 = new javax.swing.JPanel();
		runButton = new javax.swing.JButton();
		jButton1 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		countLabel = new javax.swing.JLabel();
		refreshButton = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		textArea = new javax.swing.JTextArea();
		menuBar = new javax.swing.JMenuBar();
		fileMenu = new javax.swing.JMenu();
		openMenuItem = new javax.swing.JMenuItem();
		saveMenuItem = new javax.swing.JMenuItem();
		saveAsMenuItem = new javax.swing.JMenuItem();
		exitMenuItem = new javax.swing.JMenuItem();
		editMenu = new javax.swing.JMenu();
		cutMenuItem = new javax.swing.JMenuItem();
		copyMenuItem = new javax.swing.JMenuItem();
		pasteMenuItem = new javax.swing.JMenuItem();
		deleteMenuItem = new javax.swing.JMenuItem();
		helpMenu = new javax.swing.JMenu();
		contentsMenuItem = new javax.swing.JMenuItem();
		aboutMenuItem = new javax.swing.JMenuItem();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		detailPanel.setLayout(new java.awt.GridLayout(4, 0, 0, 2));

		jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		jLabel4.setText("Capture File:");
		jPanel1.add(jLabel4);

		browseButton.setText("Browse");
		browseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				browseButtonActionPerformed(evt);
			}
		});
		jPanel1.add(browseButton);

		fileNameFld.setColumns(60);
		jPanel1.add(fileNameFld);

		buttonPanel.setLayout(new java.awt.GridLayout(2, 0));

		buttonGroup.add(asciiButton);
		asciiButton.setSelected(true);
		asciiButton.setText("Formated Ascii");
		buttonPanel.add(asciiButton);

		buttonGroup.add(binaryButton);
		binaryButton.setText("Binary");
		buttonPanel.add(binaryButton);

		jPanel1.add(buttonPanel);

		detailPanel.add(jPanel1);

		jPanel5.setPreferredSize(new java.awt.Dimension(1199, 50));
		jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		jLabel1.setText("Incoming Port:");
		jPanel5.add(jLabel1);

		inPortFld.setColumns(4);
		inPortFld.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		inPortFld.setText("9160");
		jPanel5.add(inPortFld);

		jLabel2.setText("Forward To Host:");
		jPanel5.add(jLabel2);

		jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		hostFld.setColumns(20);
		jPanel3.add(hostFld);

		jLabel3.setText("Forward To Port:");
		jPanel3.add(jLabel3);

		outPortFld.setColumns(4);
		outPortFld.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
		jPanel3.add(outPortFld);

		jPanel5.add(jPanel3);

		jLabel5.setText("Add Latency(ms)");
		jPanel5.add(jLabel5);

		latencySpinner.setModel(new javax.swing.SpinnerNumberModel());
		latencySpinner.setPreferredSize(new java.awt.Dimension(50, 20));
		jPanel5.add(latencySpinner);

		detailPanel.add(jPanel5);
		
		sslCheckBox = new JCheckBox("Open remote as SSL");
		sslCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
		sslCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		jPanel5.add(sslCheckBox);

		runButton.setText("Start");
		runButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				runButtonActionPerformed(evt);
			}
		});
		jPanel4.add(runButton);

		jButton1.setText("Clear");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		jPanel4.add(jButton1);

		detailPanel.add(jPanel4);

		countLabel.setText("Current = 100 Total = 2000");
		jPanel2.add(countLabel);

		refreshButton.setText("Refresh");
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refreshButtonActionPerformed(evt);
			}
		});
		jPanel2.add(refreshButton);

		detailPanel.add(jPanel2);

		getContentPane().add(detailPanel, BorderLayout.NORTH);

		textArea.setColumns(20);
		textArea.setRows(5);
		jScrollPane1.setViewportView(textArea);

		getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
		
		
		
		
		fileMenu.setText("File");

		openMenuItem.setText("Open");
		openMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				openMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(openMenuItem);

		saveMenuItem.setText("Save");
		saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(saveMenuItem);

		saveAsMenuItem.setText("Save As ...");
		saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveAsMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(saveAsMenuItem);

		exitMenuItem.setText("Exit");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		editMenu.setText("Edit");

		cutMenuItem.setText("Cut");
		editMenu.add(cutMenuItem);

		copyMenuItem.setText("Copy");
		editMenu.add(copyMenuItem);

		pasteMenuItem.setText("Paste");
		editMenu.add(pasteMenuItem);

		deleteMenuItem.setText("Delete");
		editMenu.add(deleteMenuItem);

		menuBar.add(editMenu);

		helpMenu.setText("Help");

		contentsMenuItem.setText("Contents");
		helpMenu.add(contentsMenuItem);

		aboutMenuItem.setText("About");
		helpMenu.add(aboutMenuItem);

		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		pack();
	}// </editor-fold>
	//GEN-END:initComponents

	private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Open");
	}

	private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Save");
		if (file != null) {
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);

			} catch (FileNotFoundException e) {
				// Not implemented
				e.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (Exception e2) {

					}
				}
			}

		}
	}

	private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Save as");
		JFileChooser fc = new JFileChooser();
		int sel = fc.showSaveDialog(this);
		if (sel == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			saveMenuItemActionPerformed(null);
		}
	}

	private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (server != null) {
			int tot = server.getConenctionCount();
			int closed = server.getCloseCount() / 2;
			int now = tot - closed;
			countLabel.setText(String.format("Current=%d  Closed=%d Total=%d",
					now, closed, tot));
		}
	}

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {
		JFileChooser fc = new JFileChooser();
		String fileName = fileNameFld.getText();
		if (fileName != null && fileName.length() > 0) {
			fc.setSelectedFile(new File(fileName));
		}
		if (fc.showDialog(this, "Select Capture File") == JFileChooser.APPROVE_OPTION) {
			fileNameFld.setText(fc.getSelectedFile().getAbsolutePath());
		}

	}

	private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (server != null) {

			server.stop();
			while (server.isRunning()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
			}
			refreshButtonActionPerformed(evt);
			server = null;
			runButton.setText("Start");
			enableControl(true);
		} else {
			try {
				int in = Integer.parseInt(inPortFld.getText());
				int out = Integer.parseInt(outPortFld.getText());
				String host = hostFld.getText();
				if (in <= 0) {
					textArea.append("Invalid inport = " + in);
				} else if (out <= 0) {
					textArea.append("Invalid outport = " + out);
				} else if (host == null || host.length() == 0) {
					textArea.append("Invalid host name = " + host);
				} else {
					server = new ProxyServer(in, out, host,fileNameFld.getText());
					if( sslCheckBox.isSelected()) {
						server.setSocketFactory(SSLSocketFactory.getDefault());
						textArea.append("Forward as SSL to " + host);
					}
					
					server.setText(textArea);
					server.setFormatAscii(asciiButton.isSelected());
					server.setAcceptTimeout(2000);
					server.setLatency(Integer.parseInt(latencySpinner.getValue().toString()));
					server.start();
					runButton.setText("Stop");
					enableControl(false);

					new Thread(new Runnable() {

				
						public void run() {
							while (runButton.getText().equals("Stop")) {
								try {
									Thread.sleep(10000);
								} catch (InterruptedException e) {
									// Not implemented
									e.printStackTrace();
								}
								refreshButtonActionPerformed(null);
							}
						}
					}).start();
				}
			} catch (Throwable e) {
				textArea.append("Error:" + e);
			}
		}
	}

	private void enableControl(boolean b) {
		inPortFld.setEnabled(b);
		outPortFld.setEnabled(b);
		hostFld.setEnabled(b);
		fileNameFld.setEnabled(b);
		browseButton.setEnabled(b);
		asciiButton.setEnabled(b);
		binaryButton.setEnabled(b);
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		textArea.setText("");
	}

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
		System.exit(0);
	}//GEN-LAST:event_exitMenuItemActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new IpProxyApplication().setVisible(true);
			}
		});
	}

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JMenuItem aboutMenuItem;
	private javax.swing.JRadioButton asciiButton;
	private javax.swing.JRadioButton binaryButton;
	private javax.swing.JButton browseButton;
	private javax.swing.ButtonGroup buttonGroup;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JMenuItem contentsMenuItem;
	private javax.swing.JMenuItem copyMenuItem;
	private javax.swing.JLabel countLabel;
	private javax.swing.JMenuItem cutMenuItem;
	private javax.swing.JMenuItem deleteMenuItem;
	private javax.swing.JPanel detailPanel;
	private javax.swing.JMenu editMenu;
	private javax.swing.JMenuItem exitMenuItem;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JTextField fileNameFld;
	private javax.swing.JMenu helpMenu;
	private javax.swing.JTextField hostFld;
	private javax.swing.JTextField inPortFld;
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JSpinner latencySpinner;
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JMenuItem openMenuItem;
	private javax.swing.JTextField outPortFld;
	private javax.swing.JMenuItem pasteMenuItem;
	private javax.swing.JButton refreshButton;
	private javax.swing.JButton runButton;
	private javax.swing.JMenuItem saveAsMenuItem;
	private javax.swing.JMenuItem saveMenuItem;
	private javax.swing.JTextArea textArea;
	private JCheckBox sslCheckBox;
	
	// End of variables declaration//GEN-END:variables

}