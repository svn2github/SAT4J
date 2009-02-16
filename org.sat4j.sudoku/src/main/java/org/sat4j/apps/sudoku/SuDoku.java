package org.sat4j.apps.sudoku;

import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

public class SuDoku {

    SDSize sdSize;

    int clauses;

    GUIInput gui;

    boolean fullCNF, createUniqueAllowed;

    SuDokuResources suDokuResources;

    MainProgramWindow mainProgramWindow;

    VecInt vi = new VecInt(), vi1 = new VecInt(), vi2 = new VecInt();

    ISolver solver;

    Random randomCellChooser;

    int variable(int row, int column, int v) {
        return row * sdSize.getBase() * sdSize.getBase() + column
                * sdSize.getBase() + v;
    }

    public MainProgramWindow getMainProgramWindow() {
        return mainProgramWindow;
    }

    public SuDokuResources getSuDokuResources() {
        return suDokuResources;
    }

    void addOneSquareSolver(int row, int column, ClauseHandler ch) {

        if (fullCNF) {
            vi.clear();
            for (int v = 1; v <= sdSize.getLargeSide(); v++) {
                vi.push(variable(row, column, v));
            }
            ch.addClause(vi);
        }

        for (int v1 = 1; v1 <= sdSize.getLargeSide() - 1; v1++) {
            for (int v2 = v1 + 1; v2 <= sdSize.getLargeSide(); v2++) {
                vi.clear();
                vi.push(-variable(row, column, v1));
                vi.push(-variable(row, column, v2));
                ch.addClause(vi);
            }
        }
    }

    int numOfVariables() {
        int b = sdSize.getBase() + 1;

        switch (sdSize.getLargeSide()) {
        case 4:
            return 444;
        case 9:
            return 999;
        default:
            return b * b * b;
        }
    }

    void readPuzzleSolver(CellGrid cellGrid, ClauseHandler ch) {

        for (int r = 1; r <= sdSize.getLargeSide(); r++) {
            for (int c = 1; c <= sdSize.getLargeSide(); c++) {
                if (cellGrid.getStatus(r, c) != CellStatus.SOLVER_ENTERED) {
                    int v = cellGrid.getIntValue(r, c);
                    if (v > 0) {
                        setSquareSolver(r, c, v, ch);
                    }
                }
            }
        }

    }

    void addOneSquaresSolver(ClauseHandler ch) {
        for (int r = 1; r <= sdSize.getLargeSide(); r++) {
            for (int c = 1; c <= sdSize.getLargeSide(); c++) {
                addOneSquareSolver(r, c, ch);
            }
        }
    }

    int blockVariable(int br, int bc, int r, int c, int v) {
        return variable((br - 1) * sdSize.getSmallRows() + r, (bc - 1)
                * sdSize.getSmallCols() + c, v);
    }

    int blockVariable1(int br, int bv, int r, int c, int v) {
        return variable((br - 1) * sdSize.getSmallRows() + r, c, (bv - 1)
                * sdSize.getSmallRows() + v);
    }

    int blockVariable2(int bv, int bc, int r, int c, int v) {
        return variable(r, (bc - 1) * sdSize.getSmallCols() + c, (bv - 1)
                * sdSize.getSmallRows() + v);
    }

    void addOneBlockSolver(int br, int bc, ClauseHandler ch) {
        for (int v = 1; v <= sdSize.getLargeSide(); v++) {
            vi.clear();
            if (gui.getUseExtra()) {
                vi1.clear();
                vi2.clear();
            }
            for (int r = 1; r <= sdSize.getSmallRows(); r++) {
                for (int c = 1; c <= sdSize.getSmallCols(); c++) {
                    vi.push(blockVariable(br, bc, r, c, v));
                    if (gui.getUseExtra()) {
                        vi1.push(blockVariable1(br, bc, r, v, c));
                        vi2.push(blockVariable2(br, bc, v, c, r));
                    }
                }
            }
            ch.addClause(vi);
            if (gui.getUseExtra()) {
                ch.addClause(vi1);
                ch.addClause(vi2);
            }

            if (fullCNF) {
                for (int i1 = 1; i1 <= sdSize.getLargeSide() - 1; i1++) {
                    for (int i2 = i1 + 1; i2 <= sdSize.getLargeSide(); i2++) {
                        int r1 = (i1 - 1) / sdSize.getSmallCols() + 1;
                        int c1 = (i1 - 1) % sdSize.getSmallCols() + 1;
                        int r2 = (i2 - 1) / sdSize.getSmallCols() + 1;
                        int c2 = (i2 - 1) % sdSize.getSmallCols() + 1;

                        vi.clear();
                        vi.push(-blockVariable(br, bc, r1, c1, v));
                        vi.push(-blockVariable(br, bc, r2, c2, v));
                        ch.addClause(vi);

                        if (gui.getUseExtra()) {
                            vi1.clear();
                            vi1.push(-blockVariable1(br, bc, r1, v, c1));
                            vi1.push(-blockVariable1(br, bc, r2, v, c2));
                            ch.addClause(vi1);

                            vi2.clear();
                            vi2.push(-blockVariable2(br, bc, v, c1, r1));
                            vi2.push(-blockVariable2(br, bc, v, c2, r2));
                            ch.addClause(vi2);
                        }
                    }
                }
            }
        }
    }

    void addOneRowSolver(int row, ClauseHandler ch) {
        for (int v = 1; v <= sdSize.getLargeSide(); v++) {
            vi.clear();
            for (int c = 1; c <= sdSize.getLargeSide(); c++) {
                vi.push(variable(row, c, v));
            }
            ch.addClause(vi);

            if (fullCNF) {
                for (int c1 = 1; c1 <= sdSize.getLargeSide() - 1; c1++) {
                    for (int c2 = c1 + 1; c2 <= sdSize.getLargeSide(); c2++) {
                        vi.clear();
                        vi.push(-variable(row, c1, v));
                        vi.push(-variable(row, c2, v));
                        ch.addClause(vi);
                    }
                }
            }
        }
    }

    void addOneRowsSolver(ClauseHandler ch) {
        for (int r = 1; r <= sdSize.getLargeSide(); r++) {
            addOneRowSolver(r, ch);
        }
    }

    void addOneColumnSolver(int column, ClauseHandler ch) {
        for (int v = 1; v <= sdSize.getLargeSide(); v++) {
            vi.clear();
            for (int r = 1; r <= sdSize.getLargeSide(); r++) {
                vi.push(variable(r, column, v));
            }
            ch.addClause(vi);

            if (fullCNF) {
                for (int r1 = 1; r1 <= sdSize.getLargeSide() - 1; r1++) {
                    for (int r2 = r1 + 1; r2 <= sdSize.getLargeSide(); r2++) {
                        vi.clear();
                        vi.push(-variable(r1, column, v));
                        vi.push(-variable(r2, column, v));
                        ch.addClause(vi);
                    }
                }
            }
        }
    }

    void addFallingDiagonalSolver(ClauseHandler ch) {
        for (int v = 1; v <= sdSize.getLargeSide(); v++) {
            vi.clear();
            for (int r = 1; r <= sdSize.getLargeSide(); r++) {
                vi.push(variable(r, r, v));
            }
            ch.addClause(vi);

            if (fullCNF) {
                for (int r1 = 1; r1 <= sdSize.getLargeSide() - 1; r1++) {
                    for (int r2 = r1 + 1; r2 <= sdSize.getLargeSide(); r2++) {
                        vi.clear();
                        vi.push(-variable(r1, r1, v));
                        vi.push(-variable(r2, r2, v));
                        ch.addClause(vi);
                    }
                }
            }
        }
    }

    void addRisingDiagonalSolver(ClauseHandler ch) {
        for (int v = 1; v <= sdSize.getLargeSide(); v++) {
            vi.clear();
            for (int r = 1; r <= sdSize.getLargeSide(); r++) {
                vi.push(variable(r, sdSize.getLargeSide() + 1 - r, v));
            }
            ch.addClause(vi);

            if (fullCNF) {
                for (int r1 = 1; r1 <= sdSize.getLargeSide() - 1; r1++) {
                    for (int r2 = r1 + 1; r2 <= sdSize.getLargeSide(); r2++) {
                        vi.clear();
                        vi
                                .push(-variable(r1, sdSize.getLargeSide() + 1
                                        - r1, v));
                        vi
                                .push(-variable(r2, sdSize.getLargeSide() + 1
                                        - r2, v));
                        ch.addClause(vi);
                    }
                }
            }
        }
    }

    void addOneColumnsSolver(ClauseHandler ch) {
        for (int c = 1; c <= sdSize.getLargeSide(); c++) {
            addOneColumnSolver(c, ch);
        }
    }

    void addOneBlocksSolver(ClauseHandler ch) {
        for (int r = 1; r <= sdSize.getSmallCols(); r++) {
            for (int c = 1; c <= sdSize.getSmallRows(); c++) {
                addOneBlockSolver(r, c, ch);
            }
        }
    }

    void setSquareSolver(int r, int c, int v, ClauseHandler ch) {
        vi.clear();
        vi.push(variable(r, c, v));
        ch.addClause(vi);
    }

    void addSuDokuRules(ClauseHandler ch) {
        addOneSquaresSolver(ch);
        addOneBlocksSolver(ch);
        addOneRowsSolver(ch);
        addOneColumnsSolver(ch);

        if (gui.getUseXSudoku()) {
            addFallingDiagonalSolver(ch);
            addRisingDiagonalSolver(ch);
        }

    }

    public SuDoku(MainProgramWindow mainProgramWindow, int maxSide,
            boolean createUniqueAllowed) {
        this.mainProgramWindow = mainProgramWindow;
        this.createUniqueAllowed = createUniqueAllowed;
        suDokuResources = new SuDokuResources();
        sdSize = new SDSize();
        gui = new GUIInput(mainProgramWindow, sdSize, this, maxSide);
        solver = SolverFactory.newDefault();
        randomCellChooser = new Random();
    }

    public GUIInput getGui() {
        return gui;
    }

    public boolean getCreateUniqueAllowed() {
        return createUniqueAllowed;
    }

    boolean passesCheck, completed;

    void checkSquares(CellGrid cellGrid) {
        for (int r = 1; (r <= sdSize.getLargeSide()) && passesCheck; r++) {
            for (int c = 1; (c <= sdSize.getLargeSide()) && passesCheck; c++) {
                int v = cellGrid.getIntValue(r, c);

                if ((v < 1) || (v > sdSize.getLargeSide())) {
                    completed = false;
                    // cellGrid.highlight(r, c);
                    // gui.setResult("Invalid cell");
                }
            }
        }
    }

    void checkBlock(CellGrid cellGrid, int rowVal[][], int colVal[][], int r,
            int c) {

        for (int i1 = 1; (i1 <= sdSize.getLargeSide() - 1) && passesCheck; i1++) {
            for (int i2 = i1 + 1; (i2 <= sdSize.getLargeSide()) && passesCheck; i2++) {
                int r1 = (i1 - 1) / sdSize.getSmallCols() + 1;
                int c1 = (i1 - 1) % sdSize.getSmallCols() + 1;
                int r2 = (i2 - 1) / sdSize.getSmallCols() + 1;
                int c2 = (i2 - 1) % sdSize.getSmallCols() + 1;

                int r3, c3, r4, c4;

                r3 = r * sdSize.getSmallRows() + r1;
                c3 = c * sdSize.getSmallCols() + c1;

                r4 = r * sdSize.getSmallRows() + r2;
                c4 = c * sdSize.getSmallCols() + c2;

                if ((cellGrid.getIntValue(r3, c3) != 0)
                        && (cellGrid.getIntValue(r3, c3) == cellGrid
                                .getIntValue(r4, c4))) {
                    cellGrid.highlight(r3, c3);
                    cellGrid.highlight(r4, c4);
                    passesCheck = false;
                    gui.setResult(suDokuResources
                            .getStringFromKey("MSG_SAME_IN_BLOCK"));
                    break;
                }

                if (gui.getUseExtra()) {
                    if ((rowVal[r3 - 1][c3 - 1] != 0)
                            && (rowVal[r3 - 1][c3 - 1] == rowVal[r4 - 1][c4 - 1])) {
                        cellGrid.highlight(r3, rowVal[r3 - 1][c3 - 1]);
                        cellGrid.highlight(r4, rowVal[r4 - 1][c4 - 1]);
                        passesCheck = false;
                        gui.setResult(suDokuResources
                                .getStringFromKey("MSG_RELATED_IN_COLUMN"));
                        break;
                    }

                    if ((colVal[r3 - 1][c3 - 1] != 0)
                            && (colVal[r3 - 1][c3 - 1] == colVal[r4 - 1][c4 - 1])) {
                        cellGrid.highlight(colVal[r3 - 1][c3 - 1], c3);
                        cellGrid.highlight(colVal[r4 - 1][c4 - 1], c4);
                        passesCheck = false;
                        gui.setResult(suDokuResources
                                .getStringFromKey("MSG_RELATED_IN_ROW"));
                        break;
                    }
                }

            }
        }

    }

    void checkBlocks(CellGrid cellGrid) {
        int rowVal[][] = null, colVal[][] = null;

        if (gui.getUseExtra()) {
            rowVal = new int[sdSize.getLargeSide()][sdSize.getLargeSide()];
            colVal = new int[sdSize.getLargeSide()][sdSize.getLargeSide()];
            for (int i = 0; i < sdSize.getLargeSide(); i++) {
                for (int j = 0; j < sdSize.getLargeSide(); j++) {
                    rowVal[i][j] = 0;
                    colVal[i][j] = 0;
                }
            }
            for (int i = 0; i < sdSize.getLargeSide(); i++) {
                for (int j = 0; j < sdSize.getLargeSide(); j++) {
                    int v1;
                    v1 = cellGrid.getIntValue(i + 1, j + 1);
                    if (v1 > 0) {
                        rowVal[i][v1 - 1] = j + 1;
                        colVal[v1 - 1][j] = i + 1;
                    }
                }
            }

        }
        for (int r = 0; (r < sdSize.getSmallCols()) && passesCheck; r++) {
            for (int c = 0; (c < sdSize.getSmallRows()) && passesCheck; c++) {
                checkBlock(cellGrid, rowVal, colVal, r, c);
            }
        }
    }

    void checkRow(CellGrid cellGrid, int r) {
        int c1, c2;

        for (c1 = 1; (c1 < sdSize.getLargeSide()) && passesCheck; c1++) {
            for (c2 = c1 + 1; (c2 <= sdSize.getLargeSide()) && passesCheck; c2++) {
                if ((cellGrid.getIntValue(r, c1) != 0)
                        && (cellGrid.getIntValue(r, c1) == cellGrid
                                .getIntValue(r, c2))) {
                    cellGrid.highlight(r, c1);
                    cellGrid.highlight(r, c2);
                    passesCheck = false;
                    gui.setResult(suDokuResources
                            .getStringFromKey("MSG_SAME_IN_ROW"));
                    break;
                }
            }
        }
    }

    void checkRows(CellGrid cellGrid) {
        for (int r = 1; (r <= sdSize.getLargeSide()) && passesCheck; r++) {
            checkRow(cellGrid, r);
        }
    }

    void checkColumn(CellGrid cellGrid, int c) {
        int r1, r2;

        for (r1 = 1; (r1 < sdSize.getLargeSide()) && passesCheck; r1++) {
            for (r2 = r1 + 1; (r2 <= sdSize.getLargeSide()) && passesCheck; r2++) {
                if ((cellGrid.getIntValue(r1, c) != 0)
                        && (cellGrid.getIntValue(r1, c) == cellGrid
                                .getIntValue(r2, c))) {
                    cellGrid.highlight(r1, c);
                    cellGrid.highlight(r2, c);
                    passesCheck = false;
                    gui.setResult(suDokuResources
                            .getStringFromKey("MSG_SAME_IN_COLUMN"));
                    break;
                }
            }
        }
    }

    void checkFallingDiagonal(CellGrid cellGrid) {
        int r1, r2;

        for (r1 = 1; (r1 < sdSize.getLargeSide()) && passesCheck; r1++) {
            for (r2 = r1 + 1; (r2 <= sdSize.getLargeSide()) && passesCheck; r2++) {
                if ((cellGrid.getIntValue(r1, r1) != 0)
                        && (cellGrid.getIntValue(r1, r1) == cellGrid
                                .getIntValue(r2, r2))) {
                    cellGrid.highlight(r1, r1);
                    cellGrid.highlight(r2, r2);
                    passesCheck = false;
                    gui.setResult(suDokuResources
                            .getStringFromKey("MSG_SAME_IN_FALLING_DIAGONAL"));
                    break;
                }
            }
        }
    }

    void checkRisingDiagonal(CellGrid cellGrid) {
        int r1, r2;

        for (r1 = 1; (r1 < sdSize.getLargeSide()) && passesCheck; r1++) {
            for (r2 = r1 + 1; (r2 <= sdSize.getLargeSide()) && passesCheck; r2++) {
                if ((cellGrid.getIntValue(r1, sdSize.getLargeSide() + 1 - r1) != 0)
                        && (cellGrid.getIntValue(r1, sdSize.getLargeSide() + 1
                                - r1) == cellGrid.getIntValue(r2, sdSize
                                .getLargeSide()
                                + 1 - r2))) {
                    cellGrid.highlight(r1, sdSize.getLargeSide() + 1 - r1);
                    cellGrid.highlight(r2, sdSize.getLargeSide() + 1 - r2);
                    passesCheck = false;
                    gui.setResult(suDokuResources
                            .getStringFromKey("MSG_SAME_IN_RISING_DIAGONAL"));
                    break;
                }
            }
        }
    }

    void checkColumns(CellGrid cellGrid) {
        for (int c = 1; (c <= sdSize.getLargeSide()) && passesCheck; c++) {
            checkColumn(cellGrid, c);
        }
    }

    public void checkSolution(CellGrid cellGrid) {
        passesCheck = true;
        completed = true;
        cellGrid.unHighlightAll();
        checkSquares(cellGrid);
        if (passesCheck) {
            checkBlocks(cellGrid);
        }
        if (passesCheck) {
            checkRows(cellGrid);
        }
        if (passesCheck) {
            checkColumns(cellGrid);
        }

        if (passesCheck && gui.getUseXSudoku()) {
            checkFallingDiagonal(cellGrid);
            checkRisingDiagonal(cellGrid);
        }

        if (passesCheck) {
            if (completed) {
                gui.setResult(suDokuResources
                        .getStringFromKey("MSG_CORRECT_SUDOKU")); // "Correct");
                JOptionPane.showMessageDialog(null, suDokuResources
                        .getStringFromKey("MSG_CORRECT_SUDOKU"));
            } else {
                gui.setResult(suDokuResources
                        .getStringFromKey("MSG_NO_BROKEN_RULES"));// "No rules
                JOptionPane.showMessageDialog(null, suDokuResources
                        .getStringFromKey("MSG_NO_BROKEN_RULES")); // broken so
                // far (may
                // not lead
                // to final
                // solution)");
            }
        }
    }

    public void graphicalSolveOneCell(CellGrid cellGrid, int row, int column)
            throws Exception {

        boolean showModel = false;
        System.gc();
        fullCNF = true;
        StringBuffer model = new StringBuffer("s SATISFIABLE\n");
        clauses = 0;

        solver = SolverFactory.newDefault();

        Sat4jClauseHandler ch = new Sat4jClauseHandler(numOfVariables(), solver);

        addSuDokuRules(ch);

        readPuzzleSolver(cellGrid, ch);

        ch.finish();

        gui.setResult("");

        try {
            if (solver.isSatisfiable()) {
                int[] m = solver.model();
                for (int i = 0; i < m.length; i++) {
                    if (showModel) {
                        model.append("v " + m[i] + "\n");
                    }

                    if ((m[i] > 0) && (m[i] % sdSize.getBase() != 0)) {
                        if ((m[i] / sdSize.getBase2() == row)
                                && ((m[i] % sdSize.getBase2())
                                        / sdSize.getBase() == column)) {
                            gui.getCellGrid().solverSetIntValue(
                                    m[i] / sdSize.getBase2(),
                                    (m[i] % sdSize.getBase2())
                                            / sdSize.getBase(),
                                    m[i] % sdSize.getBase());
                        }
                    }
                }
                if (showModel) {
                    model.append("v 0\n");
                    gui.setCNFModel(model.toString());
                }
            } else {
                gui.setResult("Cannot be solved");
                if (showModel) {
                    gui.setCNFModel("s UNSATISFIABLE");
                }
            }
        } catch (OutOfMemoryError e) {
            System.gc();
            gui.setResult("Increased Java Heap size required");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void randomCellHint(CellGrid cellGrid) {
        try {
            int freeCells = sdSize.getLargeSide() * sdSize.getLargeSide();
            for (int r = 1; r <= sdSize.getLargeSide(); r++) {
                for (int c = 1; c <= sdSize.getLargeSide(); c++) {
                    if (cellGrid.getIntValue(r, c) != 0) {
                        freeCells--;
                    }
                }
            }

            int cell = randomCellChooser.nextInt(freeCells);
            // System.out.println ("cell " + cell + ", freeCells " + freeCells);
            int thisCell = -1;
            int thisRow = 1, thisColumn = 1;
            for (int r = 1; (r <= sdSize.getLargeSide()) && (thisCell < cell); r++) {
                for (int c = 1; (c <= sdSize.getLargeSide())
                        && (thisCell < cell); c++) {
                    if (cellGrid.getIntValue(r, c) == 0) {
                        thisCell++;
                        if (thisCell == cell) {
                            thisRow = r;
                            thisColumn = c;
                        }
                    }
                }
            }

            graphicalSolveOneCell(cellGrid, thisRow, thisColumn);
        } catch (Exception e) {
        }
    }

    public void graphicalSolve(CellGrid cellGrid, boolean showModel)
            throws Exception {
        System.gc();
        fullCNF = true;
        StringBuffer model = new StringBuffer("s SATISFIABLE\n");
        clauses = 0;

        solver = SolverFactory.newDefault();

        Sat4jClauseHandler ch = new Sat4jClauseHandler(numOfVariables(), solver);

        addSuDokuRules(ch);

        readPuzzleSolver(cellGrid, ch);

        ch.finish();

        gui.setResult("");

        try {
            if (solver.isSatisfiable()) {
                int[] m = solver.model();
                for (int i = 0; i < m.length; i++) {
                    if (showModel) {
                        model.append("v " + m[i] + "\n");
                    }

                    if ((m[i] > 0) && (m[i] % sdSize.getBase() != 0)) {
                        gui.getCellGrid().solverSetIntValue(
                                m[i] / sdSize.getBase2(),
                                (m[i] % sdSize.getBase2()) / sdSize.getBase(),
                                m[i] % sdSize.getBase());
                    }
                }
                if (showModel) {
                    model.append("v 0\n");
                    gui.setCNFModel(model.toString());
                }
            } else {
                gui.setResult("Cannot be solved");
                if (showModel) {
                    gui.setCNFModel("s UNSATISFIABLE");
                }
            }
        } catch (OutOfMemoryError e) {
            System.gc();
            gui.setResult("Increased Java Heap size required");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void simplerCNF(CellGrid cellGrid) {
        System.gc();
        fullCNF = false;

        StringClauseHandler ch = new StringClauseHandler(numOfVariables());

        addSuDokuRules(ch);

        readPuzzleSolver(cellGrid, ch);

        ch.finish();

        gui.setCNFFile(ch.toString());

        gui.setResult("cnf available for solving");

    }

    public void fullCNF(CellGrid cellGrid) {
        System.gc();
        fullCNF = true;

        StringClauseHandler ch = new StringClauseHandler(numOfVariables());

        addSuDokuRules(ch);

        readPuzzleSolver(cellGrid, ch);

        ch.finish();

        gui.setCNFFile(ch.toString());

        gui.setResult("cnf available for solving");

    }

    public void parseLine(String line, CellGrid cellGrid) {
        if ((line.length() > 0) && (line.charAt(0) == 'v')) {
            WordScanner wordScanner = new WordScanner(line);
            wordScanner.next();
            int v1;
            while (wordScanner.hasNext()) {
                String w = wordScanner.next();
                try {
                    v1 = Integer.parseInt(w);

                    if ((v1 > 0) && (v1 % sdSize.getBase() > 0)) {
                        cellGrid.solverSetIntValue(v1 / sdSize.getBase2(),
                                (v1 % sdSize.getBase2()) / sdSize.getBase(), v1
                                        % sdSize.getBase());
                    }
                } catch (Exception e) {
                }
            }
            wordScanner.close();
        }
    }

    public void interpretModel(CellGrid cellGrid) {
        LineScanner lineScanner = new LineScanner(gui.getCNFModel());
        while (lineScanner.hasNext()) {
            parseLine(lineScanner.next(), cellGrid);
        }
        lineScanner.close();

    }

    public void createPuzzle(int filledCells, CellGrid cellGrid,
            boolean onlyCreateUnique) {
        System.gc();

        int v[][], testv[][];
        v = new int[sdSize.getLargeSide() + 1][sdSize.getLargeSide() + 1];
        testv = new int[sdSize.getLargeSide() + 1][sdSize.getLargeSide() + 1];

        cellGrid.clearAll();

        boolean success = false;

        fullCNF = true;

        ISolver csolver;
        ProgressMonitor progressbar = new ProgressMonitor(null, suDokuResources
                .getStringFromKey("MSG_PROGRESS_DIALOG"), "", 0, sdSize
                .getLargeSide()
                * sdSize.getLargeSide() / 2);
        progressbar.setProgress(0);
        while (!success) {
            csolver = SolverFactory.newDefault();
            Sat4jClauseHandler ch = new Sat4jClauseHandler(numOfVariables(),
                    csolver);

            addSuDokuRules(ch);

            CoordinateSet cs1 = new CoordinateSet(sdSize.getLargeSide(), sdSize
                    .getLargeSide());

            int toAdd = sdSize.getLargeSide();

            for (int v1 = 1; v1 <= toAdd; v1++) {
                Coordinate coord;
                coord = cs1.getCoordinate();
                vi.clear();
                vi.push(variable(coord.getRow(), coord.getColumn(), v1));
                ch.addClause(vi);
            }

            ch.finish();

            try {
                if (csolver.isSatisfiable()) {
                    // System.out.println("Sudoku generated!");
                    success = true;
                    int[] m = csolver.model();
                    for (int i = 0; i < m.length; i++) {
                        if ((m[i] > 0) && (m[i] % sdSize.getBase() != 0)) {
                            v[m[i] / sdSize.getBase2()][(m[i] % sdSize
                                    .getBase2())
                                    / sdSize.getBase()] = m[i]
                                    % sdSize.getBase();
                        }
                    }
                } else {
                }
            } catch (OutOfMemoryError e) {
                gui.setResult("Increased Java Heap size required");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        CoordinateSet cs = new CoordinateSet(sdSize.getLargeSide(), sdSize
                .getLargeSide());

        RandomPermutation rp;

        if (gui.getUseExtra()) {
            rp = new RandomPermutation(sdSize.getSmallRows(), sdSize
                    .getSmallCols());
        } else {
            rp = new RandomPermutation(sdSize.getLargeSide());
        }

        for (int r = 1; r <= sdSize.getLargeSide(); r++) {
            for (int c = 1; c <= sdSize.getLargeSide(); c++) {
                v[r][c] = rp.permute(v[r][c]);
            }
        }

        int requiredCells = filledCells;

        if (onlyCreateUnique) {
            requiredCells = 0;

            try {

                ISolver solver = SolverFactory.newBackjumping();
                // SingleSolutionDetector problem = new
                // SingleSolutionDetector(SolverFactory.newMiniSAT());
                Sat4jClauseHandler chp = new Sat4jClauseHandler(
                        numOfVariables(), solver);

                addSuDokuRules(chp);

                vi.clear();
                for (int r = 1; r <= sdSize.getLargeSide(); r++) {
                    for (int c = 1; c <= sdSize.getLargeSide(); c++) {
                        vi.push(-variable(r, c, v[r][c]));
                    }
                }
                chp.addClause(vi);

                boolean finished = false;

                SortedCoordinateSet s = new SortedCoordinateSet(sdSize
                        .getLargeSide(), sdSize.getLargeSide(), sdSize
                        .getSmallRows(), sdSize.getSmallCols(), v);
                int addedCount = 0;
                while (!finished) {
                    if (solver.isSatisfiable()) {
                        if (progressbar.isCanceled()) {
                            break;
                        }
                        // System.out.println("Simplifying Sudoku "+addedCount);
                        progressbar.setProgress(addedCount + 1);
                        int[] m = solver.model();
                        for (int i = 0; i < m.length; i++) {
                            if (m[i] > 0) {
                                int r = m[i] / sdSize.getBase2();
                                int c = (m[i] % sdSize.getBase2())
                                        / sdSize.getBase();
                                int val = m[i] % sdSize.getBase();
                                testv[r][c] = val;
                            }
                        }

                        boolean differenceFound = false;
                        Coordinate[] ccs = s.getAll();
                        for (int i = 0; !differenceFound; i++) {
                            Coordinate cc = ccs[i];
                            int r = cc.getRow();
                            int c = cc.getColumn();
                            if (v[r][c] != testv[r][c]) {
                                s.note(cc);
                                cellGrid.setIntValue(r, c, v[r][c]);
                                requiredCells++;
                                differenceFound = true;
                                addedCount++;
                                vi.clear();
                                vi.push(variable(r, c, v[r][c]));
                                try {
                                    chp.addClauseUnTrapped(vi);
                                } catch (ContradictionException ce) {
                                    finished = true;
                                }
                            }
                        }
                    } else {
                        finished = true;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            for (int i = 0; i < requiredCells / 4; i++) {
                Coordinate coords[] = cs.getGroup();
                for (int j = 0; j < 4; j++) {
                    cellGrid.setIntValue(coords[j].getRow(), coords[j]
                            .getColumn(), v[coords[j].getRow()][coords[j]
                            .getColumn()]);
                }
            }
            for (int j = 0; j < requiredCells % 4; j++) {
                Coordinate c = cs.getCoordinate();
                cellGrid.setIntValue(c.getRow(), c.getColumn(), v[c.getRow()][c
                        .getColumn()]);
            }

        }
        if (progressbar.isCanceled()) {
            cellGrid.clearAll();
        } else {
            gui.setFillCount(requiredCells);
            gui.setProtection(true);
        }
        progressbar.close();
    }

    interface ClauseHandler {
        public void addClause(VecInt vi);

        public void finish();
    }

    static class Sat4jClauseHandler implements ClauseHandler {
        ISolver solver;

        Sat4jClauseHandler(int numOfVariables, ISolver solver) {
            this.solver = solver;
            solver.newVar(numOfVariables);
        }

        public void addClause(VecInt vi) {
            try {
                solver.addClause(vi);
            } catch (ContradictionException ce) {
            }
        }

        public void addClauseUnTrapped(VecInt vi) throws ContradictionException {
            solver.addClause(vi);
        }

        public void finish() {
        }

    }

    static class StringClauseHandler implements ClauseHandler {
        StringBuffer buffer;

        String string;

        int clauses, variables;

        StringClauseHandler(int variables) {
            buffer = new StringBuffer();
            this.variables = variables;
            clauses = 0;
        }

        public void addClause(VecInt vi) {
            for (int i = 0; i < vi.size(); i++) {
                buffer.append(vi.get(i) + " ");
            }
            buffer.append("0\n");
            clauses++;
        }

        public void finish() {
            string = "p cnf " + variables + " " + clauses + "\n"
                    + buffer.toString();
        }

        @Override
        public String toString() {
            return string;
        }

    }

}
