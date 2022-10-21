package commands.morbconomy.bank;

import commands.Command;
import commands.morbconomy.MorbconomyCmd;
import database.dao.UserDao;
import database.model.User;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class BankBalanceCmd extends Command implements MorbconomyCmd {

    private final static UserDao userDao = UserDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BankBalanceCmd.class);

    public BankBalanceCmd() {
        this.commandName = "balance";
        this.commandDescription = "Check your bank balance.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
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
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
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
}
