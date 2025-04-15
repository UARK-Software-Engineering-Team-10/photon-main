package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import edu.uark.team10.table.PlayerEntryTable;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Represents a lazer tag game. Holds member variables for
 * mapping equipment ID to player ID, team number, score, and codename.
 * Also has methods for getting these values.
 */
public class Game {

    // Static variables for convenience
    public static final int BASE_POINTS = 100;
    public static final int TAG_POINTS = 10;
    public static final int RED_TEAM_NUMBER = 53;
    public static final int GREEN_TEAM_NUMBER = 43;

    public static long START_COUNTDOWN = 30L; // 30 seconds default
    public static long GAME_LENGTH = 360L; // default 6 minutes

    public static boolean isTestingMode = false;

    // Used for timestamps on action log
    public Instant startInstant = null;

    // Maps equipment ID to player ID
    private HashMap<Integer, Integer> players = new HashMap<>();
    // Maps equipment ID to team number
    private HashMap<Integer, Integer> playerTeams = new HashMap<>();
    // Maps equipment ID to score
    private HashMap<Integer, Integer> scores = new HashMap<>();
    // Maps equipment ID to codename
    private HashMap<Integer, String> playerCodenames = new HashMap<>();
    // A list of equipment ID who have scored a base
    private ArrayList<Integer> playersWhoScoredBase = new ArrayList<>();

    // An array for the action log. Used for the action display/gameplay screen
    private ArrayList<JLabel> actionLog = new ArrayList<>();

    private boolean isGameStart = false;
    private boolean isGameEnd = false;

    private JLabel countdownTimerLabel;
    private Timer countdownTimer;

    public Game()
    {
        this.playBackgroundMusic("edu/uark/team10/assets/game_sounds/Photon_Track_Loop.mp3", true);
    }

    /**
     * Start the game. Also starts the UDP server.
     * This method can only be called once per game instance.
     * 
     * @param tableRedTeam
     * @param tableGreenTeam
     */
    public void start(PlayerEntryTable tableRedTeam, PlayerEntryTable tableGreenTeam)
    {
        if (this.isGameStart) return; // Only allow this method to be called once

        this.isGameStart = true;

        this.addTeamPlayers(tableRedTeam, Game.RED_TEAM_NUMBER);
        this.addTeamPlayers(tableGreenTeam, Game.GREEN_TEAM_NUMBER);

        // Start the server
        UDPServer.getInstance().start(); // Server extends Thread. Threads can only be started one time

        // Wait a second to make sure the server has started first
        UDPServer.sendMessage("202", 1L, TimeUnit.SECONDS);

        this.startInstant = Instant.now();

        countdownTimerLabel = new JLabel(String.format("%02d:%02d", (int) GAME_LENGTH / 60, (int) GAME_LENGTH % 60), JLabel.CENTER);
        countdownTimerLabel.setFont(new Font("Conthrax SemBd", Font.PLAIN, 24));
        countdownTimerLabel.setForeground(Color.YELLOW);
        countdownTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        countdownTimer = new Timer(1000, new ActionListener() {
            int countdown = (int) GAME_LENGTH;

            @Override
            public void actionPerformed(ActionEvent e) {
                int minutes = countdown / 60;
                int seconds = countdown % 60;
                countdownTimerLabel.setText(String.format("%02d:%02d", minutes, seconds)); // Update the label

                if (countdown <= 0) {
                    ((Timer) e.getSource()).stop();
                    //JOptionPane.showMessageDialog(null, "Game Over!", "Game Status", JOptionPane.INFORMATION_MESSAGE);
                }

                countdown--;
            }
        });
        countdownTimer.start();

        updateActionDisplay("Game start! (" + Game.GAME_LENGTH / 60.0 + " minutes)");

        // End the game after gameLength + startCountdown seconds
        CompletableFuture<Void> gameTimerFuture = new CompletableFuture<Void>().completeOnTimeout(null, Game.GAME_LENGTH + Game.START_COUNTDOWN, TimeUnit.SECONDS);
        gameTimerFuture.whenComplete((none, exception) -> {
            if (exception != null)
            {
                exception.printStackTrace();
            }

            end(); // End the game
        });

    }

    /**
     * Ends the game. Can only be called once per game instance.
     * Can only be called if the game has started.
     */
    public void end()
    {
        if (!this.isGameStart) return; // Cannot be called until the game is started
        if (this.isGameEnd) return; // Cannot be called more than once

        this.isGameEnd = true;

        // End game code
        UDPServer.sendMessage("221"); // Send 3 times according to requirements
        UDPServer.sendMessage("221");
        UDPServer.sendMessage("221");

        // End game code (221) packet takes time to be received
        new CompletableFuture<Void>().completeOnTimeout(null, 1, TimeUnit.SECONDS)
            .whenComplete((none, exception) -> {
                if (exception != null)
                {
                    exception.printStackTrace();
                }

                updateActionDisplay("The game has ended.");

                JOptionPane.showMessageDialog(null, "Click 'OK' to start a new game.", "The game has ended.", JOptionPane.INFORMATION_MESSAGE);
                
                // Stop the server, dispose the old display, and create a new game
                UDPServer.stopServer();
                AppKeyDispatcher.clearKeyActions();
                Application.getInstance().playerEntryScreen();

            });

    }

    public boolean hasStarted()
    {
        return this.isGameStart;
    }

    /**
     * 
     * @param table
     */
    private void addTeamPlayers(PlayerEntryTable table, int teamNumber)
    {
        for (Object[] playerdata : table.getPlayerEntryTableModel().getRowData())
        {
            int equipmentId = Integer.valueOf(String.valueOf(playerdata[0]));
            int playerId = Integer.valueOf(String.valueOf(playerdata[1]));
            String codename = String.valueOf(playerdata[2]);

            this.players.put(equipmentId, playerId);
            this.playerTeams.put(equipmentId, teamNumber);
            this.scores.put(equipmentId, 0);
            this.playerCodenames.put(equipmentId, codename);
        }

    }

    /**
     * Checks the validity of equipmentId by
     * checking if it is a key of the players map
     * or if it is a team base number.
     * Throws an exception if invalid.
     * 
     * @param equipmentId Equipment ID or team number
     */
    private void checkValidity(int equipmentId)
    {
        if (equipmentId == Game.RED_TEAM_NUMBER || equipmentId == Game.GREEN_TEAM_NUMBER) return;

        try {
            if (this.players.get(equipmentId) == null) {
                throw new Exception("Invalid equipment ID: This equipment ID (" + equipmentId + ") is not registered.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Get a list of all the equipment IDs on a team.
     * 
     * @param teamNumber 53 (red) or 43 (green)
     * @return A list of equipment IDs on the team
     */
    private ArrayList<Integer> getEquipmentIdsOnTeam(int teamNumber)
    {
        this.checkValidity(teamNumber);

        ArrayList<Integer> players = new ArrayList<>();

        // Add equipmentId to players list if team numbers match
        this.playerTeams.forEach((equipmentId, teamNum) -> {
            if (teamNum != teamNumber) return;
            players.add(equipmentId);
        });

        return players;

    }

    /**
     * Get the score of the player using their equipmentId
     * 
     * @param equipmentId
     * @return This player's score
     */
    private int getPlayerScore(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.scores.get(equipmentId);
    }

    /**
     * Get the total score of a team
     * 
     * @param teamNumber
     * @return Total score of this team
     */
    private int getTeamScore(int teamNumber)
    {
        this.checkValidity(teamNumber);
        
        return this.getEquipmentIdsOnTeam(teamNumber).stream()
            .mapToInt(this::getPlayerScore) // Convert the array of equipment IDs to an IntStream of player scores
            .sum(); // Sum the player scores
    }

    /**
     * Checks if this equipment ID has scored a base.
     * 
     * @param equipmentId
     * @return True if player scored a base
     */
    private boolean hasPlayerScoredBase(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.playersWhoScoredBase.contains(equipmentId);
    }

    /**
     * Get the team number that has the highest total score
     * @return the team number or -1 if equal
     */
    public int getLeadingTeam()
    {
        int redTeamScore = this.getTeamScore(Game.RED_TEAM_NUMBER);
        int greenTeamScore = this.getTeamScore(Game.GREEN_TEAM_NUMBER);
        
        if (redTeamScore > greenTeamScore)
        {
            return Game.RED_TEAM_NUMBER;
        } else if (redTeamScore < greenTeamScore)
        {
            return Game.GREEN_TEAM_NUMBER;
        }

        return -1; // Scores are equal
    }

    /**
     * Get the team number of the equipmentId. If the equipment ID
     * is a team number, then itself is returned.
     * 
     * @param equipmentId
     * @return 53 (red) or 43 (green)
     */
    public Integer getTeamFromEquipmentId(int equipmentId)
    {
        this.checkValidity(equipmentId);

        // Team number of equipment ID. If the get is null, return the equipment ID (it is a base)
        return Optional.ofNullable(this.playerTeams.get(equipmentId)).orElse(equipmentId);
    }

    /**
     * Get player ID rom the equipment ID
     * 
     * @param equipmentId
     * @return Player ID
     */
    public Integer getPlayerIdFromEquipmentId(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.players.get(equipmentId); // May be null
    }

    /**
     * Get the codename of the player associated with equipmentId.
     * Adds stylized "B" to the beginning of their name if they scored a base.
     * 
     * @param equipmentId
     * @return The player's codename
     */
    public String getCodename(int equipmentId)
    {
        this.checkValidity(equipmentId);

        String codename = this.playerCodenames.get(equipmentId);

        if (hasPlayerScoredBase(equipmentId))
        {
            codename = "[B]".concat(codename);
        }

        return codename;
    }

    /**
     * Add points to the score of the player associated with equipmentId.
     * 
     * @param points Points to be added
     * @param equipmentId Player to add points to
     * @return The new score of this player
     */
    public int addPoints(int points, int equipmentId)
    {
        this.checkValidity(equipmentId);
        int score = this.scores.get(equipmentId) + points;
        this.scores.put(equipmentId, score);

        return score;
    }

    /**
     * Indicate that this player scored a base.
     * 
     * @param equipmentId The equipment ID of the player
     */
    public void setPlayerScoredBase(int equipmentId)
    {
        this.checkValidity(equipmentId);

        this.playersWhoScoredBase.add(equipmentId);
    }

    /**
     * Returns an ordered map of codename to score.
     * Smallest index is biggest score, biggest index is smallest score
     * 
     * @param teamNumber
     * @return A map of codename to score in descending order
     */
    public LinkedHashMap<String, Integer> getTeamPlayerScoresOrdered(int teamNumber)
    {
        this.checkValidity(teamNumber);
        
        
        LinkedHashMap<String, Integer> result = this.getEquipmentIdsOnTeam(teamNumber).stream()
            .sorted(Comparator.comparingInt(this::getPlayerScore).reversed()) // Sort players by score
            .collect(Collectors.toMap(this::getCodename, this::getPlayerScore, (oldValue, newValue) -> oldValue, LinkedHashMap::new)); // Convert the array of equipment IDs to an ordered map of codenames and scores

        return result;
    }

    private void flashJLabel(JLabel label)
    {
        Timer timer = new Timer(200, null);
        timer.addActionListener(new ActionListener() {
            private int counter = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                // Stop timer if the label isn't showing
                if (!label.isShowing())
                {
                    timer.stop();
                    return;
                }
                
                if (counter % 2 == 0)
                {
                    label.setForeground(Color.WHITE);
                } else
                {
                    label.setForeground(Color.YELLOW);
                }

                counter++;
            }
            
        });

        timer.start();

    }

    /**
     * Draws and updates the action display with the current players, scores, and action messages
     * 
     * @param newActionMessage A message action to add to the log. May be null.
     */
    public void updateActionDisplay(String newActionMessage)
    {
        final Application actionDisplay = Application.getInstance();

        actionDisplay.getContentPane().removeAll();
        actionDisplay.revalidate();
        actionDisplay.repaint();

        final Font font = new Font("Conthrax SemBd", Font.PLAIN, 18);

        // Borders
        final Border marginBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        final Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        final Border border = BorderFactory.createCompoundBorder(etchedBorder, marginBorder);

        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setPreferredSize(new Dimension(actionDisplay.getWidth() / 4, actionDisplay.getHeight()));
        logPanel.setBackground(new Color(28, 0, 64));
        logPanel.setForeground(Color.WHITE);
        logPanel.setBorder(border);

        // Top panel for timer and log header
        JPanel logTopPanel = new JPanel();
        logTopPanel.setLayout(new BoxLayout(logTopPanel, BoxLayout.Y_AXIS));
        logTopPanel.setBackground(new Color(28, 0, 64));

        logTopPanel.add(countdownTimerLabel); // Add the timer to the top panel

        // Log header
        JLabel logPanelHeader = new JLabel("Action Log");
        logPanelHeader.setFont(font);
        logPanelHeader.setForeground(Color.WHITE);
        logPanelHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        logPanelHeader.setBorder(marginBorder);
        logTopPanel.add(logPanelHeader); // Add the log header below the timer

        logPanel.add(logTopPanel, BorderLayout.NORTH); // Add the top panel to the top of logPanel

        // Panel for log messages
        JPanel logMessagePanel = new JPanel();
        logMessagePanel.setLayout(new BoxLayout(logMessagePanel, BoxLayout.Y_AXIS));
        logMessagePanel.setBackground(new Color(28, 0, 64));
        logMessagePanel.setForeground(Color.WHITE);
        logPanel.add(logMessagePanel, BorderLayout.CENTER); // Add the log messages panel to the center

        // Store the new action log in memory
        if (newActionMessage != null) {
            final int charLengthLimit = 26;
            Color messageColor = Color.WHITE;

            // Alternate colors for readability
            if (!this.actionLog.isEmpty()) {
                if (this.actionLog.get(this.actionLog.size() - 1).getForeground() == Color.WHITE) {
                    messageColor = Color.LIGHT_GRAY;
                }
            }

            // Wrap long messages
            for (int i = 0; i < Math.ceil(1.0 * newActionMessage.length() / charLengthLimit); i++) {
                String messagePiece = newActionMessage.substring(i * charLengthLimit, Math.min(i * charLengthLimit + charLengthLimit, newActionMessage.length()));

                if (i > 0) {
                    messagePiece = "..." + messagePiece;
                }

                JLabel actionMessage = new JLabel(messagePiece);
                actionMessage.setFont(font.deriveFont(11F));
                actionMessage.setHorizontalAlignment(JLabel.LEFT);
                actionMessage.setForeground(messageColor);

                this.actionLog.add(actionMessage);
            }
        }

        final int logLengthLimit = 38;
        // Display the messages. Display the newest messages if the log gets too long
        for (int i = Math.max(0, this.actionLog.size() - logLengthLimit); i < this.actionLog.size(); i++) {
            logMessagePanel.add(this.actionLog.get(i));
        }

        // Red team container for header, players, and scores
        JPanel redTeam = new JPanel(new BorderLayout());
        redTeam.setPreferredSize(new Dimension(actionDisplay.getWidth() / 3, actionDisplay.getHeight()));
        redTeam.setBackground(new Color(122, 0, 0));
        redTeam.setBorder(border);
        
        // Red team header
        JLabel redTeamHeader = new JLabel("Red Team");
        redTeamHeader.setFont(font);
        redTeamHeader.setForeground(Color.WHITE);
        redTeamHeader.setHorizontalAlignment(JLabel.CENTER);
        redTeamHeader.setBorder(marginBorder);
        redTeam.add(redTeamHeader, BorderLayout.NORTH);

        // Red team players
        JPanel redTeamPlayersPanel = new JPanel();
        redTeamPlayersPanel.setBackground(new Color(122, 0, 0));
        redTeamPlayersPanel.setForeground(Color.WHITE);
        redTeamPlayersPanel.setLayout(new BoxLayout(redTeamPlayersPanel, BoxLayout.PAGE_AXIS));
        
        // Red team scores
        JPanel redTeamScoresPanel = new JPanel();
        redTeamScoresPanel.setBackground(new Color(122, 0, 0));
        redTeamScoresPanel.setForeground(Color.WHITE);
        redTeamScoresPanel.setLayout(new BoxLayout(redTeamScoresPanel, BoxLayout.PAGE_AXIS));

        // Adds all the red team players and their scores to the players panel and scores panel
        LinkedHashMap<String, Integer> redTeamPlayers = this.getTeamPlayerScoresOrdered(Game.RED_TEAM_NUMBER);
        redTeamPlayers.forEach((codename, score) -> {
            JLabel codenameLabel = new JLabel(codename);
            codenameLabel.setFont(font.deriveFont(12F));
            codenameLabel.setForeground(Color.WHITE);

            JLabel scoreLabel = new JLabel(String.valueOf(score));
            scoreLabel.setFont(font.deriveFont(12F));
            scoreLabel.setForeground(Color.WHITE);

            redTeamPlayersPanel.add(codenameLabel);
            redTeamScoresPanel.add(scoreLabel);
        });

        JLabel redTeamTotalScoreLabel = new JLabel(String.valueOf(this.getTeamScore(Game.RED_TEAM_NUMBER)));
        redTeamTotalScoreLabel.setFont(font.deriveFont(15F));
        redTeamTotalScoreLabel.setForeground(Color.WHITE);
        redTeamScoresPanel.add(redTeamTotalScoreLabel);

        if (this.getLeadingTeam() == Game.RED_TEAM_NUMBER)
        {
            this.flashJLabel(redTeamTotalScoreLabel);
        }

        redTeam.add(redTeamPlayersPanel, BorderLayout.WEST);
        redTeam.add(redTeamScoresPanel, BorderLayout.EAST);

        // Green team container for header, players, and scores
        JPanel greenTeam = new JPanel(new BorderLayout());
        greenTeam.setPreferredSize(new Dimension(actionDisplay.getWidth() / 3, actionDisplay.getHeight()));
        greenTeam.setBackground(new Color(0, 122, 0));
        greenTeam.setBorder(border);

        // Green team header
        JLabel greenTeamHeader = new JLabel("Green Team");
        greenTeamHeader.setFont(font);
        greenTeamHeader.setForeground(Color.WHITE);
        greenTeamHeader.setHorizontalAlignment(JLabel.CENTER);
        greenTeamHeader.setBorder(marginBorder);
        greenTeam.add(greenTeamHeader, BorderLayout.NORTH);

        // Green team players
        JPanel greenTeamPlayersPanel = new JPanel();
        greenTeamPlayersPanel.setBackground(new Color(0, 122, 0));
        greenTeamPlayersPanel.setForeground(Color.WHITE);
        greenTeamPlayersPanel.setLayout(new BoxLayout(greenTeamPlayersPanel, BoxLayout.PAGE_AXIS));

        // Green team scores
        JPanel greenTeamScoresPanel = new JPanel();
        greenTeamScoresPanel.setBackground(new Color(0, 122, 0));
        greenTeamScoresPanel.setForeground(Color.WHITE);
        greenTeamScoresPanel.setLayout(new BoxLayout(greenTeamScoresPanel, BoxLayout.PAGE_AXIS));

        // Adds all the green team players and their scores to the players panel and scores panel
        LinkedHashMap<String, Integer> greenTeamPlayers = this.getTeamPlayerScoresOrdered(Game.GREEN_TEAM_NUMBER);
        greenTeamPlayers.forEach((codename, score) -> {
            JLabel codenameLabel = new JLabel(codename);
            codenameLabel.setFont(font.deriveFont(12F));
            codenameLabel.setForeground(Color.WHITE);

            JLabel scoreLabel = new JLabel(String.valueOf(score));
            scoreLabel.setFont(font.deriveFont(12F));
            scoreLabel.setForeground(Color.WHITE);

            greenTeamPlayersPanel.add(codenameLabel);
            greenTeamScoresPanel.add(scoreLabel);

        });

        JLabel greenTeamTotalScoreLabel = new JLabel(String.valueOf(this.getTeamScore(Game.GREEN_TEAM_NUMBER)));
        greenTeamTotalScoreLabel.setFont(font.deriveFont(15F));
        greenTeamTotalScoreLabel.setForeground(Color.WHITE);
        greenTeamScoresPanel.add(greenTeamTotalScoreLabel);

        if (this.getLeadingTeam() == Game.GREEN_TEAM_NUMBER)
        {
            this.flashJLabel(greenTeamTotalScoreLabel);
        }

        greenTeam.add(greenTeamPlayersPanel, BorderLayout.WEST);
        greenTeam.add(greenTeamScoresPanel, BorderLayout.EAST);

        // A container to hold everything (red team, log, green team)
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(redTeam);
        panel.add(logPanel);
        panel.add(greenTeam);

        actionDisplay.add(panel);

        actionDisplay.getContentPane().setBackground(new Color(28, 0, 64));
        actionDisplay.validate();
    }

    // Needed so garbage collector doesn't destroy it (???)
    // The media stops playing otherwise
    private static MediaPlayer mediaPlayer = null;

    public void playBackgroundMusic(String filePath, boolean loop) {
        new JFXPanel(); // Initialize JavaFX environment

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
        }

        try {
            // Convert the file path to a URI
            String uri = getClass().getClassLoader().getResource(filePath).toURI().toString();
            // Create a Media object and MediaPlayer
            Media media = new Media(uri);
            mediaPlayer = new MediaPlayer(media);

            if (loop)
            {
                // Set the media to loop until game end
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else
            {
                // Set the media to loop until game end
                mediaPlayer.setStartTime(Duration.ZERO);
                mediaPlayer.setStopTime(Duration.seconds(Game.START_COUNTDOWN + Game.GAME_LENGTH + 10));
            }

            

            // Play the media
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
