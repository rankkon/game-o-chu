package controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import model.User;
import util.UserParser;
import view.LobbyFrame;

public class LobbyController implements SocketHandler.SocketListener {
    private final SocketHandler socketHandler;
    private final User currentUser;
    private final AuthController authController;
    private LobbyFrame lobbyFrame;
    private view.GameFrame gameFrame;
    private final List<User> onlineUsers = new ArrayList<>();
    
    public LobbyController(SocketHandler socketHandler, User currentUser, AuthController authController) {
        this.socketHandler = socketHandler;
        this.currentUser = currentUser;
        this.authController = authController;
        this.socketHandler.addListener(this);
    }
    
    public void openLobby() {
        lobbyFrame = new LobbyFrame(this, currentUser);
        lobbyFrame.setVisible(true);
        
        // Request list of online users
        requestOnlineUsers();
    }
    
    public void closeLobby() {
        if (lobbyFrame != null) {
            lobbyFrame.dispose();
            lobbyFrame = null;
        }
    }
    
    public void requestOnlineUsers() {
        socketHandler.sendMessage("GET_ONLINE_USERS", null);
    }
    
    public void viewProfile(int userId) {
        JsonObject data = new JsonObject();
        data.addProperty("userId", userId);
        socketHandler.sendMessage("GET_USER_PROFILE", data);
    }
    
    public void logout() {
        authController.logout();
    }

    public void sendInvite(int toUserId) {
        JsonObject data = new JsonObject();
        data.addProperty("toUserId", toUserId);
        socketHandler.sendMessage("INVITE_SEND", data);
    }
    
    @Override
    public void onMessage(String type, JsonObject data) {
        switch (type) {
            case "ONLINE_USERS_RESPONSE":
                handleOnlineUsersResponse(data);
                break;
            case "USER_PROFILE_RESPONSE":
                handleUserProfileResponse(data);
                break;
            case "MATCH_HISTORY":
                handleMatchHistory(data);
                break;
            case "RANKING_UPDATE":
                handleRankingUpdate(data);
                break;
            case "USER_ONLINE":
                handleUserOnline(data);
                break;
            case "USER_OFFLINE":
                handleUserOffline(data);
                break;
            case "INVITE_REQUEST":
                handleInviteRequest(data);
                break;
            case "INVITE_STATUS":
                handleInviteStatus(data);
                break;
            case "INVITE_RESPONSE":
                handleInviteResponse(data);
                break;
            case "match_start":
                handleMatchStart(data);
                break;
            case "match_update":
                handleMatchUpdate(data);
                break;
            case "match_result":
                handleMatchResult(data);
                break;
            case "CHAT_MESSAGE":
                handleChatMessage(data);
                break;
            case "LEAVE_ROOM":
                handleLeaveRoom(data);
                break;
            case "WORD_CORRECT":
                handleWordCorrect(data);
                break;
            case "WORD_INCORRECT":
                handleWordIncorrect(data);
                break;
        }
    }
    
    private void handleOnlineUsersResponse(JsonObject data) {
        try {
            JsonArray usersArray = data.getAsJsonArray("users");
            List<User> updatedUsers = new ArrayList<>();
            
            for (JsonElement userElement : usersArray) {
                JsonObject userObject = userElement.getAsJsonObject();
                User user = UserParser.parseFromJson(userObject);
                updatedUsers.add(user);
            }
            
            // Update local list and UI atomically
            synchronized (onlineUsers) {
                onlineUsers.clear();
                onlineUsers.addAll(updatedUsers);
                if (lobbyFrame != null) {
                    lobbyFrame.updateOnlineUsers(onlineUsers);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling online users response: " + e.getMessage());
            if (lobbyFrame != null) {
                lobbyFrame.showError("Không thể cập nhật danh sách người chơi trực tuyến");
            }
        }
    }
    
    private void handleUserProfileResponse(JsonObject data) {
        try {
            String status = data.get("status").getAsString();
            
            if ("success".equals(status) && lobbyFrame != null) {
                JsonObject userObject = data.getAsJsonObject("user");
                User user = UserParser.parseFromJson(userObject);
                lobbyFrame.showProfile(user);
            } else if (!"success".equals(status) && lobbyFrame != null) {
                String errorMessage = data.get("message").getAsString();
                lobbyFrame.showError(errorMessage);
            }
        } catch (Exception e) {
            System.err.println("Error handling user profile response: " + e.getMessage());
            if (lobbyFrame != null) {
                lobbyFrame.showError("Không thể hiển thị thông tin người chơi");
            }
        }
    }
    
    private void handleUserOnline(JsonObject data) {
        try {
            JsonObject userObject = data.getAsJsonObject("user");
            int userId = userObject.get("id").getAsInt();
            
            synchronized (onlineUsers) {
                // Check if user already in list
                boolean found = onlineUsers.stream()
                        .anyMatch(user -> user.getId() == userId);
                
                if (!found) {
                    User user = UserParser.parseFromJson(userObject);
                    onlineUsers.add(user);
                    
                    if (lobbyFrame != null) {
                        lobbyFrame.updateOnlineUsers(onlineUsers);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling user online event: " + e.getMessage());
        }
    }
    
    private void handleUserOffline(JsonObject data) {
        try {
            int userId = data.get("userId").getAsInt();
            
            synchronized (onlineUsers) {
                boolean removed = onlineUsers.removeIf(user -> user.getId() == userId);
                
                if (removed && lobbyFrame != null) {
                    lobbyFrame.updateOnlineUsers(onlineUsers);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling user offline event: " + e.getMessage());
        }
    }

    private void handleInviteRequest(JsonObject data) {
        int fromUserId = data.get("fromUserId").getAsInt();
        String fromUsername = data.get("fromUsername").getAsString();
        if (lobbyFrame != null) {
            boolean accepted = JOptionPane.showConfirmDialog(lobbyFrame,
                    "" + fromUsername + " mời bạn đấu. Chấp nhận?",
                    "Lời mời đấu",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

            JsonObject resp = new JsonObject();
            resp.addProperty("fromUserId", fromUserId);
            resp.addProperty("accepted", accepted);
            socketHandler.sendMessage("INVITE_RESPONSE", resp);
        }
    }

    private void handleInviteStatus(JsonObject data) {
        String status = data.get("status").getAsString();
        if (lobbyFrame != null) {
            if ("sent".equals(status)) {
                lobbyFrame.showInfo("Đã gửi lời mời.");
            } else if ("accepted".equals(status)) {
                lobbyFrame.showInfo("Đã chấp nhận lời mời.");
            } else if ("rejected".equals(status)) {
                lobbyFrame.showInfo("Đã từ chối lời mời.");
            } else {
                String msg = data.has("message") ? data.get("message").getAsString() : "Lỗi";
                lobbyFrame.showError(msg);
            }
        }
    }

    private void handleInviteResponse(JsonObject data) {
        boolean accepted = data.get("accepted").getAsBoolean();
        if (lobbyFrame != null) {
            lobbyFrame.showInfo(accepted ? "Đối thủ đã chấp nhận!" : "Đối thủ đã từ chối.");
        }
    }

    private void handleMatchStart(JsonObject data) {
        if (gameFrame == null) {
            gameFrame = new view.GameFrame();
            gameFrame.setSelfUserId(currentUser.getId());
            gameFrame.setSocketHandler(socketHandler);
            
            // Set player names from match data
            JsonObject players = data.has("players") ? data.get("players").getAsJsonObject() : null;
            if (players != null) {
                String player1Name = currentUser.getName();
                String player2Name = "Đối thủ";
                
                for (Map.Entry<String, JsonElement> entry : players.entrySet()) {
                    try {
                        int uid = Integer.parseInt(entry.getKey());
                        if (uid != currentUser.getId()) {
                            JsonObject playerData = entry.getValue().getAsJsonObject();
                            if (playerData.has("name")) {
                                player2Name = playerData.get("name").getAsString();
                            }
                            break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
                
                gameFrame.setPlayerNames(player1Name, player2Name);
            }
            
            gameFrame.setVisible(true);
            gameFrame.showGameMessage("Trận đấu bắt đầu! Chúc bạn may mắn!");
        }
        gameFrame.loadMatchStart(data);
    }

    private void handleMatchUpdate(JsonObject data) {
        if (gameFrame != null) {
            gameFrame.loadMatchUpdate(data);
        }
    }

    private void handleMatchResult(JsonObject data) {
        String winnerName = null;
        if (data.has("winnerName")) {
            try { winnerName = data.get("winnerName").getAsString(); } catch (Exception ignore) { winnerName = null; }
        }
        if (winnerName == null || winnerName.trim().isEmpty()) {
            winnerName = data.has("winner") ? data.get("winner").getAsString() : "";
        }

        if (lobbyFrame != null) {
            lobbyFrame.showInfo("Kết thúc trận! Người thắng: " + winnerName);
            // Cập nhật bảng xếp hạng và danh sách người chơi sau khi kết thúc trận
            requestRankingUpdate();
            requestOnlineUsers();
        }
        if (gameFrame != null) {
            // Show result in GameFrame before closing
            JOptionPane.showMessageDialog(gameFrame, 
                "Kết thúc trận! Người thắng: " + winnerName, 
                "Kết quả trận đấu", 
                JOptionPane.INFORMATION_MESSAGE);
            gameFrame.dispose();
            gameFrame = null;
        }
    }

    private void handleChatMessage(JsonObject data) {
        if (gameFrame != null) {
            String senderName = data.has("senderName") ? data.get("senderName").getAsString() : "Người chơi";
            String message = data.has("message") ? data.get("message").getAsString() : "";
            gameFrame.receiveChatMessage(senderName, message);
        }
    }

    private void handleLeaveRoom(JsonObject data) {
        try {
            if (gameFrame != null) {
                gameFrame.dispose();
                gameFrame = null;
            }
            if (lobbyFrame != null) {
                lobbyFrame.showInfo("Đã thoát phòng trận đấu");
                // Refresh online users list when leaving room
                requestOnlineUsers();
            }
        } catch (Exception e) {
            System.err.println("Error handling leave room event: " + e.getMessage());
        }
    }

    private void handleWordCorrect(JsonObject data) {
        if (gameFrame != null) {
            int wordIdx = data.has("wordIdx") ? data.get("wordIdx").getAsInt() : -1;
            if (wordIdx >= 0) {
                gameFrame.highlightCorrectWord(wordIdx);
                gameFrame.showGameMessage("Tuyệt vời! Bạn đã đoán đúng từ!");
            }
        }
    }

    private void handleWordIncorrect(JsonObject data) {
        if (gameFrame != null) {
            int wordIdx = data.has("wordIdx") ? data.get("wordIdx").getAsInt() : -1;
            if (wordIdx >= 0) {
                gameFrame.highlightIncorrectWord(wordIdx);
                gameFrame.showGameMessage("Từ này không đúng. Hãy thử lại!");
            }
        }
    }
    
    @Override
    public void onDisconnect(String reason) {
        // This will be handled by AuthController
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public List<User> getOnlineUsers() {
        return onlineUsers;
    }

    public void requestRankingUpdate() {
        socketHandler.sendMessage("GET_RANKINGS", null);
    }

    private void handleMatchHistory(JsonObject data) {
        if (lobbyFrame != null && data != null) {
            List<model.Match> matches = new ArrayList<>();
            
            if (data.has("matches") && !data.get("matches").isJsonNull()) {
                try {
                    JsonArray matchesArray = data.getAsJsonArray("matches");
                    System.out.println("Received matches JSON: " + matchesArray.toString());
                    
                    for (JsonElement element : matchesArray) {
                        JsonObject matchObj = element.getAsJsonObject();
                        try {
                            // Get required fields with null checks
                            int matchId = matchObj.has("matchId") ? matchObj.get("matchId").getAsInt() : 0;
                            String player1Name = matchObj.has("player1Name") ? matchObj.get("player1Name").getAsString() : "Unknown";
                            String player2Name = matchObj.has("player2Name") ? matchObj.get("player2Name").getAsString() : "Unknown";
                            int player1Score = matchObj.has("player1Score") ? matchObj.get("player1Score").getAsInt() : 0;
                            int player2Score = matchObj.has("player2Score") ? matchObj.get("player2Score").getAsInt() : 0;
                            
                            // Get startTime with fallback
                            long startTimeMs = matchObj.has("startTime") ? matchObj.get("startTime").getAsLong() : 
                                             matchObj.has("matchDate") ? matchObj.get("matchDate").getAsLong() : 
                                             System.currentTimeMillis();
                            Date matchDate = new Date(startTimeMs);
                            
                            // Determine result based on winner
                            String result;
                            if (matchObj.has("winnerName") && !matchObj.get("winnerName").isJsonNull()) {
                                String winnerName = matchObj.get("winnerName").getAsString();
                                result = winnerName.equals(player1Name) ? "WIN" : "LOSE";
                            } else {
                                // If scores are equal, it's a draw
                                result = (player1Score == player2Score) ? "DRAW" : 
                                        (player1Score > player2Score) ? "WIN" : "LOSE";
                            }
                            
                            String category = matchObj.has("category") ? matchObj.get("category").getAsString() : "Không xác định";
                            String chatLog = matchObj.has("chatLog") ? matchObj.get("chatLog").getAsString() : "";
                            String opponentName = matchObj.has("opponentName") ? matchObj.get("opponentName").getAsString() : player2Name;

                            model.Match match = new model.Match(
                                matchId,
                                category,
                                player1Name,
                                player2Name,
                                player1Score,
                                player2Score,
                                matchDate,
                                result,
                                chatLog,
                                opponentName
                            );
                            matches.add(match);
                            
                        } catch (Exception e) {
                            System.err.println("Error parsing match: " + e.getMessage() + 
                                            "\nMatch data: " + matchObj.toString());
                            // Continue with next match
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing match array: " + e.getMessage() + 
                                     "\nReceived data: " + data.toString());
                }
            } else {
                System.err.println("No matches found in response or matches array is null");
            }
            
            lobbyFrame.showMatchHistory(matches);
        } else {
            System.err.println("LobbyFrame is null or received data is null");
        }
    }
    
    public void showMatchHistory() {
        socketHandler.sendMessage("GET_MATCH_HISTORY", null);
    }
    
    private void handleRankingUpdate(JsonObject data) {
        if (lobbyFrame != null) {
            JsonArray rankingsArray = data.has("data") && data.getAsJsonObject("data").has("rankings") 
                ? data.getAsJsonObject("data").getAsJsonArray("rankings") 
                : data.has("rankings") 
                    ? data.getAsJsonArray("rankings") 
                    : new JsonArray();
            
            List<model.Ranking> rankings = new ArrayList<>();
            
            int rank = 1;
            for (JsonElement element : rankingsArray) {
                JsonObject rankObj = element.getAsJsonObject();
                try {
                    model.Ranking ranking = new model.Ranking(
                        rank++,
                        rankObj.get("username").getAsString(),
                        rankObj.get("totalMatches").getAsInt(),
                        rankObj.get("wonMatches").getAsInt(),
                        rankObj.get("totalScore").getAsInt(),
                        rankObj.has("avgTimeRemaining") ? rankObj.get("avgTimeRemaining").getAsDouble() : 0.0
                    );
                    rankings.add(ranking);
                } catch (Exception e) {
                    System.err.println("Error parsing ranking data: " + e.getMessage());
                }
            }
            
            lobbyFrame.updateRankings(rankings);
        }
    }
}