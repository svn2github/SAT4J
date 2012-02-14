package org.sat4j.sat;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class TelecommandeFrame extends JFrame{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private String lookAndFeel;


	public static final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	private JMenuBar barreMenu;
	private JMenu menu;


	private TelecommandeStrategy telecomStrategy;


	public TelecommandeFrame(TelecommandeStrategy telecomStrategy){	
		super("Télécommande");

		this.telecomStrategy = telecomStrategy;

		initLookAndFeel();

		createAndShowGUI();
	}


	public void reinitialiser(){
	}


	public void initLookAndFeel(){
		JFrame.setDefaultLookAndFeelDecorated(true);
	}

	public void createAndShowGUI(){
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());

		
		createMenuBar();
		
		CommandePanel commandePanel = new CommandePanel(telecomStrategy);
		
		this.add(commandePanel);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	public void createMenuBar(){
		barreMenu = new JMenuBar();
		menu = new JMenu("Menu");
		barreMenu.add(menu);

//		JMenuItem reinitialiserItem = new JMenuItem("Réinitialiser");
//		menu.add(reinitialiserItem);
//
//		reinitialiserItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				reinitialiser();
//			}
//		});

		JMenuItem quit = new JMenuItem("Quitter");
		menu.add(quit);

		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(NORMAL);
			}
		});
		this.setJMenuBar(barreMenu);
		
	}





}