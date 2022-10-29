package commands.games.blackjack;

import commands.Command;
import commands.SubCmd;
import database.dao.BlackjackDao;
import database.dao.UserDao;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import games.BlackjackGame;
import models.cards.PlayingCard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlackjackPlayCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(BlackjackPlayCmd.class);
    private final UserDao userDao = UserDao.getInstance();
    private final BlackjackDao blackjackDao = BlackjackDao.getInstance();

    public BlackjackPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of blackjack on discord.";
        this.commandArgs = new String[]{"bet*"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData("play", "Play a game of blackjack on discord.")
                .addOptions(new OptionData(OptionType.INTEGER, "bet", "The amount of money you want to bet.", false)
                        .setRequiredRange(1, 10000));
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        BlackjackGame.newGame(event, args);
    }

    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        BlackjackGame.newGame(event);
    }
}
