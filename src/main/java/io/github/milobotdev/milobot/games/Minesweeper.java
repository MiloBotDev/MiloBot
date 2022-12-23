package io.github.milobotdev.milobot.games;

import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

public class Minesweeper {

    private final Logger logger = LoggerFactory.getLogger(Minesweeper.class);
    private Map<String, BufferedImage> images;
    public static final int ROWS = 9;
    public static final int COLS = 9;
    public static final int MINES = 10;
    private static final ArrayList<Minesweeper> minesweeperGames = new ArrayList<>();
    private final char[][] board;
    private final boolean[][] mines;
    private final User author;
    private final TimeTracker tracker;

    public Minesweeper(@NotNull User author) throws URISyntaxException {
        this.author = author;
        board = new char[ROWS][COLS];
        mines = new boolean[ROWS][COLS];
        this.loadAssets();
        this.initBoard();
        this.initMines();
        minesweeperGames.add(this);
        author.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(generateHelp().build()).queue());
        this.tracker = new TimeTracker();
        this.tracker.start();

    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            new ArrayList<>(minesweeperGames).forEach(game -> game.onMessage(event));
        }
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        if (this.author.getIdLong() != event.getAuthor().getIdLong() || event.isFromGuild()) {
            return;
        }
        ArrayList<String> receivedMessage = new ArrayList<>(Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).toList());
        if (receivedMessage.get(0).equalsIgnoreCase("check")) {
            try {
                int row = Integer.parseInt(receivedMessage.get(1));
                int col = Integer.parseInt(receivedMessage.get(2));
                if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
                    event.getChannel().sendMessage("Invalid coordinates").queue();
                    return;
                }
                boolean hitMine = this.reveal(row, col);
                if (hitMine) {
                    event.getChannel().sendMessage("You hit a mine!").queue();
                    minesweeperGames.remove(this);
                } else {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Minesweeper");
                    embedBuilder.setImage("attachment://minesweeper.png");
                    event.getChannel().sendMessageEmbeds(embedBuilder.build()).addFile(boardToPng(), "minesweeper.png").queue();
                }
            } catch (NumberFormatException | IOException e) {
                event.getChannel().sendMessage("Invalid coordinates").queue();
            }
        }
        if (receivedMessage.get(0).equalsIgnoreCase("flag")) {
            try {
                int row = Integer.parseInt(receivedMessage.get(1));
                int col = Integer.parseInt(receivedMessage.get(2));
                this.flag(row, col);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Minesweeper");
                embedBuilder.setImage("attachment://minesweeper.png");
                event.getChannel().sendMessageEmbeds(embedBuilder.build()).addFile(boardToPng(), "minesweeper.png").queue();
            } catch (NumberFormatException | IOException e) {
                event.getChannel().sendMessage("Invalid coordinates").queue();
            }
        }
        if(receivedMessage.get(0).equalsIgnoreCase("help")) {
            EmbedBuilder generateHelp = generateHelp();
            event.getChannel().sendMessageEmbeds(generateHelp.build()).queue();
        }
        boolean hasWon = checkWin();
        if (hasWon) {
            event.getChannel().sendMessage("You won!").queue();
            minesweeperGames.remove(this);
        }

    }

    private @NotNull EmbedBuilder generateHelp() {
        EmbedBuilder explanation = new EmbedBuilder();
        explanation.setTitle("Minesweeper Help");
        explanation.setTimestamp(new Date().toInstant());
        explanation.setColor(Color.blue);
        explanation.addField("check x y", "Reveals the tile at (x, y) on the board. If the tile contains a mine, the game is over. Otherwise, the board is updated and the game continues.", false);
        explanation.addField("flag x y", "Flags the tile at (x, y) on the board. Flagged tiles are marked with an 'F' on the board.", false);
        return explanation;
    }

    // initialize the board with all empty spaces
    private void initBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public void flag(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            logger.error("Invalid coordinates");
            return;
        }
        if (board[row][col] == 'F') {
            board[row][col] = ' ';
        } else {
            board[row][col] = 'F';
        }
    }

    // initialize the mines randomly on the board
    private void initMines() {
        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < MINES) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);
            if (!mines[row][col]) {
                mines[row][col] = true;
                minesPlaced++;
            }
        }
    }

    // get the number of mines surrounding a cell
    private int getSurroundingMines(int row, int col) {
        int mines = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && i < ROWS && j >= 0 && j < COLS && this.mines[i][j]) {
                    mines++;
                }
            }
        }
        return mines;
    }

    // reveal a cell on the board
    public boolean reveal(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            // invalid input
            return false;
        }
        if (board[row][col] != ' ') {
            // cell already revealed
            return false;
        }
        if (mines[row][col]) {
            // hit a mine, game over
            board[row][col] = '*';
            return true;
        } else {
            // reveal the cell and any surrounding empty cells
            revealEmpty(row, col);
            return false;
        }
    }

    // reveal all empty cells surrounding a cell
    private void revealEmpty(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            // invalid input
            return;
        }
        if (board[row][col] != ' ') {
            // cell has already been revealed
            return;
        }
        int mines = getSurroundingMines(row, col);
        if (mines > 0) {
            // cell has surrounding mines, just reveal it
            board[row][col] = (char) ('0' + mines);
            return;
        }
        // cell has no surrounding mines, reveal it and all empty cells around it
        board[row][col] = '0';
        revealEmpty(row - 1, col);
        revealEmpty(row + 1, col);
        revealEmpty(row, col - 1);
        revealEmpty(row, col + 1);
    }

    // check if the player has won the game
    private boolean checkWin() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (mines[i][j]) {
                    if (board[i][j] != 'F') {
                        return false;
                    }
                } else {
                    if (board[i][j] == ' ') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // main game loop
    public InputStream boardToPng() throws IOException {
        BufferedImage image = new BufferedImage((COLS + 1) * 32, (ROWS + 1) * 32, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        char[][] gridToDraw = addColumnIndexes(this.board);

        for (int i = 0; i < ROWS + 1; i++) {
            for (int j = 0; j < COLS + 1; j++) {
                char c = gridToDraw[i][j];
                int x = (j) * 32;
                int y = (i) * 32;
                if (c == ' ') {
                    g.drawImage(images.get("TileUnknown.png"), x, y, 32, 32, null);
                } else if (c == '*') {
                    g.drawImage(images.get("TileMine.png"), x, y, 32, 32, null);
                } else if (i == 0 || j == 0) {
                    // Draw the number or column index using the drawString() method
                    if (i == 0 && j == 0) {
                        g.drawImage(images.get("TileExploded.png"), x, y, 32, 32, null);
                    } else {
                        g.drawImage(images.get(c + ".png"), x, y, 32, 32, null);
                    }
                } else if (c == 'F') {
                    g.drawImage(images.get("TileFlag.png"), x, y, 32, 32, null);
                } else {
                    if (c == '0') {
                        g.drawImage(images.get("TileEmpty.png"), x, y, 32, 32, null);
                    } else {
                        g.drawImage(images.get("Tile" + c + ".png"), x, y, 32, 32, null);
                    }

                }
            }
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    private char[] @NotNull [] addColumnIndexes(char[] @NotNull [] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        char[][] newGrid = new char[rows + 1][cols + 1];

        // Add the column indexes to the first row and column
        for (int i = 0; i < cols; i++) {
            newGrid[0][i + 1] = (char) ('0' + i);
        }
        for (int i = 0; i < rows; i++) {
            newGrid[i + 1][0] = (char) ('0' + i);
        }

        // Copy the original grid into the new grid
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, newGrid[i + 1], 1, cols);
        }

        return newGrid;
    }

    private void loadAssets() throws URISyntaxException {
        // Create a map to store the images
        Map<String, BufferedImage> images = new HashMap<>();

        // Get the directory where the images are stored
        File dir = new File(getClass().getClassLoader().getResource("minesweeper").toURI());

        // Check if the directory exists
        if (!dir.exists()) {
            logger.error("Directory does not exist: " + dir.getAbsolutePath());
            return;
        }

        // Check if the directory is a directory
        if (!dir.isDirectory()) {
            logger.error("Error: The file is not a directory");
            return;
        }

        // Get a list of all files in the directory
        File[] files = dir.listFiles();

        // Load each image and add it to the map
        for (File file : files) {
            try {
                if (!file.isDirectory()) {
                    BufferedImage image = ImageIO.read(file);
                    images.put(file.getName(), image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.images = images;
    }

}
