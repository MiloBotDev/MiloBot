package io.github.milobotdev.milobot.utility;

import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for handling all actions with the GitHub api.
 * This class is a singleton.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class GitHubBot {

    final static Logger logger = LoggerFactory.getLogger(GitHubBot.class);

    private static GitHubBot instance;

    private GitHub gitHub;
    private GHRepository repository;

    private GitHubBot() {
        try {
            Config config = Config.getInstance();
            this.gitHub = new GitHubBuilder().withOAuthToken(config.getPersonalAccessToken()).build();
            setRepository(config.getRepositoryName());
            logger.trace(String.format("Created a %s instance.", this.getClass().getName()));
        } catch (IOException e) {
            logger.error(String.format("Failed to create a %s instance.", this.getClass().getName()), e);
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
            logger.trace(String.format("Loaded repository: %s", repoName));
        } catch (IOException e) {
            logger.error(String.format("Failed to load repository: %s.", repoName), e);
        }
    }

    /**
     * Adds a bug to the issue tracker on the GitHub repository.
     *
     * @return The url of the created issue.
     */
    public String createBugIssue(String title, String reproduce, String severity, String additionalInfo, String authorName,
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
            logger.trace(String.format("Issue created with title: %s", title));

            List<GHIssue> issues = this.repository.getIssues(GHIssueState.ALL);
            for (GHIssue _issue : issues) {
                if (_issue.getTitle().equals(title)) {
                    return _issue.getHtmlUrl().toString();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to create issue.", e);
        }
        return "";
    }

    /**
     * Returns a bug by its issue number.
     *
     * @return an Optional of GHIssue.
     */
    public Optional<GHIssue> getBug(int issueNumber) {
        try {
            List<GHIssue> issues = this.repository.getIssues(GHIssueState.OPEN);
            for (GHIssue issue : issues) {
                Collection<GHLabel> labels = issue.getLabels();
                for (GHLabel label : labels) {
                    if (label.getName().equals("bug")) {
                        if (issueNumber == issue.getNumber()) {
                            return Optional.of(issue);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load issues.", e);
        }
        return Optional.empty();
    }

    /**
     * Loads all issues labeled as a bug.
     *
     * @return A list of bugs as ArrayList<GHIssue>.
     */
    public ArrayList<GHIssue> getAllBugs() {
        ArrayList<GHIssue> bugs = new ArrayList<>();
        try {
            List<GHIssue> issues = this.repository.getIssues(GHIssueState.OPEN);
            for (GHIssue issue : issues) {
                Collection<GHLabel> labels = issue.getLabels();
                for (GHLabel label : labels) {
                    if (label.getName().equals("bug")) {
                        bugs.add(issue);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load issues.", e);
        }
        return bugs;
    }

}
