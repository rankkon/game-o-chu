package controller;

import model.MatchRoom;
import service.MatchService;
import service.UserService;
import util.JsonUtil;

import java.io.IOException;
import java.util.Map;

public class InviteController {
    private final UserService userService;
    private final MatchService matchService;

    public InviteController(UserService userService, MatchService matchService) {
        this.userService = userService;
        this.matchService = matchService;
    }

    public void handleInvite(int fromUserId, int toUserId) {
        if (!userService.isUserOnline(toUserId)) {
            String resp = JsonUtil.toJson(new java.util.HashMap<String, Object>() {{
                put("action", "invite_response");
                put("status", "error");
                put("message", "Người chơi không online.");
            }});
            userService.sendToUser(fromUserId, resp);
            return;
        }

        String req = JsonUtil.toJson(new java.util.HashMap<String, Object>() {{
            put("action", "invite_request");
            put("fromUserId", fromUserId);
        }});
        userService.sendToUser(toUserId, req);
    }

    public void handleInviteResponse(int fromUserId, int toUserId, boolean accepted) {
        if (!accepted) {
            String resp = JsonUtil.toJson(new java.util.HashMap<String, Object>() {{
                put("action", "invite_response");
                put("fromUserId", fromUserId);
                put("accepted", false);
            }});
            userService.sendToUser(toUserId, resp);
            return;
        }

        // Tạo trận đấu
        MatchRoom room = matchService.createRoom(fromUserId);
        matchService.startMatch(room.getRoomId(), toUserId);
    }
}
