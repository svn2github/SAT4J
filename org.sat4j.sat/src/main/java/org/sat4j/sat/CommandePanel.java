package org.sat4j.sat;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CommandePanel extends JPanel{
	
	private TelecommandeStrategy telecomStrategy;
	
	public final static String RESTART = "Restart";
	public final static String CLEAN = "Clean";
	
	private JButton restartButton;
	private JButton cleanButton;
	
	private JTextArea console;
	
	public CommandePanel(TelecommandeStrategy telecomStrategy){
		super();
		
		this.setPreferredSize(new Dimension(200,200));
		
		this.telecomStrategy = telecomStrategy;
		
		restartButton = new JButton(RESTART);
		
		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnRestart();
			}
		});
		
		cleanButton = new JButton(CLEAN);
		
		cleanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hasClickedOnClean();
			}
		});
		
		console = new JTextArea();
		
		JScrollPane scrollPane = new JScrollPane(console);

		//scrollPane.setMinimumSize(new Dimension(100,100));
		scrollPane.setPreferredSize(new Dimension(100,100));	
		
		
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(restartButton);
		this.add(cleanButton);
		this.add(scrollPane);
	
		
	}
	
	public void hasClickedOnRestart(){
		telecomStrategy.setHasClickedOnRestart(true);
		console.append("Has clicked on " + RESTART + "\n");
		console.repaint();
		this.repaint();
	}
	
	public void hasClickedOnClean(){
		telecomStrategy.setHasClickedOnClean(true);
		console.append("Has clicked on " + CLEAN + "\n");
		console.repaint();
		this.repaint();
	}
	
//	public void maj(){
//		console.append(fauxModele.getLastCommande() + "\n");
//		console.repaint();
//		this.repaint();
//	}

}
