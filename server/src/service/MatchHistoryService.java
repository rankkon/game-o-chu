package service;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dao.MatchDAO;

/**
 * Service for retrieving and managing match history
 */
public class MatchHistoryService {
    private final MatchDAO matchDAO;
    private final UserService userService;

    public MatchHistoryService(UserService userService) {
        this.matchDAO = new MatchDAO();
        this.userService = userService;
    }

    /**
     * Get match history for a specific user
     */
    public List<JsonObject> getMatchHistory(int userId) {
        return matchDAO.getMatchHistory(userId);
    }

    /**
     * Send match history to a specific user
     */
    public void sendMatchHistoryToUser(int userId) {
        List<JsonObject> matchHistory = getMatchHistory(userId);
        JsonObject response = new JsonObject();
        response.addProperty("type", "MATCH_HISTORY");
        
        // Convert the List<JsonObject> to JsonArray directly
        JsonArray matchArray = new JsonArray();
        for (JsonObject match : matchHistory) {
            matchArray.add(match);
        }
        response.add("matches", matchArray);
        
        userService.sendToUser(userId, response.toString());
    }
}