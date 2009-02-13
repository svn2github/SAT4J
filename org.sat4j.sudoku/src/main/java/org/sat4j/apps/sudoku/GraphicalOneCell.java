package org.sat4j.apps.sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class GraphicalOneCell extends JLabel implements OneCell,
        ActionListener, MouseWheelListener {

    public static final int CELL_WIDTH = 64;

    public static final int CELL_HEIGHT = 64;

    int max;

    boolean protcted;

    GUIInput guiInput;

    CellStatus status;

    Color[] colors;

    static Color[] nullColors = { Color.BLACK, Color.WHITE, Color.RED,
            Color.WHITE, Color.RED, Color.WHITE };

    // ...where instance variables are declared:
    JPopupMenu popup;

    static ImageIcon[] images = new ImageIcon[43];

    static {
        defaultOrder();
    }

    public static void defaultOrder() {
        for (int i = 0; i < 43; i++) {
            URL url = GraphicalOneCell.class.getResource("images/mahjongg-" + i
                    + ".jpg");
            images[i] = new ImageIcon(url);
        }
    }

    public static void bambooOrder() {
        defaultOrder();
        changeOrder(24);
    }

    public static void signsOrder() {
        defaultOrder();
        changeOrder(15);
    }

    private static void changeOrder(int distance) {
        defaultOrder();
        ImageIcon tempicon;
        for (int i = 0; i < 9; i++) {
            tempicon = images[i];
            images[i] = images[i + distance];
            images[i + distance] = tempicon;
        }
    }

    int value = 0;

    public GraphicalOneCell(GUIInput guiInput, Color[] colors) {
        super(images[42]);

        this.guiInput = guiInput;
        this.status = CellStatus.USER_ENTERED;
        this.colors = colors;
        setFont(getFont().deriveFont(Font.BOLD, 18));
        setHorizontalAlignment(SwingConstants.CENTER);
        unHighlight();
        // setBorder(BorderFactory.createLineBorder(Color.black));
        addMouseWheelListener(this);
    }

    public GraphicalOneCell() {
        this(null, nullColors);
    }

    public void protect() {
        protcted = true;
        setStatus(CellStatus.PROTECTED);
    }

    public void protectIfNotClear() {
        if (value > 0) {
            protect();
        }
    }

    public boolean getProtected() {
        return protcted;
    }

    public void clearProtection() {
        protcted = false;
        setStatus(CellStatus.USER_ENTERED);
    }

    public GraphicalOneCell(int max, GUIInput guiInput, Color[] colors) {

        this(guiInput, colors);

        this.max = max;
        protcted = false;
        // ...where the GUI is constructed:
        // Create the popup menu.
        updateContextualMenu();
        // Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
        refresh();
    }

    public GraphicalOneCell(int max) {

        this(max, null, nullColors);

    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getaText() {
        if (value == 0)
            return "";
        return "" + value;
    }

    public int getIntValue() {
        return value;
    }

    public void setIntValue(int v) {
        if (!protcted) {
            if (v == 0) {
                setIcon(images[42]);
            } else {
                setIcon(images[v - 1]);
            }
            value = v;
        }
    }

    public void setIntValue(String v) {
        if (!protcted) {
            value = Integer.parseInt(v);
            setIcon(images[Integer.parseInt(v) - 1]);
        }
    }

    public void setIntValue(Icon v) {
        if (!protcted) {
            if (v == images[42]) {
                value = 0;
            } else {
                for (int i = 0; i <= max; i++) {
                    if (images[i] == v) {
                        value = i + 1;
                        break;
                    }
                }
            }
            setIcon(v);
        }
    }

    public void solverSetIntValue(int v) {
        if (!protcted && value == 0) {
            setIcon(images[v - 1]);
            value = v;
            // setText(renderer.intToString(v));
            setStatus(CellStatus.SOLVER_ENTERED);
        }
    }

    public void clear() {
        unHighlight();
        if (!protcted) {
            setIcon(images[42]);
            setStatus(CellStatus.USER_ENTERED);
            value = 0;
        }
    }

    public void clearComputers() {
        unHighlight();
        if (!protcted && (status == CellStatus.SOLVER_ENTERED)) {
            setIcon(images[42]);
            setStatus(CellStatus.USER_ENTERED);
            value = 0;
        }

    }

    public void highlight() {
        setBackground(colors[HIGHLIGHT_COLOR_ID]);
        setEnabled(false);
    }

    public static final int USER_COLOR_ID = 0, SOLVER_COLOR_ID = 1,
            HIGHLIGHT_COLOR_ID = 2, DISABLED_COLOR_ID = 3,
            PROTECTED_COLOR_ID = 4, BACKGROUND_COLOR_ID = 5,
            UNUSED_COLOR_ID = 6;

    public void unHighlight() {
        setStatus(status);
        setEnabled(true);
    }

    public void refresh() {
        setStatus(status);
        setEnabled(true);
    }

    public void setStatus(CellStatus status) {
        this.status = status;
        switch (status) {
        case PROTECTED:
            setBackground(colors[BACKGROUND_COLOR_ID]);
            setForeground(colors[PROTECTED_COLOR_ID]);
            setToolTipText(guiInput.suDokuResources.getStringFromKey("TOOLTIP_CELL_PROTECTED"));
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            break;
        case USER_ENTERED:
            setForeground(colors[USER_COLOR_ID]);
            setBackground(colors[BACKGROUND_COLOR_ID]);
            setToolTipText(guiInput.suDokuResources.getStringFromKey("TOOLTIP_CELL_NOT_PROTECTED_GRAPH"));
            setBorder(new BevelBorder(BevelBorder.RAISED));
            break;
        case SOLVER_ENTERED:
            setForeground(colors[SOLVER_COLOR_ID]);
            setBackground(colors[BACKGROUND_COLOR_ID]);
            setToolTipText(guiInput.suDokuResources.getStringFromKey("TOOLTIP_CELL_COMPUTER"));
            setBorder(new BevelBorder(BevelBorder.RAISED));
            break;

        }
    }

    public CellStatus getStatus() {
        return status;
    }

    public static int getNumberOfColours() {
        return UNUSED_COLOR_ID;
    }

    // @Override
    // protected Document createDefaultModel() {
    // return new BoundedIntegerDocument();
    // }

    public class BoundedIntegerDocument extends PlainDocument {

        BoundedIntegerDocument() {
            super();
        }

        @Override
        public void remove(int off, int len) throws BadLocationException {
            if (status != CellStatus.PROTECTED) {
                super.remove(off, len);
            }
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {

            if ((str == null) || (status == CellStatus.PROTECTED)) {
                return;
            }

            unHighlight();

            if ((offs == 0) && (str.length() == 0)) {
                setStatus(CellStatus.USER_ENTERED);
                super.insertString(offs, str, a);
                return;
            }

            boolean ok;
            int v;

            ok = false;

            try {
                v = Integer.parseInt(getaText() + str);
                ok = ((v >= 1) && (v <= max));
                if (ok) {
                    setStatus(CellStatus.USER_ENTERED);
                    super.insertString(offs, str, a);
                }
            } catch (Exception e) {
            }

        }
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
            refresh();
        }

        private void maybeShowPopup(MouseEvent e) {
            if (!protcted && e.isPopupTrigger()) {
                setBackground(Color.DARK_GRAY);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Icon value = ((JMenuItem) e.getSource()).getIcon();
        setIntValue(value);
        refresh();
    }

    public void updateContextualMenu() {
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(images[42]);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        for (int i = 1; i <= max; i++) {
            ImageIcon smallIcon = images[i - 1]; // new
            // ImageIcon(images[i-1].getImage().getScaledInstance(32,
            // -1, 0));
            menuItem = new JMenuItem(smallIcon);
            menuItem.addActionListener(this);
            popup.add(menuItem);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        // if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
        int total = (value + e.getUnitsToScroll() / 3) % (max + 1);
        if (total < 0) {
            total += max + 1;
        }
        setIntValue(total);
        // }
    }

}
