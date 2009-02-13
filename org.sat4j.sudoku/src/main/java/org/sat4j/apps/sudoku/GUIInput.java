package org.sat4j.apps.sudoku;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
class GUIInput extends JTabbedPane {

    GUIInput(MainProgramWindow mainProgramWindow, SDSize sdSize, SuDoku sudoku,
            int maxSide) {
        super();
        this.mainProgramWindow = mainProgramWindow;
        this.sdSize = sdSize;
        this.sudoku = sudoku;
        this.suDokuResources = sudoku.getSuDokuResources();

        createMenuBar(mainProgramWindow.getJMenuBar());

        useXSudoku = new JCheckBox("X-SuDoku");

        blueColors = new Color[TextualOneCell.getNumberOfColours()];
        blueColors[OneCell.USER_COLOR_ID] = new Color(0, 0, 0);
        blueColors[OneCell.BACKGROUND_COLOR_ID] = new Color(230, 230, 255);
        blueColors[OneCell.SOLVER_COLOR_ID] = new Color(255, 0, 0);
        blueColors[OneCell.HIGHLIGHT_COLOR_ID] = new Color(100, 100, 255);
        // blueColors [OneCell.DISABLED_COLOR_ID] = new Color (0, 0, 150);
        blueColors[OneCell.PROTECTED_COLOR_ID] = new Color(51, 51, 255);

        whiteColors = new Color[TextualOneCell.getNumberOfColours()];
        whiteColors[OneCell.USER_COLOR_ID] = new Color(0, 0, 0);
        whiteColors[OneCell.SOLVER_COLOR_ID] = new Color(255, 0, 0);
        whiteColors[OneCell.BACKGROUND_COLOR_ID] = new Color(255, 255, 255);
        whiteColors[OneCell.HIGHLIGHT_COLOR_ID] = new Color(100, 100, 255);
        // whiteColors [OneCell.DISABLED_COLOR_ID] = new Color (0, 255, 100);
        whiteColors[OneCell.PROTECTED_COLOR_ID] = new Color(51, 51, 255);

        diagonalColors = new Color[TextualOneCell.getNumberOfColours()];
        diagonalColors[OneCell.USER_COLOR_ID] = new Color(0, 0, 0);
        diagonalColors[OneCell.SOLVER_COLOR_ID] = new Color(255, 0, 0);
        diagonalColors[OneCell.BACKGROUND_COLOR_ID] = new Color(170, 170, 170);
        diagonalColors[OneCell.HIGHLIGHT_COLOR_ID] = new Color(100, 100, 255);
        // whiteColors [OneCell.DISABLED_COLOR_ID] = new Color (0, 255, 100);
        diagonalColors[OneCell.PROTECTED_COLOR_ID] = new Color(51, 51, 255);

        cellGrid = new CellGrid(sdSize, this);

        gridAndControls = new JPanel();
        gridAndControls.setLayout(new BorderLayout());

        add(gridAndControls, "Puzzle");

        // JToolBar toolbar = new JToolBar();
        // gridAndControls.add(toolbar, BorderLayout.NORTH);
        cellGridPanel = new JPanel();
        cellGridPanel.add(cellGrid);
        gridAndControls.add(cellGridPanel, BorderLayout.CENTER);

        Box gridControls = Box.createVerticalBox();

        JPanel genpanel = new JPanel();
        genpanel.setBorder(new TitledBorder(suDokuResources
                .getStringFromKey("LABEL_GENERATION")));
        genpanel.setLayout(new BoxLayout(genpanel, BoxLayout.Y_AXIS));
        // Box hBox2 = Box.createHorizontalBox();
        JPanel up = new JPanel();
        JPanel down = new JPanel();
        ActionListener al;
        URL iconurl = this.getClass().getResource(
                "/toolbarButtonGraphics/general/New24.gif");
        create = new JButton(suDokuResources.getStringFromKey("BUTTON_CREATE"),
                new ImageIcon(iconurl));
        create.addActionListener(al = new CreateActionListener());
        // hBox2.add(create);
        up.add(create);
        JButton but = new JButton(new ImageIcon(iconurl));
        but.addActionListener(al);
        // toolbar.add(but);
        // toolbar.addSeparator();
        fillCount = new TextualOneCell(sdSize.getLargeSide()
                * sdSize.getLargeSide(), this, whiteColors);
        fillCount.clear();
        fillCount.setColumns(3);

        fillCount.setMaximumSize(new Dimension(50, 25));
        // hBox2.add(Box.createHorizontalStrut(10));
        // hBox2.add(new
        // JLabel(suDokuResources.getStringFromKey("BUTTON_FILLED_CELLS")));
        // hBox2.add(fillCount);
        down.add(new JLabel(suDokuResources
                .getStringFromKey("BUTTON_FILLED_CELLS")));
        down.add(fillCount);

        useXSudoku.addActionListener(new UseXSudokuActionListener());
        // hBox2.add(useXSudoku);
        up.add(useXSudoku);
        genpanel.add(up);
        genpanel.add(down);
        // gridControls.add(genpanel);
        // gridControls.add(Box.createVerticalGlue());

        onlyCreateUnique = new JCheckBox(suDokuResources
                .getStringFromKey("BUTTON_UNIQUE"));
        onlyCreateUnique
                .addActionListener(new OnlyCreateUniqueActionListener());
        onlyCreateUnique.setSelected(true);
        fillCount.protect();

        useExtra = new JCheckBox(suDokuResources
                .getStringFromKey("BUTTON_COMPLETE"));

        final JPanel efp1 = new JPanel();
        efp1.add(onlyCreateUnique);
        efp1.add(useExtra);

        textualrenderers = new JComboBox();
        textualrenderers.setToolTipText(suDokuResources.getStringFromKey("TOOLTIP_TEXT_RENDERERS"));
        textualrenderers.addItem(TextualOneCell.CLASSIC_RENDERER);
        textualrenderers.addItem(TextualOneCell.HEXA_RENDERER);
        textualrenderers.addItem(TextualOneCell.OCTAL_RENDERER);
        textualrenderers.addItem(TextualOneCell.BINARY_RENDERER);
        textualrenderers.addItem(TextualOneCell.LETTER_RENDERER);
        textualrenderers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cellGrid
                        .setCellRenderer((TextualOneCell.SudokuRenderer) textualrenderers
                                .getSelectedItem());
                cellGrid.updateContextualMenus();
            }
        });

        graphrenderers = new JComboBox();
        graphrenderers.setToolTipText(suDokuResources.getStringFromKey("TOOLTIP_GRAPH_RENDERERS"));
        graphrenderers.addItem("default");
        graphrenderers.addItem("bamboo");
        graphrenderers.addItem("signs");
        graphrenderers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    Method m = GraphicalOneCell.class.getMethod(graphrenderers
                            .getSelectedItem()
                            + "Order", new Class[] {});
                    m.invoke(null, new Object[] {});
                    cellGrid.updateContextualMenus();
                } catch (SecurityException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
        ButtonGroup butgroup = new ButtonGroup();
        final JRadioButton graphical = new JRadioButton("Graphical");
        graphical.setToolTipText(suDokuResources.getStringFromKey("TOOLTIP_GRAPH_BUTTON"));
        JRadioButton textual = new JRadioButton("Textual");
        textual.setToolTipText(suDokuResources.getStringFromKey("TOOLTIP_TEXT_BUTTON"));
        butgroup.add(graphical);
        butgroup.add(textual);
        ActionListener radioal = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean graphicui = graphical.isSelected();
                CellGrid.setGraphical(graphicui);
                setNewSize(rowSpinnerNumberModel.getNumber().intValue(),
                        colSpinnerNumberModel.getNumber().intValue());
                if (graphicui) {
                    efp1.remove(textualrenderers);
                    efp1.add(graphrenderers);
                } else {
                    efp1.remove(graphrenderers);
                    efp1.add(textualrenderers);
                }
                cellGrid.clearProtection();
                cellGrid.clear();
                GUIInput.this.mainProgramWindow.pack();
            }

        };
        graphical.addActionListener(radioal);
        textual.addActionListener(radioal);
        JPanel extraFeatures = new JPanel();
        extraFeatures.setLayout(new GridLayout(2, 1));
        extraFeatures.setBorder(new TitledBorder(suDokuResources
                .getStringFromKey("LABEL_UNUSUAL_FEATURES")));
        extraFeatures.add(efp1);
        JPanel efp2 = new JPanel();
        efp2.add(graphical);
        efp2.add(textual);
        extraFeatures.add(efp2);
        // gridControls.add(extraFeatures);
        genpanel.add(extraFeatures);

        JPanel sizepanel = new JPanel();
        sizepanel.setBorder(new TitledBorder(suDokuResources
                .getStringFromKey("LABEL_SET_SIZE")));
        sizepanel.setLayout(new GridLayout(2, 1));
        JPanel p1 = new JPanel();
        twoSize = new JButton("2 x 2");
        twoSize.addActionListener(new GridSizeListener(2));
        p1.add(twoSize);
        JButton twothree = new JButton("2 x 3");
        twothree.addActionListener(new GridSizeListener(2, 3));
        p1.add(twothree);

        threeSize = new JButton("3 x 3");
        threeSize.addActionListener(new GridSizeListener(3));
        p1.add(threeSize);

        sizepanel.add(p1);
        JPanel p2 = new JPanel();
        fourSize = new JButton("4 x 4");
        fourSize.addActionListener(new GridSizeListener(4));
        p2.add(fourSize);
        if (maxSide >= 25) {

            fiveSize = new JButton("5 x 5");
            fiveSize.addActionListener(new GridSizeListener(5));
            p2.add(fiveSize);
            /*
             * if (maxSide >= 36) { sixSize = new JButton("6 x 6");
             * sixSize.addActionListener(new GridSizeListener(6));
             * p2.add(sixSize); }
             */
        }

        rowSpinnerNumberModel = new SpinnerNumberModel(3, 1, 6, 1);
        rowSpinner = new JSpinner(rowSpinnerNumberModel);
        rowSpinner.setEditor(new JSpinner.DefaultEditor(rowSpinner));
        rowSpinner.addChangeListener(new SpinnerListener());
        colSpinnerNumberModel = new SpinnerNumberModel(3, 1, 6, 1);
        colSpinner = new JSpinner(colSpinnerNumberModel);
        colSpinner.addChangeListener(new SpinnerListener());
        colSpinner.setEditor(new JSpinner.DefaultEditor(colSpinner));

        p2.add(new JLabel(" "));
        p2.add(rowSpinner);
        p2.add(new JLabel("X"));
        p2.add(colSpinner);

        sizepanel.add(p2);

        genpanel.add(sizepanel);
        gridControls.add(genpanel);

        gridControls.add(Box.createVerticalGlue());

        JPanel userpanel = new JPanel();
        userpanel.setLayout(new GridLayout(3, 1));

        // Box hBox6 = Box.createHorizontalBox();
        JPanel pu1 = new JPanel();
        protect = new JButton(suDokuResources
                .getStringFromKey("BUTTON_PROTECT"));
        protect.addActionListener(new ProtectActionListener());
        // hBox6.add(protect);
        // hBox6.add (Box.createHorizontalGlue());
        // gridControls.add(hBox6);
        // gridControls.add(Box.createRigidArea(new Dimension(300, 1)));
        pu1.add(protect);
        // Box hBox = Box.createHorizontalBox();

        check = new JButton(suDokuResources.getStringFromKey("BUTTON_CHECK"));
        check.addActionListener(new CheckActionListener());
        pu1.add(check);

        solve = new JButton(suDokuResources.getStringFromKey("BUTTON_SOLVE"));
        solve.addActionListener(new SolveActionListener());
        pu1.add(solve);
        userpanel.add(pu1);

        JPanel pu2 = new JPanel();
        // Box hBox22 = Box.createHorizontalBox();
        iconurl = this.getClass().getResource(
                "/toolbarButtonGraphics/general/TipOfTheDay24.gif");
        randomCellHint = new JButton(suDokuResources
                .getStringFromKey("BUTTON_RANDOM"), new ImageIcon(iconurl));
        randomCellHint
                .addActionListener(al = new RandomCellHintActionListener());
        pu2.add(randomCellHint);
        but = new JButton(new ImageIcon(iconurl));
        but.addActionListener(al);
        // toolbar.add(but);
        iconurl = this.getClass().getResource(
                "/toolbarButtonGraphics/general/ContextualHelp24.gif");
        hintForCell = new JToggleButton(suDokuResources
                .getStringFromKey("BUTTON_CHOSEN"), new ImageIcon(iconurl));
        pu2.add(hintForCell);
        hintForCell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

        });
        userpanel.add(pu2);

        // Box hBox4 = Box.createHorizontalBox();
        JPanel pu3 = new JPanel();
        clearComputers = new JButton(suDokuResources
                .getStringFromKey("BUTTON_CLEAR_SOLVERS"));
        clearComputers.addActionListener(new ClearComputersActionListener());
        pu3.add(clearComputers);
        clear = new JButton(suDokuResources
                .getStringFromKey("BUTTON_CLEAR_MINE"));
        clear.addActionListener(new ClearActionListener());
        pu3.add(clear);
        userpanel.add(pu3);
        gridControls.add(userpanel);

        gridControls.add(Box.createVerticalGlue());

        JPanel colorpanel = new JPanel();
        colorpanel.setBorder(new TitledBorder(suDokuResources
                .getStringFromKey("LABEL_SET_COLOURS")));
        colorpanel.add(new ColorButton(OneCell.PROTECTED_COLOR_ID,
                suDokuResources.getStringFromKey("COLOURS_PROTECTED")));
        colorpanel.add(new ColorButton(OneCell.SOLVER_COLOR_ID, suDokuResources
                .getStringFromKey("COLOURS_COMPUTERS")));
        colorpanel.add(new ColorButton(OneCell.USER_COLOR_ID, suDokuResources
                .getStringFromKey("COLOURS_MINE")));
        gridControls.add(Box.createVerticalGlue());
        gridControls.add(colorpanel);

        if (!mainProgramWindow.isApplication()) {
            gridControls.add(Box.createVerticalGlue());
            puzzlePasteArea = new JTextArea(1, 5);
            Box hBox10 = Box.createHorizontalBox();
            hBox10.add(new JLabel("Paste area"));
            hBox10.add(new JScrollPane(puzzlePasteArea));
            gridControls.add(hBox10);
        }

        gridControls.add(Box.createVerticalGlue());
        result = new JTextField(20);
        // result.setMaximumSize(new Dimension(23 * 16, 25));
        result.setEditable(false);
        // gridControls.add(result);
        // gridControls.add(Box.createVerticalGlue());
        gridAndControls.add(result, BorderLayout.SOUTH);

        Box hBox9 = Box.createHorizontalBox();
        // hBox9.add(Box.createHorizontalGlue());
        hBox9.add(new ShowVersions());
        gridControls.add(hBox9);

        gridAndControls.add(gridControls, BorderLayout.EAST);

        Box cnfTab = Box.createHorizontalBox();

        Box cnfBox = Box.createVerticalBox();
        cnfBox.add(new JLabel("cnf problem"));
        cnfFile = new JTextArea(20, 20);
        cnfFile.setEditable(false);
        cnfScroll = new JScrollPane(cnfFile);
        cnfBox.add(cnfScroll);

        cnfTab.add(cnfBox);

        Box modelBox = Box.createVerticalBox();
        modelBox.add(new JLabel("solution"));
        cnfModel = new JTextArea(10, 10);
        modelScroll = new JScrollPane(cnfModel);
        modelBox.add(modelScroll);

        cnfTab.add(Box.createHorizontalStrut(4));
        cnfTab.add(modelBox);

        Box cnfControls = Box.createVerticalBox();

        Box hBox3 = Box.createHorizontalBox();
        fullCNF = new JButton("Full cnf");
        fullCNF.addActionListener(new FullCNFActionListener());
        hBox3.add(fullCNF);
        simplerCNF = new JButton("Simpler cnf");
        simplerCNF.addActionListener(new SimplerCNFActionListener());
        hBox3.add(simplerCNF);
        cnfControls.add(hBox3);

        Box hBox5 = Box.createHorizontalBox();

        showModel = new JButton("Show Model");
        showModel.addActionListener(new ShowModelActionListener());
        showModel.setEnabled(true);
        hBox5.add(showModel);
        interpretModel = new JButton("Interpret Model");
        interpretModel.addActionListener(new InterpretModelActionListener());
        hBox5.add(interpretModel);
        cnfControls.add(hBox5);

        if (mainProgramWindow.fileAccess()) {
            Box fileControls = Box.createHorizontalBox();
            saveCNF = new JButton("Save cnf to file");
            saveCNF.addActionListener(new SaveCNFActionListener());
            fileControls.add(saveCNF);
            readModel = new JButton("Read model from file");
            readModel.addActionListener(new ReadModelActionListener());
            fileControls.add(readModel);
            cnfControls.add(fileControls);
        }

        cnfTab.add(Box.createRigidArea(new Dimension(4, 400)));
        cnfTab.add(cnfControls);

        add(cnfTab, "CNF");

        setLocalToolTipText(create, "TOOLTIP_CREATE");
        setLocalToolTipText(fillCount, "TOOLTIP_FILLED_CELLS");
        setLocalToolTipText(useXSudoku, "TOOLTIP_XSUDOKU");
        setLocalToolTipText(onlyCreateUnique, "TOOLTIP_UNIQUE");
        setLocalToolTipText(useExtra, "TOOLTIP_COMPLETE");
        setLocalToolTipText(protect, "TOOLTIP_PROTECT");
        setLocalToolTipText(check, "TOOLTIP_CHECK");
        setLocalToolTipText(solve, "TOOLTIP_SOLVE");
        setLocalToolTipText(randomCellHint, "TOOLTIP_RANDOM");
        setLocalToolTipText(hintForCell, "TOOLTIP_CHOSEN");
        setLocalToolTipText(clearComputers, "TOOLTIP_CLEARSOL");
        setLocalToolTipText(clear, "TOOLTIP_CLEARMINE");
        setLocalToolTipText(twoSize, "TOOLTIP_SIZE_4_4");
        setLocalToolTipText(threeSize, "TOOLTIP_SIZE_9_9");
        setLocalToolTipText(fourSize, "TOOLTIP_SIZE_16_16");
        if (fiveSize != null)
            setLocalToolTipText(fiveSize, "TOOLTIP_SIZE_25_25");
        if (sixSize != null)
            setLocalToolTipText(sixSize, "TOOLTIP_SIZE_36_36");

        setNewSize(3, 3);
        textual.doClick();

        setVisible(true);
    }

    void createMenuBar(JMenuBar menuBar) {
        if (mainProgramWindow.fileAccess()) {
            FileCommandHandler fileCommandHandler = new FileCommandHandler(
                    sudoku);
            FileMenu fileMenu = new FileMenu(fileCommandHandler, sudoku);
            menuBar.add(fileMenu);
        }

        EditCommandHandler editCommandHandler = new EditCommandHandler(sudoku);
        EditMenu editMenu = new EditMenu(editCommandHandler, sudoku);
        menuBar.add(editMenu);

        HelpCommandHandler helpCommandHandler = new HelpCommandHandler(sudoku);
        HelpMenu helpMenu = new HelpMenu(helpCommandHandler, sudoku);
        menuBar.add(helpMenu);
    }

    void setLocalToolTipText(JComponent component, String text) {
        component.setToolTipText(suDokuResources.getStringFromKey(text));
    }

    public void puzzleChanged() {
        cnfModel.setText("");
        cnfFile.setText("");
        showModel.setEnabled(false);
    }

    public String getPuzzlePaste() {
        return puzzlePasteArea.getText();
    }

    public void setPuzzlePaste(String s) {
        puzzlePasteArea.setText(s);
    }

    public boolean getUseExtra() {
        return useExtra.isSelected();
    }

    public boolean getUseXSudoku() {
        return useXSudoku.isSelected();
    }

    public void setResult(String r) {
        result.setText(r);
    }

    public void setCNFFile(String s) {
        cnfFile.setText(s);
    }

    public String getCNFFile() {
        return cnfFile.getText();
    }

    public void setCNFModel(String s) {
        cnfModel.setText(s);
    }

    public String getCNFModel() {
        return cnfModel.getText();
    }

    public void clearCNF() {
        setCNFFile("");
        setCNFModel("");
    }

    public CellGrid getCellGrid() {
        return cellGrid;
    }

    public void setColor(int id, Color c) {
        blueColors[id] = c;
        whiteColors[id] = c;
        cellGrid.refreshCells();
    }

    Color[] blueColors, whiteColors, diagonalColors;

    static Color userColor = Color.WHITE,
            solverColor = new Color(255, 230, 230), highlightColor = Color.RED;

    SDSize sdSize;

    boolean protectionSet = false;

    CellGrid cellGrid;

    AbstractButton hintForCell;

    JButton check, solve, exit, clear, clearComputers, protect, clearAll;

    JPanel cellGridPanel;

    JButton twoSize, threeSize, fourSize, fiveSize = null, sixSize = null,
            randomCellHint;

    JSpinner rowSpinner, colSpinner;

    SpinnerNumberModel rowSpinnerNumberModel, colSpinnerNumberModel;

    JButton protectedColor;

    JButton saveCNF, readModel;

    JButton create;

    TextualOneCell fillCount;

    JSpinner.DefaultEditor fillCountEditor;

    JCheckBox onlyCreateUnique, useExtra, useXSudoku;

    JTextField result;

    JTextArea cnfFile, cnfModel, puzzlePasteArea;

    JScrollPane cnfScroll, modelScroll;

    JButton fullCNF, simplerCNF, showModel, interpretModel;

    JComponent gridAndControls;

    MainProgramWindow mainProgramWindow;

    SuDoku sudoku;

    SuDokuResources suDokuResources;

    private JComboBox textualrenderers;

    private JComboBox graphrenderers;

    @Override
    public void setSize(int width, int height) {
        mainProgramWindow.setMainWindowSize(width, height);
        super.setSize(width, height);
    }

    public void setFillCount(int count) {
        fillCount.clearProtection();
        fillCount.setText("" + count);
        if (onlyCreateUnique.isSelected()) {
            fillCount.protect();
        }
    }

    public Color[] getBlueColors() {
        return blueColors;
    }

    public Color[] getWhiteColors() {
        return whiteColors;
    }

    public Color[] getDiagonalColors() {
        return diagonalColors;
    }

    long memoryNeeded() {
        long result = 0;
        switch (sdSize.getLargeSide()) {
        case 4:
            result = 20000000;
            break;

        case 9:
            result = 50000000;
            break;

        case 16:
            result = 50000000;
            break;

        case 25:
            result = 50000000;
            break;

        }
        return result;
    }

    boolean enoughMemory() {
        long memory;
        boolean result;
        Runtime runtime = Runtime.getRuntime();
        memory = runtime.maxMemory();

        result = (memory > memoryNeeded());

        return result;
    }

    void solve(boolean showModel) {
        setResult("");
        if (enoughMemory()) {
            try {
                sudoku.graphicalSolve(cellGrid, showModel);
            } catch (Exception excpt) {
                excpt.printStackTrace();
            }
        } else {
            notEnoughMemory();
        }
    }

    void notEnoughMemory() {
        setResult("For this grid, java memory of " + memoryNeeded() + " needed");
    }

    void setProtection(boolean state) {
        if (state) {
            cellGrid.protect();
            protect.setText(suDokuResources
                    .getStringFromKey("BUTTON_UNPROTECT"));
            protect.setToolTipText(suDokuResources
                    .getStringFromKey("TOOLTIP_UNPROTECT"));
            protectionSet = true;
        } else {
            cellGrid.clearProtection();
            protect.setText(suDokuResources.getStringFromKey("BUTTON_PROTECT"));
            protect.setToolTipText(suDokuResources
                    .getStringFromKey("TOOLTIP_PROTECT"));
            protectionSet = false;
        }
    }

    public boolean getHintForCell() {
        return hintForCell.isSelected();
    }

    public void clearHintForCell() {
        hintForCell.setSelected(false);
    }

    class CheckActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            sudoku.checkSolution(cellGrid);
        }
    }

    class RandomCellHintActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            sudoku.randomCellHint(cellGrid);
        }
    }

    class CreateActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setResult("");
            clearCNF();
            if (enoughMemory()) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        sudoku.createPuzzle(fillCount.getIntValue(), cellGrid,
                                onlyCreateUnique.isSelected());
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            } else {
                notEnoughMemory();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    class SolveActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            solve(false);
        }
    }

    class UseXSudokuActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (getUseXSudoku()) {
                cellGrid.clearProtection();
                cellGrid.clear();
                cellGrid.enableXGrid(diagonalColors);

            } else {
                cellGrid.disableXgrid();
            }
        }
    }

    class SimplerCNFActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            sudoku.simplerCNF(cellGrid);
        }
    }

    class FullCNFActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            sudoku.fullCNF(cellGrid);
        }
    }

    class ShowModelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            setCNFModel("");
            solve(true);
        }
    }

    class InterpretModelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            cellGrid.clearComputers();
            sudoku.interpretModel(cellGrid);
        }
    }

    class OnlyCreateUniqueActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (onlyCreateUnique.isSelected()) {
                fillCount.clearProtection();
                fillCount.clear();
                fillCount.protect();
            } else {
                fillCount.clearProtection();
                fillCount.clear();
            }
        }
    }

    class ColorButton extends JButton {
        ColorButton(int id, String title) {
            super(title);
            setBackground(Color.WHITE);
            setForeground(whiteColors[id]);
            addActionListener(new ColorActionListener(this, id, "Set " + title
                    + " colour"));
        }
    }

    class ColorActionListener implements ActionListener {
        public ColorActionListener(ColorButton owner, int id, String title) {
            this.id = id;
            this.title = title;
            this.owner = owner;
        }

        public void actionPerformed(ActionEvent e) {
            Color newColor = JColorChooser.showDialog(GUIInput.this, title,
                    whiteColors[id]);
            if (newColor != null) {
                setColor(id, newColor);
                fillCount.refresh();
                owner.setForeground(newColor);
            }
        }

        int id;

        String title;

        ColorButton owner;
    }

    public SuDoku getSuDoku() {
        return sudoku;
    }

    public void setNewSize(int rows, int cols) {
        setResult("");
        CellGrid newCellGrid;

        sdSize.setSide(rows, cols);

        useExtra.setEnabled(rows == cols); // "Complete" SuDoku only makes
                                            // sense for square blocks

        newCellGrid = new CellGrid(sdSize, GUIInput.this);
        fillCount.clearProtection();
        fillCount.setMax(sdSize.getLargeSide() * sdSize.getLargeSide());
        fillCount.clear();
        // fillCount.setIntValue(sdSize.getLargeSide());

        cellGridPanel.remove(cellGrid);
        cellGrid = newCellGrid;
        cellGridPanel.add(cellGrid, 0);
        onlyCreateUnique.setSelected(true);
        fillCount.protect();

        if (rows > 4 && cols > 4) {
            if (!sudoku.getCreateUniqueAllowed()) {
                onlyCreateUnique.setEnabled(false);
                fillCount.clearProtection();
            }
            mainProgramWindow.maximize();
        } else {
            onlyCreateUnique.setEnabled(true);
        }

        setProtection(false);
        gridAndControls.validate();
        gridAndControls.repaint();
        mainProgramWindow.pack();
    }

    class GridSizeListener implements ActionListener {
        int rows, cols;

        GridSizeListener(int side) {
            this.rows = side;
            this.cols = side;
        }

        GridSizeListener(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }

        public void actionPerformed(ActionEvent e) {
            rowSpinner.setValue(rows);
            colSpinner.setValue(cols);
            setNewSize(rows, cols);
        }
    }

    class SpinnerListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            setNewSize(rowSpinnerNumberModel.getNumber().intValue(),
                    colSpinnerNumberModel.getNumber().intValue());
        }
    }

    class ClearActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            clearCNF();
            cellGrid.clear();
        }
    }

    class ClearComputersActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            cellGrid.clearComputers();
        }
    }

    class ProtectActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            setProtection(!protectionSet);
        }
    }

    class ClearAllActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            clearCNF();
            cellGrid.clearAll();
        }
    }

    class ExitActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setResult("");
            System.exit(1);
        }
    }

    class SaveCNFActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            mainProgramWindow.writeFile(getCNFFile());
        }
    }

    class ReadModelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setCNFModel(mainProgramWindow.readFile());
        }
    }

}
