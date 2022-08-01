package commands.morbconomy;

import commands.Command;
import database.DatabaseManager;
import database.queries.UsersTableQueries;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class WalletCmd extends Command implements MorbconomyCmd {

	private final DatabaseManager dbManager;

	public WalletCmd() {
		this.commandName = "wallet";
		this.commandDescription = "Check your wallet.";
		this.dbManager = DatabaseManager.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String authorId = event.getAuthor().getId();

		ArrayList<String> query = dbManager.query(UsersTableQueries.getUserCurrency, DatabaseManager.QueryTypes.RETURN, authorId);
		BigInteger wallet = new BigInteger(query.get(0));

		event.getChannel().sendMessage(String.format("You have `%d` morbcoins in your wallet.", wallet)).queue();
	}
}
