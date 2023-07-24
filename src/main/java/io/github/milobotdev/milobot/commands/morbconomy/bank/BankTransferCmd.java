package io.github.milobotdev.milobot.commands.morbconomy.bank;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.SlashCommand;
import io.github.milobotdev.milobot.commands.command.extensions.TextCommand;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.User;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BankTransferCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes {

    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(BankTransferCmd.class);
    private static final UserDao userDao = UserDao.getInstance();

    public BankTransferCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("transfer", "Send some morbcoins to another user.").addOptions(
                new OptionData(OptionType.INTEGER, "amount", "The amount of morbcoins you want to send", true)
                        .setRequiredRange(1, 10000),
                new OptionData(OptionType.USER, "user", "The user you want to send morbcoins to.", true)
        );
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("amount", "user");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        int amount;
        try {
            amount = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(String.format("`%s` is not a valid amount of morbcoins.", args.get(0))).queue();
            return false;
        }
        net.dv8tion.jda.api.entities.User author = event.getAuthor();
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            User userByDiscordId = userDao.getUserByDiscordId(con, author.getIdLong(), RowLockType.NONE);
            int currency = userByDiscordId.getCurrency();
            if (amount > currency) {
                event.getChannel().sendMessage("You can't transfer more morbcoins then you own.").queue();
                return false;
            } else if (amount > 10000) {
                event.getChannel().sendMessage("You can't transfer more then 10000 morbcoins.").queue();
                return false;
            } else if (amount < 0 || amount == 0) {
                event.getChannel().sendMessage("You can't transfer 0 or a negative amount of morbcoins").queue();
                return false;
            }
            con.commit();
        } catch (SQLException e) {
            logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
            return false;
        }
        return true;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        int amount = Integer.parseInt(args.get(0));
        net.dv8tion.jda.api.entities.User author = event.getAuthor();
        final long[] transferDiscordId = new long[1];
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            transferDiscordId[0] = Long.parseLong(args.get(1));
            transferMorbcoins(event, transferDiscordId[0], amount, author, con);
            con.commit();
        } catch (NumberFormatException e) {
            String name = args.get(1);
            event.getGuild().loadMembers(member -> {
                net.dv8tion.jda.api.entities.User user = member.getUser();
                if (name.equalsIgnoreCase(user.getName())) {
                    transferDiscordId[0] = user.getIdLong();
                    try (Connection con = DatabaseConnection.getConnection()) {
                        con.setAutoCommit(false);
                        transferMorbcoins(event, transferDiscordId[0], amount, author, con);
                        con.commit();
                    } catch (SQLException ex) {
                        logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
                    }

                }
            }).onSuccess(unused -> {
                if (transferDiscordId[0] == 0) {
                    event.getChannel().sendMessage("Unable to find user to transfer to.").queue();
                }
            });
        } catch (SQLException e) {
            logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
        }
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        int amount = (int) Objects.requireNonNull(event.getOption("amount")).getAsLong();
        net.dv8tion.jda.api.entities.User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            User userByDiscordId = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
            int currency = userByDiscordId.getCurrency();
            if (amount > currency) {
                event.reply("You can't transfer more morbcoins then you own.").queue();
                return;
            }
            transferMorbcoins(event, user.getIdLong(), amount, event.getUser(), con);
            con.commit();
        } catch (SQLException e) {
            logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
        }
    }

    private void transferMorbcoins(@NotNull Event event, long userToTransferToDiscordId, int amount,
                                   net.dv8tion.jda.api.entities.User user, Connection con) throws SQLException {
        User userToTransferTo = userDao.getUserByDiscordId(con, userToTransferToDiscordId, RowLockType.FOR_UPDATE);
        if (userToTransferTo == null) {
            if (event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent) event).getChannel().sendMessage("Unable to find user to transfer to.").queue();
            } else if (event instanceof SlashCommandInteractionEvent) {
                ((SlashCommandInteractionEvent) event).reply("Unable to find user to transfer to.").queue();
            }
        } else {
            User userToTransferFrom = userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE);
            userToTransferFrom.setCurrency(userToTransferFrom.getCurrency() - amount);
            userDao.update(con, userToTransferFrom);
            userToTransferTo.setCurrency(userToTransferTo.getCurrency() + amount);
            userDao.update(con, userToTransferTo);
            final Message[] loadingMessage = new Message[1];
            Consumer<Message> sentMorbcoinsConsumer = message -> {
                loadingMessage[0] = message;
                event.getJDA().retrieveUserById(userToTransferToDiscordId).queue(user1 -> {
                    String userToTransferToAsMention = user1.getAsMention();
                    loadingMessage[0].editMessage(String.format("Successfully sent `%d` morbcoin(s) to %s",
                            amount, userToTransferToAsMention)).queue();
                });
            };
            if(event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent) event).getChannel().sendMessage("Attempting to transfer morbcoins.").queue(
                        sentMorbcoinsConsumer);
            } else if(event instanceof SlashCommandInteractionEvent) {
                ((SlashCommandInteractionEvent) event).getHook().sendMessage("Attempting to transfer morbcoins.").queue(
                        sentMorbcoinsConsumer);
            }


        }
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
