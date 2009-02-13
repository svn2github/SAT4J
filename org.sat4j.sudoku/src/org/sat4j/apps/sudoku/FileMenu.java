package org.sat4j.apps.sudoku;

public class FileMenu extends GeneralMenu<FileCommand> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    FileMenu(CommandHandler<FileCommand> commandHandler, SuDoku suDoku) {
        super("FILE_MENU_LABEL", new ButtonHandlerFactory<FileCommand>(commandHandler),
                suDoku);

        addMenuItem("FILE_IMPORT_PUZZLE", FileCommand.IMPORT_PUZZLE);
        addMenuItem("FILE_EXPORT_PUZZLE", FileCommand.EXPORT_PUZZLE);

        addSeparator();

        addMenuItem("FILE_IMPORT_MODEL", FileCommand.IMPORT_CNF_MODEL);
        addMenuItem("FILE_EXPORT_CNF", FileCommand.EXPORT_CNF_INSTANCE);

        addSeparator();

        addMenuItem("FILE_PAGE_SETUP", FileCommand.PAGE_SETUP);
        addMenuItem("FILE_PRINT", FileCommand.PRINT);

        addSeparator();

        addMenuItem("FILE_EXIT", FileCommand.EXIT);

    }
}
