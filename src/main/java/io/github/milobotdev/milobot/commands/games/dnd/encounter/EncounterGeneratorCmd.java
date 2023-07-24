package io.github.milobotdev.milobot.commands.games.dnd.encounter;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.SlashCommand;
import io.github.milobotdev.milobot.commands.command.extensions.TextCommand;
import io.github.milobotdev.milobot.games.dnd.EncounterGenerator;
import io.github.milobotdev.milobot.games.dnd.models.Encounter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.milobotdev.milobot.utility.EmbedUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Lets users generate a random d&d encounter.
 */
public class EncounterGeneratorCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes {

    private final ExecutorService executorService;
    private final static Logger logger = LoggerFactory.getLogger(EncounterGeneratorCmd.class);
    private final String[] difficulties;
    private final String[] environments;
    private final EncounterGenerator gen;
    private final Map<String, Encounter> encounterCache;

    public EncounterGeneratorCmd(ExecutorService executorService) {
        this.executorService = executorService;
        this.difficulties = new String[]{"easy", "medium", "difficult", "deadly"};
        this.environments = new String[]{"city", "dungeon", "forest", "nature", "other plane", "underground", "water"};
        this.gen = EncounterGenerator.getInstance();
        this.encounterCache = new HashMap<>();
    }


    @Override
    public List<String> getCommandArgs() {
        return List.of("party size, party level, difficulty, *environment");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String environment;
        if(args.size() > 3) {
            environment = args.get(3);
        } else {
            environment = null;
        }
        Map<EmbedBuilder, Encounter> embedBuilderEncounterMap = buildEncounterEmbed(
                event.getAuthor(), Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)),
                args.get(2), environment);
        EmbedBuilder embed = (EmbedBuilder) embedBuilderEncounterMap.keySet().toArray()[0];
        MessageEmbed build = embed.build();
        event.getChannel().sendMessageEmbeds(build).setActionRows(ActionRow.of(
                Button.primary(event.getAuthor().getId() + ":regenerate", "Regenerate"),
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete"))).queue();
        encounterCache.put(build.getFields().get(0).getValue(), embedBuilderEncounterMap.get(embed));
    }


    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
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

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("generate", "Generate a random encounter for the given inputs.")
                .addOptions(new OptionData(OptionType.INTEGER, "size", "The size of the party.")
                        .setRequired(true)
                        .setRequiredRange(1, 10))
                .addOptions(new OptionData(OptionType.INTEGER, "level", "The average level of the party.")
                        .setRequired(true)
                        .setRequiredRange(1, 20))
                .addOptions(new OptionData(OptionType.STRING, "difficulty", "The difficulty of the encounter.")
                        .setRequired(true)
                        .addChoices(new Command.Choice("easy", "easy"),
                                new Command.Choice("medium", "medium"),
                                new Command.Choice("difficult", "difficult"),
                                new Command.Choice("deadly", "deadly")))
                .addOptions(new OptionData(OptionType.STRING, "environment", "The environment the encounter takes place in.")
                        .setRequired(false)
                        .addChoices(new net.dv8tion.jda.api.interactions.commands.Command.Choice("city", "city"),
                                new Command.Choice("dungeon", "dungeon"),
                                new Command.Choice("forest", "forest"),
                                new Command.Choice("nature", "nature"),
                                new Command.Choice("other plane", "other plane"),
                                new Command.Choice("underground", "underground"),
                                new Command.Choice("water", "water")
                        ));
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if (args.size() < 3) {
            sendMissingArgs(event);
            return false;
        }
        int partySize;
        int partyLevel;
        try {
            partySize = Integer.parseInt(args.get(0));
            partyLevel = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            sendInvalidArgs(event, "Party size and level must both be numbers.");
            logger.error(e.getMessage());
            return false;
        }
        if (partySize < 1 || partySize > 10) {
            sendInvalidArgs(event, "Party size must be a number between 1 and 10");
            return false;
        }
        if (partyLevel < 1 || partyLevel > 20) {
            sendInvalidArgs(event, "Party level must be a number between 1 and 20");
            return false;
        }
        String difficulty = args.get(2);
        if (!(Arrays.asList(difficulties).contains(difficulty.toLowerCase(Locale.ROOT)))) {
            sendInvalidArgs(event, String.format("%s is not a valid difficulty. Please choose one of: " +
                    "%s, %s, %s, %s.", difficulty, difficulties[0], difficulties[1], difficulties[2], difficulties[3]));
            return false;
        }
        String environment;
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
                sendInvalidArgs(event, envError.toString());
                return false;
            }
        }
        return true;
    }

    @NotNull
    private Map<EmbedBuilder, Encounter> buildEncounterEmbed(@NotNull User author, int partySize, int partyLevel,
                                                             String difficulty, String environment) {
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

    public void regenerateEncounter(@NotNull ButtonClickEvent event) {
        MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        EmbedBuilder newEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(newEmbed, event.getUser());
        newEmbed.setTitle(embed.getTitle());
        String description = embed.getDescription();
        newEmbed.setDescription(description);

        Encounter encounter;
        String oldDescription = embed.getFields().get(0).getValue();
        encounter = encounterCache.get(oldDescription);
        MessageEmbed build;

        if (encounter == null) {
            newEmbed.setDescription("Something went wrong!");
            event.getHook().editOriginalEmbeds(newEmbed.build()).queue();
        } else {
            Encounter newEncounter = gen.generateEncounter(encounter.getPartySize(), encounter.getPartyLevel(),
                    encounter.getDifficulty(), encounter.getEnvironment());
            newEmbed.addField("Encounter", newEncounter.toString(), false);
            build = newEmbed.build();
            encounterCache.remove(oldDescription);
            encounterCache.put(build.getFields().get(0).getValue(), newEncounter);
            event.getHook().editOriginalEmbeds(newEmbed.build()).queue();
        }
    }

}
