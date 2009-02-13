package org.sat4j.apps.sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class TextualOneCell extends JTextField implements ActionListener,
        OneCell {

    int max;

    boolean protcted;

    GUIInput guiInput;

    CellStatus status;

    Color[] colors;

    static Color[] nullColors = { Color.BLACK, Color.WHITE, Color.RED,
            Color.WHITE, Color.RED, Color.WHITE };

    // ...where instance variables are declared:
    JPopupMenu popup;

    public TextualOneCell(GUIInput guiInput, Color[] colors) {
        super(2);
        this.guiInput = guiInput;
        this.status = CellStatus.USER_ENTERED;
        this.colors = colors;
        setFont(getFont().deriveFont(Font.BOLD, 18));
        setHorizontalAlignment(SwingConstants.CENTER);
        unHighlight();
    }

    public TextualOneCell() {
        this(null, nullColors);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#protect()
     */
    public void protect() {
        protcted = true;
        setStatus(CellStatus.PROTECTED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#protectIfNotClear()
     */
    public void protectIfNotClear() {
        if (getText().length() > 0) {
            protect();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#getProtected()
     */
    public boolean getProtected() {
        return protcted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#clearProtection()
     */
    public void clearProtection() {
        protcted = false;
        setEditable(true);
        setStatus(CellStatus.USER_ENTERED);
    }

    public TextualOneCell(int max, GUIInput guiInput, Color[] colors) {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#setColors(java.awt.Color[])
     */
    public void setColors(Color[] colors) {
        this.colors = colors;
        refresh();
    }

    public TextualOneCell(int max) {

        this(max, null, nullColors);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#setMax(int)
     */
    public void setMax(int max) {
        this.max = max;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#getaText()
     */
    public String getaText() {
        return getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#getIntValue()
     */
    public int getIntValue() {
        int result = 0;

        if (getText().length() != 0) {
            try {
                result = renderer.parseInt(getText());
            } catch (Exception e) {
            }
        }

        return result;
    }

    public void setIntValue(int v) {
        if (!protcted) {
            setText(renderer.intToString(v));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#setIntValue(java.lang.String)
     */
    public void setIntValue(String v) {
        if (!protcted) {
            setText(v);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#solverSetIntValue(int)
     */
    public void solverSetIntValue(int v) {
        if (!protcted && getText().equals("")) {
            setText(renderer.intToString(v));
            setStatus(CellStatus.SOLVER_ENTERED);
        }
    }

    static interface SudokuRenderer {
        String intToString(int v);

        int parseInt(String s);
    }

    static class ClassicRenderer implements SudokuRenderer {

        public String intToString(int v) {
            return Integer.toString(v);
        }

        public int parseInt(String s) throws NumberFormatException {
            return Integer.parseInt(s);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "CLASSIC";
        }
    }

    static class HexaRenderer implements SudokuRenderer {

        public String intToString(int v) {
            return Integer.toHexString(v).toUpperCase();
        }

        public int parseInt(String s) throws NumberFormatException {
            return Integer.parseInt(s, 16);
        }

        @Override
        public String toString() {
            return "HEXA";
        }
    }

    static class OctalRenderer implements SudokuRenderer {

        public String intToString(int v) {
            return Integer.toOctalString(v).toUpperCase();
        }

        public int parseInt(String s) throws NumberFormatException {
            return Integer.parseInt(s, 8);
        }

        @Override
        public String toString() {
            return "OCTAL";
        }
    }

    static class BinaryRenderer implements SudokuRenderer {

        public String intToString(int v) {
            return Integer.toBinaryString(v);
        }

        public int parseInt(String s) throws NumberFormatException {
            return Integer.parseInt(s, 2);
        }

        @Override
        public String toString() {
            return "BINARY";
        }
    }

    static class LetterRenderer implements SudokuRenderer {

        public String intToString(int v) {
            return Character.toString((char) ('A' + v - 1));
        }

        public int parseInt(String s) throws NumberFormatException {
            char ch;

            if (s.length() == 0 || s.length() > 1)
                throw new NumberFormatException();

            ch = s.charAt(0);
            return 1 + ch - 'A';
        }

        @Override
        public String toString() {
            return "LETTER";
        }
    }

    private static SudokuRenderer renderer = new ClassicRenderer(); // To match
                                                                    // initialisaion
                                                                    // in
                                                                    // GUIInput

    public static SudokuRenderer CLASSIC_RENDERER = new ClassicRenderer();

    public static SudokuRenderer HEXA_RENDERER = new HexaRenderer();

    public static SudokuRenderer OCTAL_RENDERER = new OctalRenderer();

    public static SudokuRenderer BINARY_RENDERER = new BinaryRenderer();

    public static SudokuRenderer LETTER_RENDERER = new LetterRenderer();

    public static void setRenderer(SudokuRenderer sr) {
        renderer = sr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#clear()
     */
    public void clear() {
        unHighlight();
        if (!protcted) {
            setText("");
            setStatus(CellStatus.USER_ENTERED);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#clearComputers()
     */
    public void clearComputers() {
        unHighlight();
        if (!protcted && (status == CellStatus.SOLVER_ENTERED)) {
            setText("");
            setStatus(CellStatus.USER_ENTERED);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#highlight()
     */
    public void highlight() {
        setBackground(colors[HIGHLIGHT_COLOR_ID]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#unHighlight()
     */
    public void unHighlight() {
        setStatus(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#refresh()
     */
    public void refresh() {
        setStatus(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#setStatus(org.sat4j.apps.sudoku.CellStatus)
     */
    public void setStatus(CellStatus status) {
        this.status = status;
        switch (status) {
        case PROTECTED:
            setBackground(colors[BACKGROUND_COLOR_ID]);
            setForeground(colors[PROTECTED_COLOR_ID]);
            setToolTipText(guiInput.suDokuResources.getStringFromKey("TOOLTIP_CELL_PROTECTED"));
            break;
        case USER_ENTERED:
            setForeground(colors[USER_COLOR_ID]);
            setBackground(colors[BACKGROUND_COLOR_ID]);
            setToolTipText(guiInput.suDokuResources.getStringFromKey("TOOLTIP_CELL_NOT_PROTECTED_TEXT"));
            break;
        case SOLVER_ENTERED:
            setForeground(colors[SOLVER_COLOR_ID]);
            setBackground(colors[BACKGROUND_COLOR_ID]);
            setToolTipText(guiInput.suDokuResources.getStringFromKey("TOOLTIP_CELL_COMPUTER"));
            break;

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#getStatus()
     */
    public CellStatus getStatus() {
        return status;
    }

    public static int getNumberOfColours() {
        return UNUSED_COLOR_ID;
    }

    @Override
    protected Document createDefaultModel() {
        return new BoundedIntegerDocument();
    }

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

            str = str.toUpperCase();

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
                v = renderer.parseInt(getaText() + str);
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
        String value = ((JMenuItem) e.getSource()).getText();
        setIntValue(value);
        refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.apps.sudoku.IOneCell#updateContextualMenu()
     */
    public void updateContextualMenu() {
        popup = new JPopupMenu();
        JMenuItem menuItem;
        for (int i = 1; i <= max; i++) {
            menuItem = new JMenuItem(renderer.intToString(i));
            menuItem.addActionListener(this);
            popup.add(menuItem);
        }

    }

}
