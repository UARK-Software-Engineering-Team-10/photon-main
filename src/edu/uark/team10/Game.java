package edu.uark.team10;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Game {

    public static final int BASE_POINTS = 100;
    public static final int TAG_POINTS = 10;

    public static final int RED_TEAM_NUMBER = 53;
    public static final int GREEN_TEAM_NUMBER = 43;

    private static long startCountdown = 30L;
    private static long gameLength = 360L; // 6 minutes

    public static boolean isTestingMode = false;

    // Maps equipment ID to player ID
    private HashMap<Integer, Integer> players = new HashMap<>();
    // Maps equipment ID to team number
    private HashMap<Integer, Integer> playerTeams = new HashMap<>();
    // Maps equipment ID to score
    private HashMap<Integer, Integer> scores = new HashMap<>();
    // Maps equipment ID to codename
    private HashMap<Integer, String> playerCodenames = new HashMap<>();

    private ArrayList<Integer> playersWhoScoredBase = new ArrayList<>();

    private boolean isGameStart = false;
    private boolean isGameEnd = false;

    private UDPServer server = null;

    public void start(UDPServer server)
    {
        if (isGameStart) return;

        this.server = server;
        this.isGameStart = true;

        this.server.run();

        // TODO countdown timer
        this.server.sendMessage("202");

        if (isTestingMode)
        {
            this.startCountdown = 5L;
            this.gameLength = 30L;
        }

        CompletableFuture<Void> gameTimerFuture = new CompletableFuture<Void>().completeOnTimeout(null, Game.gameLength, TimeUnit.SECONDS);
        gameTimerFuture.whenComplete((none, exception) -> {
            if (exception != null)
            {
                exception.printStackTrace();
            }

            end();
        });

    }

    public void end()
    {
        if (isGameEnd) return;

        this.isGameEnd = true;

        this.server.sendMessage("221");
        this.server.sendMessage("221");
        this.server.sendMessage("221");

    }

    public void addPlayer(Object[] playerdata)
    {
        int equipmentId = Integer.valueOf(String.valueOf(playerdata[0]));
        int playerId = Integer.valueOf(String.valueOf(playerdata[1]));
        int teamNumber = Integer.valueOf(String.valueOf(playerdata[2]));
        String codename = String.valueOf(playerdata[3]);

        this.players.put(equipmentId, playerId);
        this.playerTeams.put(equipmentId, teamNumber);
        this.scores.put(equipmentId, 0);
        this.playerCodenames.put(equipmentId, codename);

    }

    // Checks the validity of equipmentId by
    // checking if it is a key of the players map
    // or if it is a team base number.
    // Throws an exception if invalid
    private void checkValidity(int equipmentId)
    {
        if (equipmentId == Game.RED_TEAM_NUMBER || equipmentId == Game.GREEN_TEAM_NUMBER) return;

        try {
            if (this.players.get(equipmentId) == null)
            {
                throw new Exception("Invalid equipment ID: This equipment ID (" + equipmentId + ") is not registered.");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private ArrayList<Integer> getPlayersOnTeam(int teamNumber)
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

    // Get the team number of the player associated with equipmentId
    // 53 is red team, 43 is green team
    public Integer getTeamFromEquipmentId(int equipmentId)
    {
        this.checkValidity(equipmentId);

        // Returns the correct player team, or equipmentId if the get is null.
        // If the get is null, it means the equipment ID is a team base
        return Optional.ofNullable(this.playerTeams.get(equipmentId)).orElse(equipmentId);
    }

    // Get player ID rom the equipment ID
    public Integer getPlayerIdFromEquipmentId(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.players.get(equipmentId); // May be null
    }

    // Get the score of the player associated with equipmentId
    public int getPlayerScore(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.scores.get(equipmentId);
    }

    // Get the total score of a team
    public int getTeamScore(int teamNumber)
    {
        this.checkValidity(teamNumber);
        
        return this.getPlayersOnTeam(teamNumber).stream()
            .mapToInt(this::getPlayerScore) // Convert the array of equipment IDs to an IntStream of player scores
            .sum(); // Sum the player scores
    }

    // Get the codename of the player associated with equipmentId.
    // Adds stylized "B" to the beginning of their name if they scored a base.
    public String getCodename(int equipmentId)
    {
        this.checkValidity(equipmentId);
        String codename = this.playerCodenames.get(equipmentId);

        if (hasPlayerScoredBase(equipmentId))
        {
            codename = "[B] ".concat(codename);
        }

        return codename;
    }

    public boolean hasPlayerScoredBase(int equipmentId)
    {
        this.checkValidity(equipmentId);

        return this.playersWhoScoredBase.contains(equipmentId);
    }

    // Add points to the score of the player associated with equipmentId
    public int addPoints(int points, int equipmentId)
    {
        this.checkValidity(equipmentId);
        int score = this.scores.get(equipmentId) + points;
        this.scores.put(equipmentId, points);

        return score;
    }

    public void playerScoredBase(int equipmentId)
    {
        this.checkValidity(equipmentId);

        this.playersWhoScoredBase.add(equipmentId);
    }

    // Returns an ordered map of codename to score.
    // Smallest index is smallest score, biggest index is biggest score
    public Map<String, Integer> getTeamPlayerScoresOrdered(int teamNumber)
    {
        this.checkValidity(teamNumber);
        
        return this.getPlayersOnTeam(teamNumber).stream()
            .sorted(Comparator.comparingInt(this::getPlayerScore)) // Sort players by score
            .collect(Collectors.toMap(this::getCodename, this::getPlayerScore)); // Convert the array of equipment IDs to a map of codenames and scores
    }
    
}
