package tk.milobot.commands.command;

import org.jetbrains.annotations.NotNull;

public interface ISubCommand {

    @NotNull ParentCommand getParentCommand();
}
