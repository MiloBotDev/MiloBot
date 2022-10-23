package commands.games.dnd.encounter;

import commands.Command;
import commands.SubCmd;
import games.dnd.models.Encounter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import games.dnd.EncounterGenerator;

import java.util.*;

/**
 * Lets users generate a random d&d encounter.
 */
public class EncounterGeneratorCmd extends Command implements SubCmd {

    private final static Logger logger = LoggerFactory.getLogger(EncounterGeneratorCmd.class);
    private static EncounterGeneratorCmd instance;
    private final String[] difficulties;
    private final String[] environments;
    private final EncounterGenerator gen;
    private final Map<String, Encounter> encounterCache;

    private EncounterGeneratorCmd() {
        this.commandName = "generate";
        this.commandDescription = "Generate a random encounter for a given average party level, party size, " +
                "difficulty and an optional environment.";
        this.commandArgs = new String[]{"party size, party level, difficulty, *environment"};
        this.difficulties = new String[]{"easy", "medium", "difficult", "deadly"};
        this.environments = new String[]{"city", "dungeon", "forest", "nature", "other plane", "underground", "water"};
        this.gen = EncounterGenerator.getInstance();
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.encounterCache = new HashMap<>();
        this.slashSubcommandData = new SubcommandData("generate", "Generate a random encounter for the given inputs.")
                .addOptions(new OptionData(OptionType.INTEGER, "size", "The size of the party.")
                        .setRequired(true)
                        .setRequiredRange(1, 10))
                .addOptions(new OptionData(OptionType.INTEGER, "level", "The average level of the party.")
                        .setRequired(true)
                        .setRequiredRange(1, 20))
                .addOptions(new OptionData(OptionType.STRING, "difficulty", "The difficulty of the encounter.")
                        .setRequired(true)
                        .addChoices(new net.dv8tion.jda.api.interactions.commands.Command.Choice("easy", "easy"), new net.dv8tion.jda.api.interactions.commands.Command.Choice("medium", "medium"),
                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("difficult", "difficult"), new net.dv8tion.jda.api.interactions.commands.Command.Choice("deadly", "deadly")))
                .addOptions(new OptionData(OptionType.STRING, "environment", "The environment the encounter takes place in.")
                        .setRequired(false)
                        .addChoices(new net.dv8tion.jda.api.interactions.commands.Command.Choice("city", "city"), new net.dv8tion.jda.api.interactions.commands.Command.Choice("dungeon", "dungeon"),
                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("forest", "forest"), new net.dv8tion.jda.api.interactions.commands.Command.Choice("nature", "nature"),
                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("other plane", "other plane"), new net.dv8tion.jda.api.interactions.commands.Command.Choice("underground", "underground"),
                                new net.dv8tion.jda.api.interactions.commands.Command.Choice("water", "water")
                        ));
    }

    public static EncounterGeneratorCmd getInstance() {
        if (instance == null) {
            instance = new EncounterGeneratorCmd();
        }
        return instance;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (args.size() < 3) {
            sendCommandUsage(event);
            return;
        }
        int partySize;
        int partyLevel;
        try {
            partySize = Integer.parseInt(args.get(0));
            partyLevel = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Party size and level must both be numbers.").queue();
            logger.error(e.getMessage());
            return;
        }
        if (partySize < 1 || partySize > 10) {
            event.getChannel().sendMessage("Party size must be a number between 1 and 10").queue();
            return;
        }
        if (partyLevel < 1 || partyLevel > 20) {
            event.getChannel().sendMessage("Party level must be a number between 1 and 20").queue();
            return;
        }
        String difficulty = args.get(2);
        if (!(Arrays.asList(difficulties).contains(difficulty.toLowerCase(Locale.ROOT)))) {
            event.getChannel().sendMessage(String.format("%s is not a valid difficulty. Please choose one of: " +
                    "%s, %s, %s, %s.", difficulty, difficulties[0], difficulties[1], difficulties[2], difficulties[3])).queue();
            return;
        }
        String environment = null;
        if (args.size() > 3) {
            environment = args.get(3);
            if (args.size() > 4) {
                environment += String.format(" %s", args.get(4));
            }
            if (!(Arrays.asList(environments).contains(environment.toLowerCase(Locale.ROOT)))) {
                StringBuilder envError = new StringBuilder();
                envError.append(String.format("%s is not a valid environment. Please choose one of: ", environment));
                for (int i = 0; i < environments.length; i++) {
                    envError.append(environments[i]);
                    if (i + 1 == environments.length) {
                        envError.append(".");
                    }
                }
                event.getChannel().sendMessage(envError.toString()).queue();
                return;
            }
        }
        Map<EmbedBuilder, Encounter> embedBuilderEncounterMap = buildEncounterEmbed(
                event.getAuthor(), partySize, partyLevel, difficulty, environment);
        EmbedBuilder embed = (EmbedBuilder) embedBuilderEncounterMap.keySet().toArray()[0];
        MessageEmbed build = embed.build();
        event.getChannel().sendMessageEmbeds(build).setActionRows(ActionRow.of(
                Button.primary(event.getAuthor().getId() + ":regenerate", "Regenerate"),
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete"))).queue();
        encounterCache.put(build.getFields().get(0).getValue(), embedBuilderEncounterMap.get(embed));
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        int partySize = Math.toIntExact(Objects.requireNonNull(event.getOption("size")).getAsLong());
        int partyLevel = Math.toIntExact(Objects.requireNonNull(event.getOption("level")).getAsLong());
        String difficulty = Objects.requireNonNull(event.getOption("difficulty")).getAsString();
        String environment = null;
        if (!(event.getOption("environment") == null)) {
            environment = Objects.requireNonNull(event.getOption("environment")).getAsString();
        }
        Map<EmbedBuilder, Encounter> embedBuilderEncounterMap = buildEncounterEmbed(
                event.getUser(), partySize, partyLevel, difficulty, environment);
        EmbedBuilder embed = (EmbedBuilder) embedBuilderEncounterMap.keySet().toArray()[0];
        String id = event.getUser().getId();
        MessageEmbed build = embed.build();
        event.replyEmbeds(build).addActionRows(
                ActionRow.of(Button.primary(id + ":regenerate", "Regenerate"),
                        Button.secondary(id + ":delete", "Delete"))).queue();
        encounterCache.put(build.getFields().get(0).getValue(), embedBuilderEncounterMap.get(embed));
    }

    @NotNull
    private Map<EmbedBuilder, Encounter> buildEncounterEmbed(@NotNull User author, int partySize, int partyLevel, String difficulty, String environment) {
        Encounter encounter = gen.generateEncounter(partySize, partyLevel, difficulty, environment);
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, author);
        embed.setTitle("Generated encounter");
        String desc = String.format("**Party Size:** %d\n**Party Level:** %d\n**Difficulty:** %s\n",
                partySize, partyLevel, difficulty);
        if (environment != null) {
            desc += String.format("**Environment:** %s", environment);
        }
        embed.setDescription(desc);
        embed.addField("Encounter", encounter.toString(), false);
        return Map.of(embed, encounter);
    }

    /**
     * Generates the encounter with the same inputs.
     */
    public MessageEmbed regenerateEncounter(@NotNull MessageEmbed embed, User author) {
        EmbedBuilder newEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(newEmbed, author);
        newEmbed.setTitle(embed.getTitle());
        String description = embed.getDescription();
        newEmbed.setDescription(description);

        Encounter encounter;
        String oldDescription = embed.getFields().get(0).getValue();
        encounter = encounterCache.get(oldDescription);
        MessageEmbed build;

        if (encounter == null) {
            newEmbed.setDescription("Something went wrong!");
            return newEmbed.build();
        } else {
            Encounter newEncounter = gen.generateEncounter(encounter.getPartySize(), encounter.getPartyLevel(),
                    encounter.getDifficulty(), encounter.getEnvironment());
            newEmbed.addField("Encounter", newEncounter.toString(), false);
            build = newEmbed.build();
            encounterCache.remove(oldDescription);
            encounterCache.put(build.getFields().get(0).getValue(), newEncounter);
            return build;
        }
    }

}
