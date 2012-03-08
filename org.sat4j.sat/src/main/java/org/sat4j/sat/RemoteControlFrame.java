/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.sat4j.minisat.core.ICDCL;
import org.sat4j.minisat.core.ICDCLLogger;
import org.sat4j.minisat.orders.RandomWalkDecorator;

/**
 * 
 * JFrame for the remote control.
 * 
 * @author sroussel
 *
 */
public class RemoteControlFrame extends JFrame implements ICDCLLogger{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private String lookAndFeel;


	public static final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	private JMenuBar barreMenu;
	private JMenu menu;
	private JMenuItem activateTracing;
	
	private DetailedCommandPanel commandePanel;
	private String filename;
	
	private String ramdisk;

	private RemoteControlStrategy telecomStrategy;
	private RandomWalkDecorator randomWalk;
	private ICDCL solver;
	
	private final static String ACTIVATE  = "Activate Gnuplot Tracing";
	private final static String DEACTIVATE  = "Deactivate Gnuplot Tracing";

	public RemoteControlFrame(String filename, String ramdisk, ICDCL solver){
		super("Remote Control");
		
		this.filename=filename;
		this.ramdisk=ramdisk;
		this.solver=solver;
		initLookAndFeel();

		createAndShowGUI();
	}
	
	public RemoteControlFrame(String filename, String ramdisk){	
		this(filename, ramdisk,null);
	}
	
	public RemoteControlFrame(String filename){	
		this(filename, "",null);
	}
	
	public RemoteControlFrame(String filename, ICDCL solver){	
		this(filename, "",solver);
	}


	public void reinitialiser(){
	}

	
	public void setActivateGnuplot(boolean b){
		activateTracing.setSelected(b);
		activateGnuplotTracing(b);
	}

	public void initLookAndFeel(){
		JFrame.setDefaultLookAndFeelDecorated(true);
	}

	public void createAndShowGUI(){
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());

		
		createMenuBar();
		
		commandePanel = new DetailedCommandPanel(filename,ramdisk,solver);
		
		JScrollPane scrollPane = new JScrollPane(commandePanel);
		
		this.add(scrollPane);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
	
	public void clickOnAboutSolver(){
		if(commandePanel.getSolver()!=null)
		JOptionPane.showMessageDialog(this,
			    commandePanel.getSolver().toString());
		else{
			JOptionPane.showMessageDialog(this,
				    "No solver is running at the moment. Please start solver.");
		}
	}

	public void createMenuBar(){
		barreMenu = new JMenuBar();
		menu = new JMenu("File");
		barreMenu.add(menu);
		
		JMenuItem aboutSolver = new JMenuItem("About Solver");
		menu.add(aboutSolver);
		
		aboutSolver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clickOnAboutSolver();
			}
		});
		
		activateTracing = new JMenuItem(ACTIVATE);
		menu.add(activateTracing);
		
		activateTracing.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				activateGnuplotTracing(activateTracing.getText().equals(ACTIVATE));
			}
		});

//		JMenuItem reinitialiserItem = new JMenuItem("RŽinitialiser");
//		menu.add(reinitialiserItem);
//
//		reinitialiserItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				reinitialiser();
//			}
//		});

		JMenuItem quit = new JMenuItem("Exit");
		menu.add(quit);

		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				String[] cmdarray = new String[]{"killall","gnuplot"};
//				try{
//					Runtime.getRuntime().exec(cmdarray);
//				}
//				catch(IOException ex){
//					ex.printStackTrace();
//				}
//				System.exit(NORMAL);
				commandePanel.stopGnuplot();
				System.exit(NORMAL);
			}
		});
		this.setJMenuBar(barreMenu);
		
	}
	
	public void log(String message){
		commandePanel.log(message);
	}
	
	public void activateGnuplotTracing(boolean b){
		if(b){
			activateTracing.setText(DEACTIVATE);
		}
		else{
			activateTracing.setText(ACTIVATE);
		}
		commandePanel.activateGnuplotTracing(b);
	}





}