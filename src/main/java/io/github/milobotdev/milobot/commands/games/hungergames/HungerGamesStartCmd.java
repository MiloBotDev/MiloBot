package io.github.milobotdev.milobot.commands.games.hungergames;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.model.CantCreateLobbyException;
import io.github.milobotdev.milobot.database.dao.HungerGamesDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.games.hungergames.HungerGames;
import io.github.milobotdev.milobot.games.hungergames.model.Item;
import io.github.milobotdev.milobot.games.hungergames.model.LobbyEntry;
import io.github.milobotdev.milobot.games.hungergames.model.Player;
import io.github.milobotdev.milobot.utility.lobby.BotLobby;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HungerGamesStartCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, Aliases {

    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(HungerGamesStartCmd.class);
    private static final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();
    private static final UserDao userDao = UserDao.getInstance();

    public HungerGamesStartCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("start", "Starts the Hunger Games")
                .addOptions(new OptionData(OptionType.INTEGER, "max-players", "Maximum number of players", false)
                        .setRequiredRange(2, 8));
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("s", "host");
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("*maxPlayers");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if (args.size() > 0) {
            try {
                int maxPlayers = Integer.parseInt(args.get(0));
                if (maxPlayers < 2 || maxPlayers > 8) {
                    event.getChannel().sendMessage("maxPlayers must be a number between 2 and 8.").queue();
                    return false;
                } else {
                    return true;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        try {
            User author = event.getAuthor();
            int maxPlayers = 8;
            BotLobby hungerGamesLobby = new BotLobby("Hunger Games Lobby", author,
                    (entries, message) -> {
                        ArrayList<LobbyEntry> participants = new ArrayList<>();
                        entries.forEach((players, npcs) -> {
                            npcs.forEach(npc -> participants.add(new LobbyEntry(npc.getName())));
                            players.forEach(user -> participants.add(new LobbyEntry(user.getIdLong(), user.getName(), user.getAsMention())));
                        });
                        HungerGames game = new HungerGames(participants);
                        game.startGame();
                        HungerGamesStartCmd.runGame(event.getChannel(), game);
                    }, 2, maxPlayers);
            if (args.size() > 0) {
                maxPlayers = Integer.parseInt(args.get(0));
                hungerGamesLobby.setMaxPlayers(maxPlayers);
                hungerGamesLobby.initialize(event.getChannel());
            } else {
                hungerGamesLobby.initialize(event.getChannel());
            }
        } catch (CantCreateLobbyException e) {
            event.getMessage().reply("You can't create a new lobby when you are already in one.").queue();
        }

    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        try {
            event.deferReply().queue();
            User author = event.getUser();
            int maxPlayers = 8;
            if (event.getOption("max-players") != null) {
                maxPlayers = Integer.parseInt(String.valueOf(Objects.requireNonNull(event.getOption("max-players")).getAsLong()));
            }
            BotLobby hungerGamesLobby = new BotLobby("Hunger Games Lobby", author,
                    (entries, message) -> {
                        ArrayList<LobbyEntry> participants = new ArrayList<>();
                        entries.forEach((players, npcs) -> {
                            npcs.forEach(npc -> participants.add(new LobbyEntry(npc.getName())));
                            players.forEach(user -> participants.add(new LobbyEntry(user.getIdLong(), user.getName(), user.getAsMention())));
                        });
                        HungerGames game = new HungerGames(participants);
                        game.startGame();
                        HungerGamesStartCmd.runGame(event.getChannel(), game);
                    }, 2, maxPlayers);
            hungerGamesLobby.initialize(event);
        } catch (CantCreateLobbyException e) {
            event.reply("You can't create a new lobby when you are already in one.").queue();
        }

    }


    public static void runGame(MessageChannel channel, @NotNull HungerGames game) {
        Map<Integer, Map<List<String>, List<Player>>> roundData = game.getRoundData();
        List<RestAction<Void>> messages = new ArrayList<>();

        roundData.forEach((key1, value1) -> {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.BLUE);
            embed.setTimestamp(new Date().toInstant());
            embed.setTitle("Round " + key1);

            StringBuilder logs = new StringBuilder();
            value1.forEach((key, value) -> {
                for (String s : key) {
                    logs.append(s).append("\n");
                }

                for (Player player : value) {
                    StringBuilder playerDesc = new StringBuilder();
                    playerDesc.append("**Health:** ").append(player.getHealth());
                    playerDesc.append("\n**Inventory:** ");
                    List<Item> inventory = player.getInventory();
                    for (int i = 0; i < inventory.size(); i++) {
                        playerDesc.append(inventory.get(i).getName());
                        if (i != inventory.size() - 1) {
                            playerDesc.append(", ");
                        }
                    }
                    embed.addField(player.getUserName(), playerDesc.toString(), true);
                }
            });
            embed.setDescription(logs.toString());

            RestAction<Void> voidRestAction = channel.sendMessageEmbeds(embed.build())
                    .delay(15, TimeUnit.SECONDS)
                    .flatMap(Message::delete);
            messages.add(voidRestAction);
        });

        Timer timer = new Timer();
        TimerTask sendMessages = new TimerTask() {
            @Override
            public void run() {
                messages.forEach(messageAction -> {
                    messageAction.queue();
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                EmbedBuilder embedBuilder = HungerGamesStartCmd.generateRecapEmbed(game);
                channel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        };
        timer.schedule(sendMessages, 0);
    }

    private static @NotNull EmbedBuilder generateRecapEmbed(@NotNull HungerGames game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Hunger Games Recap");
        embed.setColor(Color.BLUE);
        embed.setTimestamp(new Date().toInstant());
        embed.setDescription("**Winner:** " + game.getWinner().getUserName());

        List<Player> players = game.getPlayers();
        for (Player player : players) {
            if (!player.isBot()) {
                try (Connection con = DatabaseConnection.getConnection()) {
                    con.setAutoCommit(false);
                    io.github.milobotdev.milobot.database.model.HungerGames hungerGamesDaoByUserDiscordId = hungerGamesDao.getByUserDiscordId(con, player.getUserId(), RowLockType.FOR_UPDATE);
                    if (hungerGamesDaoByUserDiscordId == null) {
                        io.github.milobotdev.milobot.database.model.HungerGames hungerGames = new io.github.milobotdev.milobot.database.model.HungerGames(Objects.requireNonNull(userDao.getUserByDiscordId(con, player.getUserId(), RowLockType.NONE)).getId());
                        hungerGamesDao.add(con, hungerGames);
                        updateHungerGamesDb(player, hungerGames);
                        hungerGamesDaoByUserDiscordId = hungerGames;
                    } else {
                        updateHungerGamesDb(player, hungerGamesDaoByUserDiscordId);
                    }
                    hungerGamesDao.update(con, hungerGamesDaoByUserDiscordId);
                    con.commit();
                } catch (Exception e) {
                    logger.error("Error creating hungergames entry for user in database when user wanted to play hungergames.", e);
                }
            }
            int damageDone = player.getDamageDone();
            int kills = player.getKills();
            int healingDone = player.getHealingDone();
            int itemsCollected = player.getItemsCollected();
            int damageTaken = player.getDamageTaken();

            String playerDesc = "*Kills:* " + kills + "\n" +
                    "*Damage Done:* " + damageDone + "\n" +
                    "*Damage Taken:* " + damageTaken + "\n" +
                    "*Healing Done:* " + healingDone + "\n" +
                    "*Items Collected:* " + itemsCollected + "\n";

            embed.addField(player.getUserName(), playerDesc, true);
        }

        return embed;
    }

    private static void updateHungerGamesDb(@NotNull Player player, io.github.milobotdev.milobot.database.model.HungerGames hungerGames) {
        if (player.isWinner()) {
            hungerGames.addGame(io.github.milobotdev.milobot.database.model.HungerGames.HungerGamesResult.WIN,
                    player.getKills(), player.getDamageDone(), player.getDamageTaken(), player.getHealingDone(), player.getItemsCollected());
        } else {
            hungerGames.addGame(io.github.milobotdev.milobot.database.model.HungerGames.HungerGamesResult.LOSS,
                    player.getKills(), player.getDamageDone(), player.getDamageTaken(), player.getHealingDone(), player.getItemsCollected());
        }
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.TEXT);
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

}

