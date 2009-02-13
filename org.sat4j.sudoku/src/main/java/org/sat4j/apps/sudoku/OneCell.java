package org.sat4j.apps.sudoku;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.border.Border;

public interface OneCell {

    public abstract void protect();

    public abstract void protectIfNotClear();

    public abstract boolean getProtected();

    public abstract void clearProtection();

    public abstract void setColors(Color[] colors);

    public abstract void setMax(int max);

    public abstract String getaText();

    public abstract int getIntValue();

    public abstract void setIntValue(String v);

    public abstract void solverSetIntValue(int v);

    public abstract void clear();

    public abstract void clearComputers();

    public abstract void highlight();

    public static final int BACKGROUND_COLOR_ID = 5;

    public static final int DISABLED_COLOR_ID = 3;

    public static final int HIGHLIGHT_COLOR_ID = 2;

    public static final int PROTECTED_COLOR_ID = 4;

    public static final int SOLVER_COLOR_ID = 1;

    public static final int UNUSED_COLOR_ID = 6;

    public static final int USER_COLOR_ID = 0;

    public abstract void unHighlight();

    public abstract void refresh();

    public abstract void setStatus(CellStatus status);

    public abstract CellStatus getStatus();

    public abstract void updateContextualMenu();

    public abstract void setIntValue(int v);

    public abstract void setCursor(Cursor predefinedCursor);

    public abstract Color getForeground();

    public abstract void setBorder(Border border);

}