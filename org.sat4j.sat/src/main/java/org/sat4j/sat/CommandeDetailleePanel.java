/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *******************************************************************************/
package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.sat4j.minisat.core.RestartStrategy;


/**
 * 
 * This panel contains buttons that control restart and clean on solver.
 * It also displays history of commands.
 * 
 * @author sroussel
 *
 */
public class CommandeDetailleePanel extends JPanel{


	private static final long serialVersionUID = 1L;

	private static final EmptyBorder border5 = new EmptyBorder(5,5,5,5);

	private TelecommandeStrategy telecomStrategy;

	private final static String RESTART_PANEL = "Restart strategy";	
	private final static String RESTART = "Restart";
	
	private JPanel restartPanel;
	
	private JLabel chooseRestartStrategyLabel;
	private final static String CHOOSE_RESTART_STRATEGY = "Choose restart strategy: ";

	private JComboBox listeRestarts;
	private final static String RESTART_NO_STRATEGY = "No strategy";
	private final static String RESTART_STRATEGY_CLASS = "org.sat4j.minisat.core.RestartStrategy";
	private final static String RESTART_PATH="org.sat4j.minisat.restarts";

	private JButton restartButton;
	
	private final static String CLEAN_PANEL = "Learned Constraint Deletion Strategy";
	private final static String CLEAN = "Clean";
	
	private JPanel cleanPanel;
	
	private JButton cleanButton;

	private JTextArea console;

	public CommandeDetailleePanel(TelecommandeStrategy telecomStrategy){
		super();

		this.setPreferredSize(new Dimension(400,300));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.telecomStrategy = telecomStrategy;

		createRestartPanel();
		createCleanPanel();

		console = new JTextArea();

		JScrollPane scrollPane = new JScrollPane(console);

		//scrollPane.setMinimumSize(new Dimension(100,100));
		scrollPane.setPreferredSize(new Dimension(100,100));	




		this.add(restartPanel);
		this.add(cleanPanel);
		this.add(scrollPane);


	}

	public void hasClickedOnRestart(){
		telecomStrategy.setHasClickedOnRestart(true);
		String choix = (String)listeRestarts.getSelectedItem();
		if(choix.equals(RESTART_NO_STRATEGY)){
			telecomStrategy.setRestartStrategy(null);
		}
		else{
			try{
				RestartStrategy restart = (RestartStrategy)Class.forName(RESTART_PATH+"."+choix).newInstance();
				assert restart!=null;
				telecomStrategy.setRestartStrategy(restart);
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
			}
			catch(IllegalAccessException e){
				e.printStackTrace();
			}
			catch(InstantiationException e){
				e.printStackTrace();
			}
		}
		console.append("Has clicked on " + RESTART + " with "+ choix +"\n");
		console.repaint();
		this.repaint();
	}

	public void hasClickedOnClean(){
		telecomStrategy.setHasClickedOnClean(true);
		console.append("Has clicked on " + CLEAN +"\n");
		console.repaint();
		this.repaint();
	}


	public void createRestartPanel(){
		restartPanel = new JPanel();

		restartPanel.setName(RESTART_PANEL);
		restartPanel.setBorder(new CompoundBorder(new TitledBorder(null, restartPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		restartPanel.setLayout(new BorderLayout());

		JPanel tmpPanel1 = new JPanel();
		tmpPanel1.setLayout(new FlowLayout());

		chooseRestartStrategyLabel = new JLabel(CHOOSE_RESTART_STRATEGY);

		listeRestarts = new JComboBox(getListOfRestartStrategies());

		tmpPanel1.add(chooseRestartStrategyLabel);
		tmpPanel1.add(listeRestarts);

		restartButton = new JButton(RESTART);

		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnRestart();
			}
		});

		JPanel tmpPanel2 = new JPanel();
		tmpPanel2.add(restartButton);

		restartPanel.add(tmpPanel1,BorderLayout.CENTER);
		restartPanel.add(tmpPanel2,BorderLayout.SOUTH);

	}
	
	public void createCleanPanel(){
		cleanPanel = new JPanel();
		
		cleanPanel.setName(CLEAN_PANEL);
		cleanPanel.setBorder(new CompoundBorder(new TitledBorder(null, cleanPanel.getName(), 
				TitledBorder.LEFT, TitledBorder.TOP), border5));

		cleanPanel.setLayout(new BorderLayout());
		
		JPanel tmpPanel2 = new JPanel();
		
		cleanButton = new JButton(CLEAN);

		cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnClean();
			}
		});
		
		tmpPanel2.add(cleanButton);
		
		cleanPanel.add(tmpPanel2,BorderLayout.SOUTH);
		
	}

	public Vector<String> getListOfRestartStrategies(){
		Vector<String> resultRTSI = RTSI.find(RESTART_STRATEGY_CLASS);
		Vector<String> finalResult = new Vector<String>();

		finalResult.add(RESTART_NO_STRATEGY);

		for(String s:resultRTSI){
			if(!s.contains("Telecommande")){
				finalResult.add(s);
			}
		}

		return finalResult;
	}

	//	public void maj(){
	//		console.append(fauxModele.getLastCommande() + "\n");
	//		console.repaint();
	//		this.repaint();
	//	}

}
