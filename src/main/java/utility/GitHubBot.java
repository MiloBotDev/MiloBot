package utility;

import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GitHubBot {

	final static Logger logger = LoggerFactory.getLogger(GitHubBot.class);

	private static GitHubBot instance;

	private GitHub gitHub;
	private GHRepository repository;

	private GitHubBot() {
		try {
			Config config = Config.getInstance();
			this.gitHub = new GitHubBuilder().withOAuthToken(config.personalAccessToken).build();
			setRepository(config.repositoryName);
			logger.info(String.format("Created a %s instance.", this.getClass().getName()));
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.info(String.format("Failed to create a %s instance.", this.getClass().getName()));
		}
	}

	/**
	 * Get the only existing instance of this class.
	 *
	 * @return The instance of this class.
	 */
	public static GitHubBot getInstance() {
		if (instance == null) {
			instance = new GitHubBot();
		}
		return instance;
	}

	/**
	 * Sets the repository to the provided name.
	 */
	public void setRepository(String repoName) {
		try {
			this.repository = gitHub.getRepository(repoName);
			logger.info(String.format("Loaded repository: %s", repoName));
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.info(String.format("Failed to load repository: %s.", repoName));
		}
	}

	/**
	 * Adds a bug to the issue tracker on the GitHub repository.
	 */
	public void createBugIssue(String title, String reproduce, String severity, String additionalInfo, String authorName,
							   String authorId) {
		try {
			GHIssueBuilder issue = this.repository.createIssue(title);
			String body = "## Steps to Reproduce\n" +
					reproduce +
					"\n## Severity\n" +
					severity +
					"\n## Additional Information\n" +
					additionalInfo +
					"\n## Author\n" +
					String.format("`%s`: %s", authorName, authorId);
			issue.body(body);

			issue.label("bug");
			issue.create();
			logger.info(String.format("Issue created with title: %s", title));
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.info("Failed to create issue.");
		}
	}

}
