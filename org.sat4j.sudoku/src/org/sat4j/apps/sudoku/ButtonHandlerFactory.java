package org.sat4j.apps.sudoku;

class ButtonHandlerFactory<T extends Enum<T>> {
    ButtonHandlerFactory() {
    }

    ButtonHandlerFactory(CommandHandler<T> commandHandler) {
        this.commandHandler = commandHandler;
    }

    ButtonHandler<T> newButtonHandler(Enum<T> command) {
        return new ButtonHandler<T>(commandHandler, command);
    }

    CommandHandler<T> commandHandler;
}
