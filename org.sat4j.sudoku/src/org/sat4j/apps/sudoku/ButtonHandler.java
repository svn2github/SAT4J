package org.sat4j.apps.sudoku;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonHandler<T extends Enum<T>> implements ActionListener {

    ButtonHandler() {
    }

    ButtonHandler(CommandHandler<T> commandHandler, Enum<T> command) {
        this.commandHandler = commandHandler;
        this.command = command;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            commandHandler.execute(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  CommandHandler<T> commandHandler;

    private  Enum<T> command;
}
