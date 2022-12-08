package tk.milobot.commands.newcommand;

import org.jetbrains.annotations.NotNull;

public interface ISubCommand {

    @NotNull ParentCommand getParentCommand();
}
