package io.github.milobotdev.milobot.commands.morbconomy.bank;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.User;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BankBalanceCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs {

    private final ExecutorService executorService;
    private final static UserDao userDao = UserDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BankBalanceCmd.class);

    public BankBalanceCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("balance", "Check your bank balance.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        User user;
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            user = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.NONE);
            con.commit();
        } catch (SQLException e) {
            logger.error("Error getting user from database at bank balance command.", e);
            return;
        }
        int wallet = Objects.requireNonNull(user).getCurrency();
        event.getChannel().sendMessage(String.format("You have `%d` morbcoins in your bank.", wallet)).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        User user;
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            user = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.NONE);
            con.commit();
        } catch (SQLException e) {
            logger.error("Error getting user from database at bank balance command.", e);
            return;
        }
        int wallet = Objects.requireNonNull(user).getCurrency();
        event.reply(String.format("You have `%d` morbcoins in your bank.", wallet)).queue();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }
}
