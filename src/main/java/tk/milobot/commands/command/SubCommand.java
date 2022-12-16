package tk.milobot.commands.command;

import org.jetbrains.annotations.NotNull;

public abstract class SubCommand extends Command implements ISubCommand {
    private ParentCommand parentCommand = null;

    void assignParentCommand(@NotNull ParentCommand parentCommand) {
        if (this.parentCommand == null) {
            this.parentCommand = parentCommand;
        } else {
            throw new IllegalArgumentException("Parent command already assigned.");
        }
    }

    @Override
    public @NotNull ParentCommand getParentCommand() {
        return parentCommand;
    }
}
