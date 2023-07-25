package io.github.milobotdev.milobot.commands.morbconomy;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.SlashCommand;
import io.github.milobotdev.milobot.commands.command.extensions.TextCommand;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.ParentSlashCommandData;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.database.dao.*;
import io.github.milobotdev.milobot.database.model.*;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.Users;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ProfileCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, MorbconomyCmd{

    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(ProfileCmd.class);
    private final Users user;
    private final UserDao userDao = UserDao.getInstance();
    private final DailyDao dailyDao = DailyDao.getInstance();
    private final BlackjackDao blackjackDao = BlackjackDao.getInstance();
    private final WordleDao wordleDao = WordleDao.getInstance();
    private final UnoDao unoDao = UnoDao.getInstance();
    private final CommandTrackerDao commandTrackerDao = CommandTrackerDao.getInstance();
    private final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();

    public ProfileCmd(ExecutorService executorService) {
        this.executorService = executorService;
        this.user = Users.getInstance();
    }

    @Override
    public @NotNull ParentSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSlashCommandData(
                Commands.slash("profile", "View your own or someone else's profile.")
                    .addOption(OptionType.USER, "user", "The user to view the profile of.", false)
        );
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("*user");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if(args.size() == 0) {
            return true;
        }
        return true;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        net.dv8tion.jda.api.entities.User author = event.getAuthor();
        if (args.size() == 0) {
            String name = author.getName();
            Optional<EmbedBuilder> embedBuilder = makeEmbed(name, author, author);
            if (embedBuilder.isPresent()) {
                event.getChannel().sendMessageEmbeds(embedBuilder.get().build()).setActionRow(
                        Button.secondary(author.getId() + ":delete", "Delete")).queue();
            } else {
                event.getChannel().sendMessage("Something went wrong.").queue();
            }
        } else {
            String findUser = String.join(" ", args);
            try {
                List<Member> usersByName = new ArrayList<>();
                event.getGuild().findMembers(e -> e.getUser().getName().toLowerCase(Locale.ROOT).equals(findUser)
                                || e.getUser().getAsMention().equals(findUser) || (e.getNickname() != null && e.getNickname().equals(findUser)))
                        .onSuccess(members -> {
                            usersByName.addAll(members);
                            if (usersByName.size() == 0) {
                                event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
                            } else {
                                net.dv8tion.jda.api.entities.User user = usersByName.get(0).getUser();
                                String name = user.getName();
                                Optional<EmbedBuilder> embed = makeEmbed(name, author, user);
                                if (embed.isPresent()) {
                                    event.getChannel().sendMessageEmbeds(embed.get().build()).setActionRow(
                                            Button.secondary(author.getId() + ":delete", "Delete")).queue();
                                } else {
                                    event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
                                }
                            }
                        });
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        net.dv8tion.jda.api.entities.User author = event.getUser();
        if (event.getOption("user") == null) {
            String name = author.getName();
            Optional<EmbedBuilder> embedBuilder = makeEmbed(name, author, author);
            if (embedBuilder.isPresent()) {
                event.getHook().sendMessageEmbeds(embedBuilder.get().build()).addActionRow(
                        Button.secondary(author.getId() + ":delete", "Delete")).queue();
            } else {
                event.getHook().sendMessage("Something went wrong.").queue();
            }
        } else {
            String findUser = Objects.requireNonNull(event.getOption("user")).getAsString();
            try {
                List<Member> usersByName = new ArrayList<>();
                Objects.requireNonNull(event.getGuild()).findMembers(e -> e.getUser().getId().equals(findUser))
                        .onSuccess(members -> {
                            usersByName.addAll(members);
                            if (usersByName.size() == 0) {
                                event.getHook().sendMessage(String.format("User `%s` not found.", findUser)).queue();
                            } else {
                                net.dv8tion.jda.api.entities.User user = usersByName.get(0).getUser();
                                String name = user.getName();
                                Optional<EmbedBuilder> embed = makeEmbed(name, author, user);
                                if (embed.isPresent()) {
                                    event.getHook().sendMessageEmbeds(embed.get().build()).addActionRow(
                                            Button.secondary(author.getId() + ":delete", "Delete")).queue();
                                } else {
                                    event.getHook().sendMessage(String.format("Can't create a profile for `%s`.", name)).queue();
                                }
                            }
                        });
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Builds the embed for the Profile command.
     */
    private Optional<EmbedBuilder> makeEmbed(String name, net.dv8tion.jda.api.entities.User author,
                                             net.dv8tion.jda.api.entities.User userToLookup) {
        User userDbObj;
        Daily dailyDbObj;
        Optional<Uno> unoDbObj;
        Blackjack blackjackDbObj;
        Wordle wordleDbObj;
        HungerGames hungerGamesDbObj;
        int experienceRank;
        int currencyRank;
        int totalUsers;
        long userIdToLookup = userToLookup.getIdLong();
        int totalCommandsUsed;
        String mostUsedCommand;
        try (Connection con = DatabaseConnection.getConnection()) {
            userDbObj = userDao.getUserByDiscordId(con, userIdToLookup, RowLockType.NONE);
            dailyDbObj = dailyDao.getDailyByUserDiscordId(con, userIdToLookup, RowLockType.NONE);
            experienceRank = userDao.getUserExperienceRank(con, Objects.requireNonNull(userDbObj).getId());
            totalUsers = userDao.getTotalUserCount(con, RowLockType.NONE);
            unoDbObj = unoDao.getByUserDiscordId(con, userIdToLookup, RowLockType.NONE);
            blackjackDbObj = blackjackDao.getByUserDiscordId(con, userIdToLookup, RowLockType.NONE);
            wordleDbObj = wordleDao.getByUserDiscordId(con, userIdToLookup, RowLockType.NONE);
            int userId = userDbObj.getId();
            totalCommandsUsed = commandTrackerDao.getUserTotalCommandUsage(userId);
            hungerGamesDbObj = hungerGamesDao.getByUserDiscordId(con, userIdToLookup, RowLockType.NONE);
            mostUsedCommand = commandTrackerDao.getUserMostUsedCommand(userId);
            currencyRank = userDao.getUserCurrencyRank(con, userId);
        } catch (Exception e) {
            logger.error("Error getting user at making user embed at profile command", e);
            return Optional.empty();
        }

        int currency = userDbObj.getCurrency();
        int level = userDbObj.getLevel();
        int experience = userDbObj.getExperience();
        int morbcoinsClaimed = 0;
        int dailyStreak = 0;
        int totalClaimed = 0;
        if(dailyDbObj != null) {
            morbcoinsClaimed = dailyDbObj.getTotalCurrencyClaimed();
            dailyStreak = dailyDbObj.getStreak();
            totalClaimed = dailyDbObj.getTotalClaimed();
        }

        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, author);
        embed.setTitle(name);
        embed.setThumbnail(userToLookup.getEffectiveAvatarUrl());

        StringBuilder levelDescription = new StringBuilder();
        Optional<String> levelProgressBar = generateLevelProgressBar(level, experience);
        levelDescription.append(String.format("Level: `%s`\n", level));
        levelDescription.append(String.format("Experience: `%s`\n", experience));
        if (levelProgressBar.isPresent()) {
            levelDescription.append("Progress till next level: ");
            levelDescription.append(String.format("`%s`\n", levelProgressBar.get()));
        } else {
            levelDescription.append("You are at the maximum level.\n");
        }
        levelDescription.append(String.format("Rank: `%s` out of `%d` players.\n", generateOrdinal(experienceRank), totalUsers));
        embed.setDescription(levelDescription.toString());


        String morbcoinStats = String.format("Total Morbcoins: `%s`\n", currency) +
                String.format("Morbcoins Claimed: `%s`\n", morbcoinsClaimed) +
                String.format("Daily Streak: `%s`\n", dailyStreak) +
                String.format("Total Dailies Claimed: `%s`\n", totalClaimed) +
                String.format("Rank: `%s` out of `%d` players.\n", generateOrdinal(currencyRank), totalUsers);
        embed.addField("Morbcoin Stats", morbcoinStats, false);

        int unoWins = 0;
        int blackjackWins = 0;
        int wordleWins = 0;
        int hungerGamesWins = 0;
        if(unoDbObj.isPresent()) {
            unoWins = unoDbObj.get().getTotalWins();
        }
        if(blackjackDbObj != null) {
            blackjackWins = blackjackDbObj.getTotalWins();
        }
        if(wordleDbObj != null) {
            wordleWins = wordleDbObj.getTotalWins();
        }
        if(hungerGamesDbObj != null) {
            hungerGamesWins = hungerGamesDbObj.getTotalWins();
        }

        String gameStats = String.format("Uno Wins: `%s`\n", unoWins) +
                String.format("Blackjack Wins: `%s`\n", blackjackWins) +
                String.format("Wordle Wins: `%s`\n", wordleWins) +
                String.format("Hunger Games Wins: `%s`\n", hungerGamesWins);
        embed.addField("Game Statistics", gameStats, false);

        if(mostUsedCommand == null) {
        	mostUsedCommand = "None";
        }

        String miscStats = String.format("Total Commands Used: `%s`\n", totalCommandsUsed) +
                String.format("Most Used Command: `%s`\n", mostUsedCommand);
        embed.addField("Misc Statistics", miscStats, false);

        return Optional.of(embed);
    }

    /**
     * Generates a progress bar based on the amount of experience is needed for the next level.
     */
    private @NotNull Optional<String> generateLevelProgressBar(int currentLevel, int currentExperience) {
        int nextLevel = currentLevel + 1;
        if (nextLevel > user.maxLevel) {
            return Optional.empty();
        }

        int experienceDifference = user.levels.get(nextLevel) - user.levels.get(currentLevel);
        int gainedExperience = currentExperience - user.levels.get(currentLevel);
        float percentageDone = (float) gainedExperience / experienceDifference;

        DecimalFormat df = new DecimalFormat("#.#");
        String neededBlocksFormat = df.format(percentageDone);
        int neededBlocks = Integer.parseInt(neededBlocksFormat.substring(neededBlocksFormat.length() - 1));

        df = new DecimalFormat("#.##");
        String percentageDoneFormat = df.format(percentageDone);
        if (percentageDoneFormat.length() == 3) {
            percentageDoneFormat += "0";
        }
        String percentageDoneString;
        if (percentageDoneFormat.equals("0")) {
            percentageDoneString = "0";
        } else {
            percentageDoneString = percentageDoneFormat.substring(percentageDoneFormat.length() - 2);
        }
        if(percentageDoneString.length() == 2 && percentageDoneString.charAt(0) == '0') {
            percentageDoneString = percentageDoneString.substring(1);
        }

        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (neededBlocks > 0) {
                progressBar.append("#");
                neededBlocks--;
            } else {
                progressBar.append("-");
            }
        }
        progressBar.append(String.format("] %s", percentageDoneString)).append("%");

        return Optional.of(progressBar.toString());
    }

    private String generateOrdinal(int number) {
        if (number >= 11 && number <= 13) {
            return number + "th";
        }

        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }
}
