package org.sat4j.apps.sudoku;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class FileCommandHandler extends CommandHandler<FileCommand> {

    public FileCommandHandler(SuDoku sudoku) {
        super(sudoku);
        pageFormat = new PageFormat();
        pageFormat.setPaper(new A4Paper());
    }

    void importPuzzle() {
        MainProgramWindow mainProgramWindow;

        mainProgramWindow = sudoku.getMainProgramWindow();
        if (mainProgramWindow.isApplet()) {
            sudoku.getGui().setResult(
                    "Can't read files from an applet - use Edit menu");
        } else if (!mainProgramWindow.fileAccess()) {
            sudoku.getGui().setResult("No access to files");
        } else {
            String toImport = mainProgramWindow.readFile();
            CellGrid cellGrid = sudoku.getGui().getCellGrid();
            cellGrid.clearProtection();
            cellGrid.clearAll();
            sudoku.getGui().getCellGrid().importString(toImport);
            sudoku.getGui().setProtection(true);
        }
    }

    void exportPuzzle() {
        MainProgramWindow mainProgramWindow;

        mainProgramWindow = sudoku.getMainProgramWindow();
        if (mainProgramWindow.isApplet()) {
            sudoku.getGui().setResult(
                    "Can't write to files from an applet - use Edit menu");
        } else if (!mainProgramWindow.fileAccess()) {
            sudoku.getGui().setResult("No access to files");
        } else {
            String toExport = sudoku.getGui().getCellGrid().exportString();
            mainProgramWindow.writeFile(toExport);
        }
    }

    void importModel() {
        MainProgramWindow mainProgramWindow;

        mainProgramWindow = sudoku.getMainProgramWindow();
        if (mainProgramWindow.isApplet()) {
            sudoku.getGui().setResult(
                    "Can't read files from an applet - use tab \"cnf\"");
        } else if (!mainProgramWindow.fileAccess()) {
            sudoku.getGui().setResult("No access to files");
        } else {
            String toImport = mainProgramWindow.readFile();
            sudoku.getGui().setCNFModel(toImport);
            CellGrid cellGrid = sudoku.getGui().getCellGrid();
            cellGrid.clearComputers();
            sudoku.interpretModel(cellGrid);
        }
    }

    void exportInstance() {
        MainProgramWindow mainProgramWindow;

        mainProgramWindow = sudoku.getMainProgramWindow();
        if (mainProgramWindow.isApplet()) {
            sudoku.getGui().setResult(
                    "Can't write to files from an applet - use tab \"cnf\"");
        } else if (!mainProgramWindow.fileAccess()) {
            sudoku.getGui().setResult("No access to files");
        } else {
            sudoku.fullCNF(sudoku.getGui().getCellGrid());
            String toExport = sudoku.getGui().getCNFFile();
            mainProgramWindow.writeFile(toExport);
        }
    }

    void processPrint() {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintable(sudoku.getGui().getCellGrid(), pageFormat);
            if (printerJob.printDialog()) {
                printerJob.print();
            }
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    void processPageSetup() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        pageFormat = printerJob.pageDialog(pageFormat);
    }

    @Override
    public void execute(Enum<FileCommand> command) {
        switch ((FileCommand) command) {
        case IMPORT_PUZZLE:
            importPuzzle();
            break;

        case EXPORT_PUZZLE:
            exportPuzzle();
            break;

        case IMPORT_CNF_MODEL:
            importModel();
            break;

        case EXPORT_CNF_INSTANCE:
            exportInstance();
            break;

        case PAGE_SETUP:
            processPageSetup();
            break;

        case PRINT:
            processPrint();
            break;

        case EXIT:
            if (sudoku.getMainProgramWindow().isApplet()) {
                sudoku.getGui().setResult("Can't exit from an applet");
            } else {
                System.exit(0);
            }
            break;
        }
    }

    PageFormat pageFormat;

}
