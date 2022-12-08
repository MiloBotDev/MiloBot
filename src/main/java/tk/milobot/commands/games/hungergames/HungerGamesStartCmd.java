package tk.milobot.commands.games.hungergames;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.database.dao.HungerGamesDao;
import tk.milobot.database.dao.UserDao;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import tk.milobot.games.hungergames.HungerGames;
import tk.milobot.games.hungergames.model.Item;
import tk.milobot.games.hungergames.model.LobbyEntry;
import tk.milobot.games.hungergames.model.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.lobby.BotLobby;

import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HungerGamesStartCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(HungerGamesStartCmd.class);
    private static final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();
    private static final UserDao userDao = UserDao.getInstance();

    public HungerGamesStartCmd() {
        this.commandName = "start";
        this.aliases = new String[]{"s", "host"};
        this.commandArgs = new String[]{"*maxPlayers"};
        this.commandDescription = "Starts the Hunger Games";
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
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
        if(args.size() > 0) {
            try {
                maxPlayers = Integer.parseInt(args.get(0));
                if(maxPlayers < 2 || maxPlayers > 8) {
                    event.getChannel().sendMessage("maxPlayers must be a number between 2 and 8.").queue();
                }  else {
                    hungerGamesLobby.setMaxPlayers(maxPlayers);
                    hungerGamesLobby.initialize(event.getChannel());
                }
            } catch (NumberFormatException e) {
                logger.error("Failed formatting argument to number when setting the max players for hg lobby ", e);
                event.getChannel().sendMessage("maxPlayers must be a number between 2 and 8.").queue();
            }
        } else {
            hungerGamesLobby.initialize(event.getChannel());
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {

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
            if(!player.isBot()) {
                try (Connection con = DatabaseConnection.getConnection()) {
                    con.setAutoCommit(false);
                    tk.milobot.database.model.HungerGames hungerGamesDaoByUserDiscordId = hungerGamesDao.getByUserDiscordId(con, player.getUserId(), RowLockType.FOR_UPDATE);
                    if (hungerGamesDaoByUserDiscordId == null) {
                        tk.milobot.database.model.HungerGames hungerGames = new tk.milobot.database.model.HungerGames(Objects.requireNonNull(userDao.getUserByDiscordId(con, player.getUserId(), RowLockType.NONE)).getId());
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

    private static void updateHungerGamesDb(@NotNull Player player, tk.milobot.database.model.HungerGames hungerGames) {
        if(player.isWinner()) {
            hungerGames.addGame(tk.milobot.database.model.HungerGames.HungerGamesResult.WIN,
                    player.getKills(), player.getDamageDone(), player.getDamageTaken(), player.getHealingDone(), player.getItemsCollected());
        } else {
            hungerGames.addGame(tk.milobot.database.model.HungerGames.HungerGamesResult.LOSS,
                    player.getKills(), player.getDamageDone(), player.getDamageTaken(), player.getHealingDone(), player.getItemsCollected());
        }
    }

}

