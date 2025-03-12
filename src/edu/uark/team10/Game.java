package edu.uark.team10;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private boolean isGameStart = false;
    private boolean isGameEnd = false;

    private UDPServer server = null;

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
        if (isGameStart) return; // Only allow this method to be called once

        this.server = server;
        this.isGameStart = true;

        this.addTeamPlayers(tableRedTeam, Game.RED_TEAM_NUMBER);
        this.addTeamPlayers(tableGreenTeam, Game.GREEN_TEAM_NUMBER);

        // Start the server
        this.server.start(); // Server extends Thread. Threads can only be started once\

        // Start the game after startCountdown seconds. This acts like the countdown timer
        this.server.sendMessage("202", Game.START_COUNTDOWN, TimeUnit.SECONDS);

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
     * Get the score of the player using their equipmentId
     * 
     * @param equipmentId
     * @return This player's score
     */
    public int getPlayerScore(int equipmentId)
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
    public int getTeamScore(int teamNumber)
    {
        this.checkValidity(teamNumber);
        
        return this.getEquipmentIdsOnTeam(teamNumber).stream()
            .mapToInt(this::getPlayerScore) // Convert the array of equipment IDs to an IntStream of player scores
            .sum(); // Sum the player scores
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
     * Checks if this equipment ID has scored a base.
     * 
     * @param equipmentId
     * @return True if player scored a base
     */
    public boolean hasPlayerScoredBase(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.playersWhoScoredBase.contains(equipmentId);
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
    public void playerScoredBase(int equipmentId)
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
    
}
