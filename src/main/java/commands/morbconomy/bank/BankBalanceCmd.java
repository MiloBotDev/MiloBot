package commands.morbconomy.bank;

import commands.Command;
import commands.games.hungergames.HungerGamesStartCmd;
import commands.morbconomy.MorbconomyCmd;
import database.dao.UserDao;
import database.model.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class BankBalanceCmd extends Command implements MorbconomyCmd {

    private final static UserDao userDao = UserDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BankBalanceCmd.class);

    public BankBalanceCmd() {
        this.commandName = "balance";
        this.commandDescription = "Check your bank balance.";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        User user;
        try {
            user = userDao.getUserByDiscordId(event.getAuthor().getIdLong());
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
        try {
            user = userDao.getUserByDiscordId(event.getUser().getIdLong());
        } catch (SQLException e) {
            logger.error("Error getting user from database at bank balance command.", e);
            return;
        }
        int wallet = Objects.requireNonNull(user).getCurrency();
        event.reply(String.format("You have `%d` morbcoins in your bank.", wallet)).queue();
    }
}
