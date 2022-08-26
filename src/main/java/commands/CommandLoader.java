package commands;

import commands.bot.StatusCmd;
import commands.bot.bug.BugCmd;
import commands.botadmin.RemoveUserCmd;
import commands.dnd.encounter.EncounterCmd;
import commands.games.blackjack.BlackjackCmd;
import commands.games.hungergames.HungerGamesCmd;
import commands.games.poker.PokerCmd;
import commands.games.wordle.WordleCmd;
import commands.morbconomy.DailyCmd;
import commands.morbconomy.ProfileCmd;
import commands.morbconomy.WalletCmd;
import commands.utility.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads in every command.
 */
public class CommandLoader {

    public static Map<List<String>, Command> commandList = new HashMap<>();

    public static void loadAllCommands(JDA bot) {
        ArrayList<Command> commands = new ArrayList<>();
        commands.add(HelpCmd.getInstance());
        commands.add(new InviteCmd());
        commands.add(new StatusCmd());
        commands.add(new PrefixCmd());
        commands.add(new UsageCmd());
        commands.add(new UserCmd());
        commands.add(new ProfileCmd());
        commands.add(new WordleCmd());
        commands.add(new BugCmd());
        commands.add(new EncounterCmd());
        commands.add(new BlackjackCmd());
        commands.add(new WalletCmd());
        commands.add(new DailyCmd());
        commands.add(new RemoveUserCmd());
        commands.add(new HungerGamesCmd());
        commands.add(new PokerCmd());

        commands.stream().flatMap(command -> command.listeners.stream()).forEach(bot::addEventListener);
        commands.stream().flatMap(command -> command.subCommands.stream())
                .flatMap(subCommand -> subCommand.listeners.stream()).forEach(bot::addEventListener);

        for (Command c : commands) {
            ArrayList<String> keys = new ArrayList<>(List.of(c.aliases));
            keys.add(c.commandName);
            commandList.put(keys, c);
        }

        bot.updateCommands()
                .addCommands(new CommandData("help", "Shows the user a list of available commands.")
                        .addOption(OptionType.STRING, "command", "The command you want information about.", false))

                .addCommands(new CommandData("encounter", "D&D 5e encounter generator.")
                        .addSubcommands(new SubcommandData("generate", "Generate a random encounter for the given inputs.")
                                .addOptions(new OptionData(OptionType.INTEGER, "size", "The size of the party.")
                                        .setRequired(true)
                                        .setRequiredRange(1, 10))
                                .addOptions(new OptionData(OptionType.INTEGER, "level", "The average level of the party.")
                                        .setRequired(true)
                                        .setRequiredRange(1, 20))
                                .addOptions(new OptionData(OptionType.STRING, "difficulty", "The difficulty of the encounter.")
                                        .setRequired(true)
                                        .addChoices(new Choice("easy", "easy"), new Choice("medium", "medium"),
                                                new Choice("difficult", "difficult"), new Choice("deadly", "deadly")))
                                .addOptions(new OptionData(OptionType.STRING, "environment", "The environment the encounter takes place in.")
                                        .setRequired(false)
                                        .addChoices(new Choice("city", "city"), new Choice("dungeon", "dungeon"),
                                                new Choice("forest", "forest"), new Choice("nature", "nature"),
                                                new Choice("other plane", "other plane"), new Choice("underground", "underground"),
                                                new Choice("water", "water")
                                        ))
                        ))

                .addCommands(new CommandData("wordle", "Wordle brought to discord.")
                        .addSubcommands(
                                new SubcommandData("leaderboard", "View the wordle leaderboards.")
                                        .addOptions(new OptionData(OptionType.STRING, "leaderboard", "The leaderboard you want to view.", true).addChoices(
                                                new Choice("total games played", "totalGamesPlayed"),
                                                new Choice("highest streak", "highestStreak"),
                                                new Choice("current streak", "currentStreak")
                                        )),
                                new SubcommandData("play", "Play a game of wordle."),
                                new SubcommandData("stats", "View your own blackjack statistics.")))

                .addCommands(new CommandData("bug", "Add bugs to the bots issue tracker, or view them.")
                        .addSubcommands(List.of(
                                new SubcommandData("report", "Report a bug you found."),
                                new SubcommandData("list", "Shows a list of all reported bugs."),
                                new SubcommandData("view", "Lookup a specific bug on the issue tracker.").addOptions(
                                        new OptionData(OptionType.INTEGER, "id", "The id of the bug you want to view", true)
                                ))))

                .addCommands(new CommandData("invite", "Sends an invite link to add the bot to another server."))

                .addCommands(new CommandData("profile", "View your own or someone else's profile.")
                        .addOption(OptionType.USER, "user", "The user you want to view the profile of.", false))

                .addCommands(new CommandData("prefix", "Change the prefix of the guild you're in.")
                        .addOption(OptionType.STRING, "prefix", "The new prefix.", true))

                .addCommands(new CommandData("blackjack", "Blackjack brought to discord").addSubcommands(
                        new SubcommandData("play", "Play a game of blackjack on discord.")
                                .addOptions(new OptionData(OptionType.INTEGER, "bet", "The amount of money you want to bet.", false)
                                        .setRequiredRange(1, 10000)),
                        new SubcommandData("stats", "View your own blackjack statistics.")))

                .addCommands(new CommandData("daily", "Collect your daily reward."))

                .queue();
    }
}
