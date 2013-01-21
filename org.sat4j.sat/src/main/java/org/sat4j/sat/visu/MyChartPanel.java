package org.sat4j.sat.visu;

import info.monitorenter.gui.chart.Chart2D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MyChartPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel titleLabel;

    public MyChartPanel(Chart2D chart, String title) {
        this(chart, title, Color.WHITE, Color.BLACK);
    }

    public MyChartPanel(Chart2D chart, String title, Color bg, Color fg) {
        super();
        this.titleLabel = new JLabel(title);

        Font f = this.titleLabel.getFont();

        chart.setBackground(bg);
        chart.setForeground(fg);

        // bold
        this.titleLabel.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));

        this.titleLabel.setBackground(bg);
        this.titleLabel.setForeground(fg);

        this.setBorder(new EmptyBorder(4, 4, 4, 4));
        this.setLayout(new BorderLayout());

        JPanel labelPanel = new JPanel();

        labelPanel.add(this.titleLabel);
        this.add(labelPanel, BorderLayout.NORTH);
        this.add(chart, BorderLayout.CENTER);
    }

}
