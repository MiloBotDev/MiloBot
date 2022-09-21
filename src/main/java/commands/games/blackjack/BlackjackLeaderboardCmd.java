package commands.games.blackjack;

import commands.Command;
import commands.SubCmd;
import database.dao.UserDao;
import database.model.Blackjack;
import database.model.Wordle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import utility.Users;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlackjackLeaderboardCmd extends Command implements SubCmd {

    private static final UserDao userDao = UserDao.getInstance();
    private static final Users userUtil = Users.getInstance();

    public BlackjackLeaderboardCmd() {
        this.commandName = "leaderboard";
        this.commandDescription = "View the blackjack leaderboards.";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        SelectionMenu menu = SelectionMenu.create(event.getAuthor().getId() + ":blackjackLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Draws", "totalDraws")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Earnings", "totalEarnings")
                .build();
        event.getChannel().sendMessage("Blackjack Leaderboard Selection").setActionRow(menu).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        SelectionMenu menu = SelectionMenu.create(event.getUser().getId() + ":blackjackLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Draws", "totalDraws")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Earnings", "totalEarnings")
                .build();
        event.reply("Blackjack Leaderboard Selection").addActionRow(menu).queue();
    }

    public static @NotNull ArrayList<MessageEmbed> buildBlackjackEmbeds(@NotNull List<Blackjack> blackjacks, String title, JDA jda) {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] counter = {1};
        blackjacks.forEach((blackjack) -> {
            try {
                long discordId = Objects.requireNonNull(userDao.getUserById(blackjack.getUserId())).getDiscordId();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Highest Streak" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, blackjack.getHighestStreak()));
                    case "Current Streak" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, blackjack.getStreak()));
                    case "Total Wins" -> desc[0].append(String.format("`%d.` %s - %d wins.\n", counter[0], name, blackjack.getTotalWins()));
                    case "Total Draws" -> desc[0].append(String.format("`%d.` %s - %d draws.\n", counter[0], name, blackjack.getTotalDraws()));
                    case "Total Games Played" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, blackjack.getTotalGames()));
                    case "Total Earnings" -> desc[0].append(String.format("`%d.` %s - %d morbcoins.\n", counter[0], name, blackjack.getTotalEarnings()));
                }
                counter[0]++;
                if(counter[0] % 10 == 0) {
                    embed[0].setDescription(desc[0]);
                    embeds.add(embed[0].build());
                    embed[0] = new EmbedBuilder();
                    desc[0] = new StringBuilder();
                    embed[0].setTitle(title);
                    embed[0].setColor(Color.BLUE);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        embed[0].setDescription(desc[0]);
        embeds.add(embed[0].build());

        return embeds;
    }
}
