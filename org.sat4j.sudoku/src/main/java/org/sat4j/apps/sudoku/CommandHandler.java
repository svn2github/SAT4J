package org.sat4j.apps.sudoku;

public abstract class CommandHandler<T extends Enum<T>> {

    public CommandHandler(SuDoku sudoku) {
        this.sudoku = sudoku;
    }

    public abstract void execute(Enum<T> command);

    SuDoku sudoku;

}
