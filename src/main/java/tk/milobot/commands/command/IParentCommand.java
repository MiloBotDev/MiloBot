package tk.milobot.commands.command;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IParentCommand {

    @NotNull String getSubCommandsText(@NotNull String prefix);
    @NotNull IParentCommand addSubCommand(@NotNull SubCommand subCommand);
    @NotNull List<SubCommand> getSubCommands();
}
