package org.sat4j.sat;

import javax.swing.JPanel;

public abstract class CommandComponent extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public CommandComponent() {
        super();
    }

    public abstract void createPanel();

}
