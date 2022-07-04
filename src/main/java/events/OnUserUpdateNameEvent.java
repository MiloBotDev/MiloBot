package events;

import database.DatabaseManager;
import database.queries.UserTableQueries;
import events.guild.OnGuildLeaveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * An event triggered when a user updates their name.
 * This event checks if that user is in our database, and in that case updates the name.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class OnUserUpdateNameEvent extends ListenerAdapter {

	final static Logger logger = LoggerFactory.getLogger(OnGuildLeaveEvent.class);
	private final DatabaseManager manager;

	public OnUserUpdateNameEvent() {
		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void onUserUpdateName(@Nonnull UserUpdateNameEvent event) {
		String newName = event.getNewName();
		String oldName = event.getOldName();
		String userId = event.getUser().getId();
		manager.query(UserTableQueries.updateUserName, DatabaseManager.QueryTypes.UPDATE, newName, userId);
		logger.info(String.format("%s changed their name to: %s.", oldName, newName));
	}
}
