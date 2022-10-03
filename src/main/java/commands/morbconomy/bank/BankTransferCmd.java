package commands.morbconomy.bank;

import commands.Command;
import commands.SubCmd;
import database.dao.UserDao;
import database.model.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            User userByDiscordId = userDao.getUserByDiscordId(author.getIdLong());
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
        } catch (SQLException e) {
            logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
            return;
        }
        final long[] transferDiscordId = new long[1];
        try {
            transferDiscordId[0] = Long.parseLong(args.get(1));
            transferMorbcoins(event, transferDiscordId[0], amount, author);
        } catch (NumberFormatException e) {
            String name = args.get(1);
            event.getGuild().loadMembers(member -> {
                net.dv8tion.jda.api.entities.User user = member.getUser();
                if (name.equalsIgnoreCase(user.getName())) {
                    transferDiscordId[0] = user.getIdLong();
                    transferMorbcoins(event, transferDiscordId[0], amount, author);
                }
            }).onSuccess(unused -> {
                if(transferDiscordId[0] == 0) {
                    event.getChannel().sendMessage("Unable to find user to transfer to.").queue();
                }
            });
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        int amount = (int) Objects.requireNonNull(event.getOption("amount")).getAsLong();
        net.dv8tion.jda.api.entities.User user = Objects.requireNonNull(event.getOption("user")).getAsUser();
        try {
            User userByDiscordId = userDao.getUserByDiscordId(event.getUser().getIdLong());
            int currency = userByDiscordId.getCurrency();
            if (amount > currency) {
                event.reply("You can't transfer more morbcoins then you own.").queue();
                return;
            }
        } catch (SQLException e) {
            logger.error("Error while trying to load a user by its discord id at bank transfer command.", e);
            return;
        }
        transferMorbcoins(event, user.getIdLong(), amount, event.getUser());
    }

    private void transferMorbcoins(@NotNull Event event, long transferDiscordId, int amount, net.dv8tion.jda.api.entities.User user) {
        try {
            User userToTransferTo = userDao.getUserByDiscordId(transferDiscordId);
            if (userToTransferTo == null) {
                if(event instanceof MessageReceivedEvent) {
                    ((MessageReceivedEvent)event).getChannel().sendMessage("Unable to find user to transfer to.").queue();
                } else if(event instanceof SlashCommandEvent) {
                    ((SlashCommandEvent)event).reply("Unable to find user to transfer to.").queue();
                }
            } else {
                User userToTransferFrom = userDao.getUserByDiscordId(user.getIdLong());
                userToTransferFrom.setCurrency(userToTransferFrom.getCurrency() - amount);
                userDao.update(userToTransferFrom);
                userToTransferTo.setCurrency(userToTransferTo.getCurrency() + amount);
                userDao.update(userToTransferTo);
                if(event instanceof MessageReceivedEvent) {
                    ((MessageReceivedEvent)event).getChannel().sendMessage(String.format("Successfully sent `%d` morbcoins.", amount)).queue();
                } else if(event instanceof SlashCommandEvent) {
                    ((SlashCommandEvent)event).reply(String.format("Successfully sent `%d` morbcoins.", amount)).queue();
                }
            }
        } catch (SQLException e) {
            if(event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent)event).getChannel().sendMessage("Unable to find user to transfer to.").queue();
            } else if(event instanceof SlashCommandEvent) {
                ((SlashCommandEvent)event).reply("Unable to find user to transfer to.").queue();
            }
        }
    }
}
