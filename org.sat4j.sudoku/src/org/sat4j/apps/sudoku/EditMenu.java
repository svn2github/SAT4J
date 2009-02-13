package org.sat4j.apps.sudoku;

public class EditMenu extends GeneralMenu<EditCommand> {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    EditMenu(CommandHandler<EditCommand> commandHandler, SuDoku suDoku) {
        super("EDIT_MENU_LABEL", new ButtonHandlerFactory<EditCommand>(commandHandler),
                suDoku);

        addMenuItem("EDIT_COPY_PUZZLE", EditCommand.COPY_PUZZLE);
        addMenuItem("EDIT_PASTE_PUZZLE", EditCommand.PASTE_PUZZLE);

    }
}
