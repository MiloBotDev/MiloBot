package commands.morbconomy;

import commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import database.dao.UserDao;
import database.model.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class WalletCmd extends Command implements MorbconomyCmd {
    private static final Logger logger = LoggerFactory.getLogger(WalletCmd.class);
    private final UserDao userDao = UserDao.getInstance();

    public WalletCmd() {
        this.commandName = "wallet";
        this.commandDescription = "Check your wallet.";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        User user;
        try {
            user = userDao.getUserByDiscordId(event.getAuthor().getIdLong());
        } catch (SQLException e) {
            logger.error("Error getting user from database at wallet command.", e);
            return;
        }
        int wallet = Objects.requireNonNull(user).getCurrency();
        event.getChannel().sendMessage(String.format("You have `%d` morbcoins in your wallet.", wallet)).queue();
    }
}
