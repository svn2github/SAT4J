package org.sat4j.sat;

import info.monitorenter.gui.chart.Chart2D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MyChartPanel extends JPanel {

//	private Chart2D chart;
	private JLabel titleLabel;
//	private String titleText;
	
	public MyChartPanel(Chart2D chart, String title){
		super();
//		this.chart = chart;
//		this.titleText = title;
		this.titleLabel = new JLabel(title);
		
		Font f = this.titleLabel.getFont();
		
//		chart.setBackground(Color.BLACK);
//		chart.setForeground(Color.white);

		// bold
		this.titleLabel.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		
		
//		this.setBorder(new CompoundBorder(new TitledBorder(null, "", 
//				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(2, 2, 2, 2)));
		this.setBorder(new EmptyBorder(4, 4, 4, 4));
		this.setLayout(new BorderLayout());
		
		JPanel labelPanel = new JPanel();
		
		labelPanel.add(titleLabel);
		this.add(labelPanel,BorderLayout.NORTH);
		this.add(chart,BorderLayout.CENTER);
	}
		
		
}
