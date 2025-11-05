package controller;

import service.MatchHistoryService;
import service.UserService;

/**
 * Controller for handling match history related requests
 */
public class MatchHistoryController {
    private final MatchHistoryService matchHistoryService;

    public MatchHistoryController(UserService userService) {
        this.matchHistoryService = new MatchHistoryService(userService);
    }

    /**
     * Handle request to view match history
     */
    public void handleGetMatchHistory(int userId) {
        matchHistoryService.sendMatchHistoryToUser(userId);
    }
}