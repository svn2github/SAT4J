package org.sat4j.apps.sudoku;

public class HelpMenu extends GeneralMenu<HelpCommand> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    HelpMenu(CommandHandler<HelpCommand> commandHandler, SuDoku suDoku) {
        super("HELP_MENU_LABEL", new ButtonHandlerFactory<HelpCommand>(commandHandler),
                suDoku);

        addMenuItem("HELP_SUDOKU", HelpCommand.SUDOKU);
        addMenuItem("HELP_COMPLETE_SUDOKU", HelpCommand.COMPLETE_SUDOKU);
        addMenuItem("HELP_SAT4J", HelpCommand.SAT4J);

        addSeparator();

        addMenuItem("HELP_ABOUT", HelpCommand.ABOUT);

    }
}
