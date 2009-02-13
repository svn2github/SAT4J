package org.sat4j.apps.sudoku;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

class GeneralMenu<T extends Enum<T>> extends JMenu {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    GeneralMenu(String title, ButtonHandlerFactory<T> buttonHandlerFactory,
            SuDoku suDoku) {
        super(suDoku.getSuDokuResources().getStringFromKey(title));

        this.suDoku = suDoku;
        this.buttonHandlerFactory = buttonHandlerFactory;
    }

    JMenuItem addMenuItem(String key, Enum<T> command) {
        String name = suDoku.getSuDokuResources().getStringFromKey(key);
        ImageIcon icon = suDoku.getSuDokuResources().getIconFromKey(key);

        JMenuItem menuItem = new JMenuItem(name, icon);
        menuItem.addActionListener(buttonHandlerFactory
                .newButtonHandler(command));
        add(menuItem);
        return menuItem;
    }

    private SuDoku suDoku;

    private ButtonHandlerFactory<T> buttonHandlerFactory;
}
