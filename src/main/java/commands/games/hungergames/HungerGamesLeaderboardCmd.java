package commands.games.hungergames;

import commands.Command;
import commands.SubCmd;
import database.dao.UserDao;
import database.model.HungerGames;
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

public class HungerGamesLeaderboardCmd extends Command implements SubCmd {

    private static final UserDao userDao = UserDao.getInstance();
    private static final Users userUtil = Users.getInstance();

    public HungerGamesLeaderboardCmd() {
        this.commandName = "leaderboard";
        this.commandDescription = "View the hungergames leaderboards.";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        SelectionMenu menu = SelectionMenu.create(event.getAuthor().getId() + ":hgLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Total Kills", "totalKills")
                .addOption("Total Damage Done", "totalDamageDone")
                .addOption("Total Damage Taken", "totalDamageTaken")
                .addOption("Total Healing Done", "totalHealingDone")
                .addOption("Total Items Collected", "totalItemsCollected")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Wins", "totalWins")
                .build();
        event.getChannel().sendMessage("HungerGames Leaderboard Selection").setActionRow(menu).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        SelectionMenu menu = SelectionMenu.create(event.getUser().getId() + ":hgLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Total Kills", "totalKills")
                .addOption("Total Damage Done", "totalDamageDone")
                .addOption("Total Damage Taken", "totalDamageTaken")
                .addOption("Total Healing Done", "totalHealingDone")
                .addOption("Total Items Collected", "totalItemsCollected")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Wins", "totalWins")
                .build();
        event.reply("HungerGames Leaderboard Selection").addActionRow(menu).queue();
    }

    public static @NotNull ArrayList<MessageEmbed> buildHgEmbeds(@NotNull List<HungerGames> hungerGames, String title, JDA jda) {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] counter = {1};
        hungerGames.forEach((hg) -> {
            try {
                long discordId = Objects.requireNonNull(userDao.getUserById(hg.getUserId())).getDiscordId();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Total Kills" -> desc[0].append(String.format("`%d.` %s - %d kills.\n", counter[0], name, hg.getTotalKills()));
                    case "Total Damage Done" -> desc[0].append(String.format("`%d.` %s - %d damage done.\n", counter[0], name, hg.getTotalDamageDone()));
                    case "Total Damage Taken" -> desc[0].append(String.format("`%d.` %s - %d damage taken.\n", counter[0], name, hg.getTotalDamageTaken()));
                    case "Total Healing Done" -> desc[0].append(String.format("`%d.` %s - %d healing done.\n", counter[0], name, hg.getTotalHealingDone()));
                    case "Total Items Collected" -> desc[0].append(String.format("`%d.` %s - %d items collected.\n", counter[0], name, hg.getTotalItemsCollected()));
                    case "Total Games Played" -> desc[0].append(String.format("`%d.` %s - %d games played.\n", counter[0], name, hg.getTotalGamesPlayed()));
                    case "Total Wins" -> desc[0].append(String.format("`%d.` %s - %d wins.\n", counter[0], name, hg.getTotalWins()));
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
