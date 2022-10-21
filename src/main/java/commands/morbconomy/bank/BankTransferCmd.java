package commands.morbconomy.bank;

import commands.Command;
import commands.SubCmd;
import database.dao.UserDao;
import database.model.User;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class BankTransferCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(BankTransferCmd.class);
    private static final UserDao userDao = UserDao.getInstance();

    public BankTransferCmd() {
        this.commandName = "transfer";
        this.commandArgs = new String[]{"amount", "player"};
        this.aliases = new String[]{"send"};
        this.commandDescription = "Send some morbcoins to another user.";
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription)
                .addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "The amount of morbcoins you want to send", true)
                                .setRequiredRange(1, 10000),
                        new OptionData(OptionType.USER, "user", "The user you want to send morbcoins to.", true)
                );
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        int amount;
        try {
            amount = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(String.format("`%s` is not a valid amount of morbcoins.", args.get(0))).queue();
            return;
        }
        net.dv8tion.jda.api.entities.User author = event.getAuthor();
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            User userByDiscordId = userDao.getUserByDiscordId(con, author.getIdLong(), RowLockType.NONE);
            int currency = userByDiscordId.getCurrency();
            if (amount > currency) {
                event.getChannel().sendMessage("You can't transfer more morbcoins then you own.").queue();
                return;
            } else if (amount > 10000){
                event.getChannel().sendMessage("You can't transfer more then 10000 morbcoins.").queue();
                return;
            } else if (amount < 0 || amount == 0) {
                event.getChannel().sendMessage("You can't transfer 0 or a negative amount of morbcoins").queue();
                return;
            }
            con.commit();
        } catch (SQLException e) {
            logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
            return;
        }
        final long[] transferDiscordId = new long[1];
        try(Connection con = DatabaseConnection.getConnection()) {
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
                    try(Connection con = DatabaseConnection.getConnection()) {
                        con.setAutoCommit(false);
                        transferMorbcoins(event, transferDiscordId[0], amount, author,con);
                        con.commit();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }
            }).onSuccess(unused -> {
                if(transferDiscordId[0] == 0) {
                    event.getChannel().sendMessage("Unable to find user to transfer to.").queue();
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        int amount = (int) Objects.requireNonNull(event.getOption("amount")).getAsLong();
        net.dv8tion.jda.api.entities.User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        try(Connection con = DatabaseConnection.getConnection()) {
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

    private void transferMorbcoins(@NotNull Event event, long transferDiscordId, int amount,
                                   net.dv8tion.jda.api.entities.User user, Connection con) throws SQLException {
        User userToTransferTo = userDao.getUserByDiscordId(con, transferDiscordId, RowLockType.FOR_UPDATE);
        if (userToTransferTo == null) {
            if(event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent)event).getChannel().sendMessage("Unable to find user to transfer to.").queue();
            } else if(event instanceof SlashCommandEvent) {
                ((SlashCommandEvent)event).reply("Unable to find user to transfer to.").queue();
            }
        } else {
            User userToTransferFrom = userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE);
            userToTransferFrom.setCurrency(userToTransferFrom.getCurrency() - amount);
            userDao.update(con, userToTransferFrom);
            userToTransferTo.setCurrency(userToTransferTo.getCurrency() + amount);
            userDao.update(con, userToTransferTo);
            if(event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent)event).getChannel().sendMessage(String.format("Successfully sent `%d` morbcoins.", amount)).queue();
            } else if(event instanceof SlashCommandEvent) {
                ((SlashCommandEvent)event).reply(String.format("Successfully sent `%d` morbcoins.", amount)).queue();
            }
        }
    }
}
