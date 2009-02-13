package org.sat4j.apps.sudoku;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class EditCommandHandler extends CommandHandler<EditCommand> {

    public EditCommandHandler(SuDoku sudoku) {
        super(sudoku);
    }

    void writeToClipboard(String writeMe) {
        // get the system clipboard
        Clipboard systemClipboard = Toolkit.getDefaultToolkit()
                .getSystemClipboard();
        // set the textual content on the clipboard to our
        // Transferable object
        // we use the
        Transferable transferableText = new StringSelection(writeMe);
        systemClipboard.setContents(transferableText, null);

    }

    String readFromClipboard() {
        String str = "";
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            str = (String) c.getContents(this).getTransferData(
                    DataFlavor.stringFlavor);
            if (str == null) {
                str = "";
            }
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

    @Override
    public void execute(Enum<EditCommand> command) {
        switch ((EditCommand) command) {
        case COPY_PUZZLE:
            String toCopy = sudoku.getGui().getCellGrid().exportString();
            if (sudoku.getMainProgramWindow().isApplication()) {
                writeToClipboard(toCopy);
            } else {
                sudoku.getGui().setPuzzlePaste(toCopy);
            }
            break;

        case PASTE_PUZZLE:
            String toPaste;
            if (sudoku.getMainProgramWindow().isApplication()) {
                toPaste = readFromClipboard();
            } else {
                toPaste = sudoku.getGui().getPuzzlePaste();
            }

            CellGrid cellGrid = sudoku.getGui().getCellGrid();
            cellGrid.clearProtection();
            cellGrid.clearAll();
            sudoku.getGui().getCellGrid().importString(toPaste);
            sudoku.getGui().setProtection(true);
            break;
        }
    }

}
