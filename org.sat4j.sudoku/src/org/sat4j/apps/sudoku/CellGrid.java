package org.sat4j.apps.sudoku;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/*
 * The GUI component which contains the current grid of cells. Each cell is of
 * type OneCell.
 */

@SuppressWarnings("serial")
class CellGrid extends JPanel implements Printable {
    CellGrid(SDSize sdSize, GUIInput guiInput) {
        super();

        this.sdSize = sdSize;
        this.guiInput = guiInput;
        if (graphical) {
            setBackground(Color.BLACK);
        } else {
            setBackground(null);
            setBorder(BorderFactory.createTitledBorder(BorderFactory
                    .createBevelBorder(BevelBorder.RAISED), "SuDoku Puzzle"));
        }
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(1000, 1000));
        add(Box.createRigidArea(new Dimension(1, 400)));

        Box all = Box.createVerticalBox();

        all.add(Box.createRigidArea(new Dimension(400, 1)));
        cells = new OneCell[sdSize.getLargeSide()][sdSize.getLargeSide()];
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            if ((r > 0) && (r % sdSize.getSmallRows() == 0)) {
                all.add(Box.createRigidArea(new Dimension(22 * sdSize
                        .getLargeSide(), 3)));
            }
            Box row = Box.createHorizontalBox();
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                if ((c > 0) && (c % sdSize.getSmallCols() == 0)) {
                    row.add(Box.createRigidArea(new Dimension(3, 22)));
                }
                cells[r][c] = createCell(sdSize, guiInput, r, row, c);
            }

            all.add(row);
        }

        add(Box.createVerticalGlue());
        all.setMinimumSize(new Dimension(22 * sdSize.getLargeSide(),
                22 * sdSize.getLargeSide()));
        add(all);
        add(Box.createVerticalGlue());

        if (guiInput.getUseXSudoku()) {
            enableXGrid(guiInput.getDiagonalColors());
        }
    }

    private OneCell createCell(SDSize sdSize, GUIInput guiInput, int r,
            Box row, int c) {
        if (graphical) {
            GraphicalOneCell toc = new GraphicalOneCell(sdSize.getLargeSide(),
                    guiInput, cellColors(r, c));
            toc.addMouseListener(new CellClicked(r, c));
            row.add(toc);
            return toc;
        }
        TextualOneCell toc = new TextualOneCell(sdSize.getLargeSide(),
                guiInput, cellColors(r, c));
        toc.addMouseListener(new CellClicked(r, c));
        row.add(toc);
        return toc;

    }

    Color[] cellColors(int r, int c) {
        int r1 = (r) / sdSize.getSmallRows();
        int c1 = (c) / sdSize.getSmallCols();

        // if (guiInput.getUseXSudoku()
        // && ((r == c) || (r + c == sdSize.getLargeSide() - 1))) {
        // return guiInput.getDiagonalColors();
        // }
        //
        return (r1 % 2 == c1 % 2) ? guiInput.getBlueColors() : guiInput
                .getWhiteColors();
    }

    OneCell[][] cells;

    private static boolean graphical = true;

    public static void setGraphical(boolean b) {
        graphical = b;
    }

    public OneCell getCell(int r, int c) {
        return cells[r - 1][c - 1];
    }

    public int getIntValue(int r, int c) {
        return cells[r - 1][c - 1].getIntValue();
    }

    public CellStatus getStatus(int r, int c) {
        return cells[r - 1][c - 1].getStatus();
    }

    public void setIntValue(int r, int c, int v) {
        if ((1 <= r) && (r <= sdSize.getLargeSide()) && (1 <= c)
                && (c <= sdSize.getLargeSide())) {
            cells[r - 1][c - 1].setIntValue(v);
        }
    }

    public void solverSetIntValue(int r, int c, int v) {
        if ((1 <= r) && (r <= sdSize.getLargeSide()) && (1 <= c)
                && (c <= sdSize.getLargeSide())) {
            cells[r - 1][c - 1].solverSetIntValue(v);
        }
    }

    public void clear() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].clear();
            }
        }
    }

    public void refreshCells() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].refresh();
            }
        }
    }

    public void clearComputers() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].clearComputers();
            }
        }
    }

    public void protect() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].protectIfNotClear();
            }
        }
    }

    public void clearProtection() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].clearProtection();
            }
        }
    }

    public void clearAll() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].clearProtection();
                cells[r][c].clear();
            }
        }
    }

    public void unHighlightAll() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].unHighlight();
            }
        }
    }

    public void highlight(int r, int c) {
        cells[r - 1][c - 1].highlight();
    }

    public String exportString() {
        StringBuffer result = new StringBuffer();
        String separator = System.getProperty("line.separator");

        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                int v = cells[r][c].getIntValue();
                if (c != 0) {
                    result.append(" ");
                }
                if (v < 10) {
                    result.append(" ");
                }
                result.append(v);
            }
            result.append(separator);
        }
        return result.toString();

    }

    public void importString(String s) {
        ImportParser parser = new ImportParser(s);
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                int v = parser.nextValue();
                if (v != 0) {
                    cells[r][c].setIntValue(v);
                }
            }
        }
    }

    class ImportParser {
        ImportParser(String s) {
            line = s.toCharArray();
            p = 0;
        }

        public int nextValue() {
            boolean finished = false;
            int result = 0;
            if (p < line.length) {
                while ((p < line.length) && (line[p] != '.')
                        && ((line[p] < '0') || (line[p] > '9'))) {
                    p++;
                }
                if ((p < line.length) && (line[p] == '.')) {
                    p++;
                } else {
                    while ((p < line.length) && !finished && ('0' <= line[p])
                            && (line[p] <= '9')) {
                        char ch = line[p];
                        int thisValue = ch - '0';
                        result = result * 10 + thisValue;
                        p++;
                        if (p < line.length) {
                            ch = line[p];
                        }
                        if (sdSize.getLargeSide() < 10) {
                            finished = true;
                        }
                    }
                }
                while ((p < line.length) && (line[p] != '.')
                        && ((line[p] < '0') || (line[p] > '9'))) {
                    p++;
                }
            }
            return result;
        }

        char line[];

        int p;
    }

    SDSize sdSize;

    GUIInput guiInput;

    int scaleWidthToHeight(int iconWidth, ImageIcon icon) {
        return iconWidth * icon.getIconHeight() / icon.getIconWidth();
    }

    class CellClicked extends MouseAdapter {
        CellClicked(int row, int column) {
            this.row = row;
            this.column = column;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            if (guiInput.getHintForCell() && !cells[row][column].getProtected()) {
                cells[row][column].setCursor(Cursor
                        .getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseExited(MouseEvent e) {
            if (guiInput.getHintForCell()) {
                cells[row][column].setCursor(Cursor
                        .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (guiInput.getHintForCell()) {
                try {
                    guiInput.getSuDoku().graphicalSolveOneCell(CellGrid.this,
                            row + 1, column + 1);
                } catch (Exception ex) {
                }
                guiInput.clearHintForCell();
                cells[row][column].setCursor(Cursor
                        .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                guiInput.setCursor(Cursor
                        .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        int row, column;
    }

    void drawCell(Graphics2D the2DGraphics, int leftx, int topy, int cellSize,
            int r, int c) {
        the2DGraphics.drawLine(leftx + c * cellSize, topy + r * cellSize, leftx
                + (c + 1) * cellSize, topy + r * cellSize);
        the2DGraphics.drawLine(leftx + (c + 1) * cellSize, topy + r * cellSize,
                leftx + (c + 1) * cellSize, topy + (r + 1) * cellSize);
        the2DGraphics.drawLine(leftx + (c + 1) * cellSize, topy + (r + 1)
                * cellSize, leftx + c * cellSize, topy + (r + 1) * cellSize);
        the2DGraphics.drawLine(leftx + c * cellSize, topy + (r + 1) * cellSize,
                leftx + c * cellSize, topy + r * cellSize);
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex == 0) {
            int cellSize = 60;
            int leftx = (int) pageFormat.getImageableX() + 5;
            int topy = (int) pageFormat.getImageableY() + 5;
            int minSide = (int) pageFormat.getImageableWidth() - 10;

            // To improve print quality, the resolution will be increased by
            // a factor of 1.0/theScaleFactor

            double theScaleFactor = 0.5;

            //
            // Print the grid
            //

            // Check whether a cellSize of 60 is permissible and make it smaller
            // if the page is not big enough

            if (minSide > (int) pageFormat.getImageableHeight() - 10) {
                minSide = (int) pageFormat.getImageableHeight() - 10;
            }

            if (theScaleFactor * cellSize * sdSize.getLargeSide() > minSide) {
                cellSize = (int) (minSide / (theScaleFactor * sdSize
                        .getLargeSide()));
            }

            Graphics2D the2DGraphics = (Graphics2D) graphics;

            the2DGraphics.scale(theScaleFactor, theScaleFactor);
            leftx /= theScaleFactor;
            topy /= theScaleFactor;

            the2DGraphics.setColor(Color.BLACK);

            Stroke basicStroke = new BasicStroke();
            Stroke heavyStroke = new BasicStroke(3.0f);
            Stroke veryHeavyStroke = new BasicStroke(5.0f);

            for (int r = 0; r <= sdSize.getLargeSide(); r++) {
                if (r % sdSize.getSmallRows() == 0) {
                    the2DGraphics.setStroke(heavyStroke);
                } else {
                    the2DGraphics.setStroke(basicStroke);
                }

                the2DGraphics
                        .drawLine(leftx, topy + r * cellSize, leftx
                                + sdSize.getLargeSide() * cellSize, topy + r
                                * cellSize);
            }

            for (int c = 0; c <= sdSize.getLargeSide(); c++) {
                if (c % sdSize.getSmallCols() == 0) {
                    the2DGraphics.setStroke(heavyStroke);
                } else {
                    the2DGraphics.setStroke(basicStroke);
                }

                the2DGraphics.drawLine(leftx + c * cellSize, topy, leftx + c
                        * cellSize, topy + sdSize.getLargeSide() * cellSize);
            }

            the2DGraphics.setStroke(basicStroke);

            // Choose a font size which is appropriate for the size of cell
            Font theFont = the2DGraphics.getFont().deriveFont(
                    (float) (cellSize / 2.0));
            the2DGraphics.setFont(theFont);
            FontMetrics fontMetrics = the2DGraphics.getFontMetrics();
            int maxDescent = fontMetrics.getMaxDescent();

            for (int r = 0; r < sdSize.getLargeSide(); r++) {
                for (int c = 0; c < sdSize.getLargeSide(); c++) {
                    String v = cells[r][c].getaText();
                    if (!"".equals(v)) {
                        // The size of the text is needed to ensure that it is
                        // centred in the cell
                        //
                        Rectangle2D rect = theFont.getStringBounds(v,
                                the2DGraphics.getFontRenderContext());
                        the2DGraphics.setColor(cells[r][c].getForeground());
                        the2DGraphics.drawString(v,
                                (int) (leftx + c * cellSize + (cellSize - rect
                                        .getWidth()) / 2.0), (int) (topy + r
                                        * cellSize + (cellSize + rect
                                        .getHeight()) / 2.0)
                                        - maxDescent);
                    }
                }
            }

            the2DGraphics.setColor(Color.BLACK);

            if (guiInput.getUseXSudoku()) {
                the2DGraphics.setStroke(veryHeavyStroke);
                for (int r = 0; r < sdSize.getLargeSide(); r++) {
                    drawCell(the2DGraphics, leftx, topy, cellSize, r, r);
                    drawCell(the2DGraphics, leftx, topy, cellSize, r, sdSize
                            .getLargeSide()
                            - (r + 1));
                }
                the2DGraphics.setStroke(basicStroke);
            }

            //
            // Print the footer
            //

            int fontSize = 18;
            Font smallFont = theFont.deriveFont((float) fontSize);
            the2DGraphics.setFont(smallFont);
            StringBuilder footer = new StringBuilder();

            if (guiInput.getUseExtra()) {
                footer.append("Complete ");
            }

            if (guiInput.getUseXSudoku()) {
                footer.append("X-SuDoku ");
            } else {
                footer.append("SuDoku ");
            }

            int newy = topy + (sdSize.getLargeSide() + 1) * cellSize;

            String footerProperty = guiInput.getSuDoku().getSuDokuResources()
                    .getParsedProperty("printout.footer");
            if ((footerProperty == null) || (footerProperty.length() == 0)) {
                // footer.append();
                // the2DGraphics.drawString(footer.toString(), leftx, newy);
                // newy += fontSize;
                footerProperty = guiInput.getSuDoku().getSuDokuResources()
                        .getStringFromKey("DEFAULT_PRINTER_FOOTER");
            }
            String allFooters[] = footerProperty.split("\n");
            if (allFooters.length >= 1) {
                footer.append(allFooters[0]);
                the2DGraphics.drawString(footer.toString(), leftx, newy);
            }
            for (int row = 1; row < allFooters.length; row++) {
                the2DGraphics.drawString(allFooters[row], leftx, newy + row
                        * fontSize);
            }

            newy += fontSize * allFooters.length;

            //
            // Print the icons
            //

            ImageIcon sat4jIcon, ecitIcon, crilIcon, objectWebIcon, scienceFestIcon;
            SuDokuResources suDokuResources = guiInput.getSuDoku()
                    .getSuDokuResources();
            sat4jIcon = suDokuResources.getSat4jIcon();
            ecitIcon = suDokuResources.getECITIcon();
            crilIcon = suDokuResources.getCRILIcon();
            objectWebIcon = suDokuResources.getObjectWebIcon();
            scienceFestIcon = suDokuResources.getEventIcon();

            // Display all icons with the same width, find the height to
            // preserve the aspect ration

            int icony = topy;
            int iconx;
            int iconWidth;

            int iconHeight;

            if (sdSize.getLargeSide() <= 9) // icons to right of grid
            {
                iconWidth = 250;
                iconx = leftx + (sdSize.getLargeSide() + 1) * cellSize;
                iconHeight = scaleWidthToHeight(iconWidth, sat4jIcon);
                the2DGraphics.drawImage(sat4jIcon.getImage(), iconx, icony,
                        iconWidth, iconHeight, sat4jIcon.getImageObserver());
                icony += iconHeight + 20;

                iconHeight = scaleWidthToHeight(iconWidth, ecitIcon);
                the2DGraphics.drawImage(ecitIcon.getImage(), iconx, icony,
                        iconWidth, iconHeight, ecitIcon.getImageObserver());
                icony += iconHeight + 20;

                iconHeight = scaleWidthToHeight(iconWidth, crilIcon);
                the2DGraphics.drawImage(crilIcon.getImage(), iconx, icony,
                        iconWidth, iconHeight, crilIcon.getImageObserver());
                icony += iconHeight + 20;

                iconHeight = scaleWidthToHeight(iconWidth, objectWebIcon);
                the2DGraphics
                        .drawImage(objectWebIcon.getImage(), iconx, icony,
                                iconWidth, iconHeight, objectWebIcon
                                        .getImageObserver());
                icony += iconHeight + 20;

                iconHeight = scaleWidthToHeight(iconWidth, scienceFestIcon);
                the2DGraphics.drawImage(scienceFestIcon.getImage(), iconx + 10,
                        icony, iconWidth, iconHeight, scienceFestIcon
                                .getImageObserver());

            } else // smaller icons below grid and footer
            {
                iconx = leftx;
                icony = newy + 20;
                iconWidth = 200;
                iconx = leftx;
                iconHeight = scaleWidthToHeight(iconWidth, sat4jIcon);
                the2DGraphics.drawImage(sat4jIcon.getImage(), iconx, icony,
                        iconWidth, iconHeight, sat4jIcon.getImageObserver());
                iconx += iconWidth + 20;

                iconHeight = scaleWidthToHeight(iconWidth, ecitIcon);
                the2DGraphics.drawImage(ecitIcon.getImage(), iconx, icony,
                        iconWidth, iconHeight, ecitIcon.getImageObserver());
                iconx += iconWidth + 20;

                iconHeight = scaleWidthToHeight(iconWidth, crilIcon);
                the2DGraphics.drawImage(crilIcon.getImage(), iconx, icony,
                        iconWidth, iconHeight, crilIcon.getImageObserver());
                iconx += iconWidth + 20;

                iconHeight = scaleWidthToHeight(iconWidth, objectWebIcon);
                the2DGraphics
                        .drawImage(objectWebIcon.getImage(), iconx, icony,
                                iconWidth, iconHeight, objectWebIcon
                                        .getImageObserver());
                iconx += iconWidth + 15;

                iconHeight = scaleWidthToHeight(iconWidth, scienceFestIcon);
                the2DGraphics.drawImage(scienceFestIcon.getImage(), iconx,
                        icony - 75, iconWidth, iconHeight, scienceFestIcon
                                .getImageObserver());
            }

            return PAGE_EXISTS;
        }
        return NO_SUCH_PAGE;
    }

    public void setCellRenderer(TextualOneCell.SudokuRenderer sr) {
        TextualOneCell.setRenderer(sr);
    }

    public void updateContextualMenus() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            for (int c = 0; c < sdSize.getLargeSide(); c++) {
                cells[r][c].updateContextualMenu();
            }
        }
    }

    public void enableXGrid(Color[] diagonalColors) {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            if (graphical) {
                cells[r][r].setBorder(BorderFactory.createLineBorder(
                        Color.BLACK, 1));
                cells[r][sdSize.getLargeSide() - r - 1].setBorder(BorderFactory
                        .createLineBorder(Color.BLACK, 1));
            } else {
                cells[r][r].setColors(diagonalColors);
                cells[r][sdSize.getLargeSide() - r - 1]
                        .setColors(diagonalColors);
            }
        }

    }

    public void disableXgrid() {
        for (int r = 0; r < sdSize.getLargeSide(); r++) {
            if (graphical) {
                cells[r][r].setBorder(null);
                cells[r][sdSize.getLargeSide() - r - 1].setBorder(null);
            } else {
                cells[r][r].setColors(cellColors(r, r));
                cells[r][sdSize.getLargeSide() - r - 1].setColors(cellColors(r,
                        sdSize.getLargeSide() - r - 1));
            }
        }

    }
}
