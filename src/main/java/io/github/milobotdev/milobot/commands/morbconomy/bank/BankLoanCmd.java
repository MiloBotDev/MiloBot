package io.github.milobotdev.milobot.commands.morbconomy.bank;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.SlashCommand;
import io.github.milobotdev.milobot.commands.command.extensions.TextCommand;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BankLoanCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes {

    private final ExecutorService executorService;
    private final UserDao userDao = UserDao.getInstance();

    public BankLoanCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(
                new SubcommandData("loan", "Loan some morbcoins from the bank.")
        );
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("amount");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        try {
            int amount = Integer.parseInt(args.get(0));
            if(amount >= 1 && amount <= 10000) {
                return true;
            } else {
                event.getChannel().sendMessage("You can only loan between 1 and 10000 morbcoins.").queue();
                return false;
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Invalid amount.").queue();
            return false;
        }
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {

    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {

    }

    private void updateDatabase(@NotNull User author) throws SQLException {
        try(Connection con = DatabaseConnection.getConnection()) {
            userDao.getUserByDiscordId(con, author.getIdLong(), RowLockType.FOR_UPDATE);
        }
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @NotNull
    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
