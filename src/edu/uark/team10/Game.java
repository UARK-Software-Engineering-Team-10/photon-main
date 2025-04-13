package edu.uark.team10;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import edu.uark.team10.table.PlayerEntryTable;

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

    public static long START_COUNTDOWN = 30L;
    public static long GAME_LENGTH = 360L; // 6 minutes

    public static boolean isTestingMode = false;

    private static Application actionDisplay = null;

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
    private ArrayList<String> actionLog = new ArrayList<>();

    private boolean isGameStart = false;
    private boolean isGameEnd = false;

    private UDPServer server = null;

    public static void setActionDisplay(Application actionDisplay)
    {
        Game.actionDisplay = actionDisplay;
    }

    /**
     * Start the game. Also starts the UDP server.
     * This method can only be called once per game instance.
     * 
     * @param server
     * @param tableRedTeam
     * @param tableGreenTeam
     */
    public void start(UDPServer server, PlayerEntryTable tableRedTeam, PlayerEntryTable tableGreenTeam)
    {
        if (this.isGameStart) return; // Only allow this method to be called once

        try {
            if (Game.actionDisplay == null)
            {
                throw new Exception("Game error: action display not set.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Running game without display.");
        }

        this.server = server;
        this.isGameStart = true;

        this.addTeamPlayers(tableRedTeam, Game.RED_TEAM_NUMBER);
        this.addTeamPlayers(tableGreenTeam, Game.GREEN_TEAM_NUMBER);

        // Start the server
        this.server.start(); // Server extends Thread. Threads can only be started one time

        // Wait a second to make sure the server has started first
        this.server.sendMessage("202", 1L, TimeUnit.SECONDS);

        this.actionLog.add("Game start! (" + Game.GAME_LENGTH / 60.0 + " minutes)");

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
        this.server.sendMessage("221"); // Send 3 times according to requirements
        this.server.sendMessage("221");
        this.server.sendMessage("221");

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
        this.scores.put(equipmentId, points);

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
     * Smallest index is smallest score, biggest index is biggest score
     * 
     * @param teamNumber
     * @return A map of codename to score in ascending order
     */
    public Map<String, Integer> getTeamPlayerScoresOrdered(int teamNumber)
    {
        this.checkValidity(teamNumber);
        
        return this.getEquipmentIdsOnTeam(teamNumber).stream()
            .sorted(Comparator.comparingInt(this::getPlayerScore)) // Sort players by score
            .collect(Collectors.toMap(this::getCodename, this::getPlayerScore)); // Convert the array of equipment IDs to a map of codenames and scores
    }

    private void updateActionDisplay()
    {
        actionDisplay.getContentPane().removeAll();
        actionDisplay.revalidate();
        actionDisplay.repaint();

        final Font font = new Font("Conthrax SemBd", Font.PLAIN, 18);

        // Borders
        final Border marginBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        final Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        final Border border = BorderFactory.createCompoundBorder(etchedBorder, marginBorder);

        // Log
        JPanel logPanel = new JPanel();
        logPanel.setPreferredSize(new Dimension(actionDisplay.getWidth() / 4, actionDisplay.getHeight()));
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.PAGE_AXIS));
        logPanel.setBackground(new Color(28, 0, 64));
        logPanel.setBorder(border);



        // Log header
        JLabel logPanelHeader = new JLabel("Action Log");
        logPanelHeader.setFont(font);
        logPanelHeader.setForeground(Color.WHITE);
        logPanelHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        logPanelHeader.setBorder(marginBorder);
        logPanel.add(logPanelHeader);

        // TODO add action log
        // Listens for added/removed components for the action log container.
        /*
        logPanel.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                // Removes log entries to make room for new log entries
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Component[] components = logPanel.getComponents();
        
                        if (components == null) return;
                        if (components.length < 15) return;
        
                        for (int i = 15; i < components.length; i++)
                        {
                            logPanel.remove(components[i]);
                        }
        
        
                    }
                    
                });
            }
            @Override
            public void componentRemoved(ContainerEvent e) {
                //validate(); // might not need--will need testing
            }
            
        });
        */

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
        Map<String, Integer> redTeamPlayers = this.getTeamPlayerScoresOrdered(Game.RED_TEAM_NUMBER);
        redTeamPlayers.forEach((codename, score) -> {
            JLabel codenameLabel = new JLabel(codename);
            codenameLabel.setFont(font.deriveFont(14F));
            codenameLabel.setForeground(Color.WHITE);

            JLabel scoreLabel = new JLabel(String.valueOf(score));
            scoreLabel.setFont(font.deriveFont(14F));
            scoreLabel.setForeground(Color.WHITE);

            redTeamPlayersPanel.add(codenameLabel);
            redTeamScoresPanel.add(scoreLabel);
        });
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
        Map<String, Integer> greenTeamPlayers = this.getTeamPlayerScoresOrdered(Game.GREEN_TEAM_NUMBER);
        greenTeamPlayers.forEach((codename, score) -> {
            JLabel codenameLabel = new JLabel(codename);
            codenameLabel.setFont(font.deriveFont(14F));
            codenameLabel.setForeground(Color.WHITE);

            JLabel scoreLabel = new JLabel(String.valueOf(score));
            scoreLabel.setFont(font.deriveFont(14F));
            scoreLabel.setForeground(Color.WHITE);

            greenTeamPlayersPanel.add(codenameLabel);
            greenTeamScoresPanel.add(scoreLabel);

        });
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
    
}
