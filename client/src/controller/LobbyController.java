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
    private String reconnectMatchId;
    
    public LobbyController(SocketHandler socketHandler, User currentUser, AuthController authController, String reconnectMatchId) {
        this.socketHandler = socketHandler;
        this.currentUser = currentUser;
        this.authController = authController;
        this.reconnectMatchId = reconnectMatchId;
        this.socketHandler.addListener(this);
    }
    
    public void openLobby() {
        lobbyFrame = new LobbyFrame(this, currentUser);
        lobbyFrame.updateButtonsForReconnect(this.reconnectMatchId);
        lobbyFrame.setVisible(true);
        
        // Request list of online users
        requestOnlineUsers();
    }

    public String getReconnectMatchId() {
        return this.reconnectMatchId;
    }
    public void requestReconnect() {
        if (this.reconnectMatchId != null) {
            JsonObject data = new JsonObject();
            data.addProperty("roomId", this.reconnectMatchId);
            socketHandler.sendMessage("RECONNECT_MATCH", data);
        }
    }
    public void requestExitMatch(String roomId) {
        JsonObject data = new JsonObject();
        data.addProperty("roomId", roomId);
        socketHandler.sendMessage("REQUEST_EXIT_MATCH", data);
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
        // Đảm bảo đóng cửa sổ lobby và ngừng nhận sự kiện trước khi logout
        closeLobby();
        socketHandler.removeListener(this);
        authController.logout();
    }

    public void requestMatchmaking() {
        socketHandler.sendMessage("MATCHMAKE_START", null);
    }

    public void cancelMatchmaking() {
        socketHandler.sendMessage("MATCHMAKE_CANCEL", null);
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
            case "MATCHMAKE_WAITING":
                handleMatchmakeWaiting(data);
                break;
            case "MATCHMAKE_CANCELED":
                handleMatchmakeCanceled(data);
                break;
            case "INVITE_REQUEST":
                handleInviteRequest(data);
                break;
            case "INVITE_RESPONSE":
                handleInviteResponse(data);
                break;
            case "match_start":
                this.reconnectMatchId = null; 
                if (lobbyFrame != null) {
                    lobbyFrame.updateButtonsForReconnect(null); 
                }
                handleMatchStart(data);
                break;
            case "match_update":
                handleMatchUpdate(data);
                break;
            case "match_result":
                this.reconnectMatchId = null;
                if (lobbyFrame != null) {
                    lobbyFrame.updateButtonsForReconnect(null);
                }
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
            case "EXIT_MATCH_ACK":
                handleExitMatchAck(data);
                break;
            case "ask_continue":
                handleAskContinue(data);
                break;
            case "return_to_lobby":
                handleReturnToLobby(data);
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

    private void handleMatchmakeWaiting(JsonObject data) {
        if (lobbyFrame != null) {
            // Cập nhật UI sang trạng thái "Đang chờ"
            lobbyFrame.setMatchmakingStatus(true);
        }
    }

    private void handleMatchmakeCanceled(JsonObject data) {
        if (lobbyFrame != null) {
            // Cập nhật UI về trạng thái "Rảnh"
            lobbyFrame.setMatchmakingStatus(false);
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

    private void handleInviteResponse(JsonObject data) {
        boolean accepted = data.get("accepted").getAsBoolean();
        if (lobbyFrame != null) {
            lobbyFrame.showInfo(accepted ? "Đối thủ đã chấp nhận!" : "Đối thủ đã từ chối.");
        }
    }

    private void handleMatchStart(JsonObject data) {
        if (lobbyFrame != null) {
            lobbyFrame.setMatchmakingStatus(false);
        }
        JsonObject gameData = data.has("data") ? data.get("data").getAsJsonObject() : data;
        
        if (gameFrame == null) {
            gameFrame = new view.GameFrame();
            gameFrame.setSelfUserId(currentUser.getId());
            gameFrame.setSocketHandler(socketHandler);
            
            // Set player names from match data
            JsonObject players = gameData.has("players") ? gameData.get("players").getAsJsonObject() : null;
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
            // Chỉ cần yêu cầu lại danh sách online; bảng xếp hạng đã được server broadcast
            requestOnlineUsers();
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
                handleExitMatchAck(data);
            }
        } catch (Exception e) {
            System.err.println("Error handling leave room event: " + e.getMessage());
        }
    }

    private void handleExitMatchAck(JsonObject data) {
        // 1. Đóng GameFrame
        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }
        // 2. Mở LobbyFrame (nếu đang bị ẩn)
        if (lobbyFrame != null) {
            lobbyFrame.setVisible(true);
            
            // 3. Cập nhật reconnectId và UI
            if (data.has("reconnectMatchId") && !data.get("reconnectMatchId").isJsonNull()) {
                this.reconnectMatchId = data.get("reconnectMatchId").getAsString();
            } else {
                this.reconnectMatchId = null;
            }
            lobbyFrame.updateButtonsForReconnect(this.reconnectMatchId);
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

    public void handleMatchHistory(JsonObject data) {
        if (lobbyFrame != null && data != null) {
            List<model.Match> matches = new ArrayList<>();
            
            if (data.has("matches") && !data.get("matches").isJsonNull()) {
                try {
                    JsonArray matchesArray = data.getAsJsonArray("matches");
                    // System.out.println("Received matches JSON: " + matchesArray.toString());
                    
                    for (JsonElement element : matchesArray) {
                        JsonObject matchObj = element.getAsJsonObject();
                        try {                        // Build match entry relative to current user
                        int matchId = matchObj.has("matchId") ? matchObj.get("matchId").getAsInt() : 0;
                        int player1Id = matchObj.has("player1Id") ? matchObj.get("player1Id").getAsInt() : -1;
                        int player2Id = matchObj.has("player2Id") ? matchObj.get("player2Id").getAsInt() : -1;
                        String player1Name = matchObj.has("player1Name") ? matchObj.get("player1Name").getAsString() : "Unknown";
                        String player2Name = matchObj.has("player2Name") ? matchObj.get("player2Name").getAsString() : "Unknown";
                        int player1Score = matchObj.has("player1Score") ? matchObj.get("player1Score").getAsInt() : 0;
                        int player2Score = matchObj.has("player2Score") ? matchObj.get("player2Score").getAsInt() : 0;

                        long startTimeMs = matchObj.has("startTime") ? matchObj.get("startTime").getAsLong()
                                : matchObj.has("matchDate") ? matchObj.get("matchDate").getAsLong()
                                : System.currentTimeMillis();
                        Date matchDate = new Date(startTimeMs);

                        int currentPlayerId = currentUser != null ? currentUser.getId() : player1Id;
                        boolean isPlayerOne = currentPlayerId == player1Id;
                        String selfName = currentUser != null ? currentUser.getName()
                                : (isPlayerOne ? player1Name : player2Name);
                        String opponentName = isPlayerOne ? player2Name : player1Name;
                        int opponentId = isPlayerOne ? player2Id : player1Id;
                        int myScore = isPlayerOne ? player1Score : player2Score;
                        int opponentScore = isPlayerOne ? player2Score : player1Score;

                        int winnerId = (matchObj.has("winnerId") && !matchObj.get("winnerId").isJsonNull())
                                ? matchObj.get("winnerId").getAsInt()
                                : -1;

                        String result = (matchObj.has("result") && !matchObj.get("result").isJsonNull())
                                ? matchObj.get("result").getAsString().toUpperCase()
                                : null;
                        if (result == null || result.isEmpty()) {
                            if (winnerId > 0) {
                                result = winnerId == currentPlayerId ? "WIN"
                                        : (winnerId == opponentId ? "LOSE" : "DRAW");
                            } else if (myScore == opponentScore) {
                                result = "DRAW";
                            } else {
                                result = myScore > opponentScore ? "WIN" : "LOSE";
                            }
                        }

                        String category = matchObj.has("category") ? matchObj.get("category").getAsString() : "Chua x�c d?nh";
                        String chatLog = (matchObj.has("chatLog") && !matchObj.get("chatLog").isJsonNull())
                                ? matchObj.get("chatLog").getAsString()
                                : "Kh�ng c�";

                        model.Match match = new model.Match(
                            matchId,
                            category,
                            selfName,
                            opponentName,
                            myScore,
                            opponentScore,
                            matchDate,
                            result,
                            chatLog,
                            opponentName
                        );
                        matches.add(match);
                            
                        } catch (Exception e) {
                            // System.err.println("Error parsing match: " + e.getMessage() + 
                            //                 "\nMatch data: " + matchObj.toString());
                            // Continue with next match
                        }
                    }
                } catch (Exception e) {
                    // System.err.println("Error parsing match array: " + e.getMessage() + 
                    //                  "\nReceived data: " + data.toString());
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
    
    private void handleAskContinue(JsonObject data) {
        String roomId = data.get("roomId").getAsString();
        
        if (gameFrame != null && gameFrame.isVisible()) {
            // Hiển thị dialog hỏi có muốn chơi tiếp không
            int choice = JOptionPane.showConfirmDialog(
                gameFrame,
                "Bạn có muốn chơi tiếp với đối thủ này không?",
                "Chơi tiếp?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            // Gửi phản hồi về server
            JsonObject response = new JsonObject();
            response.addProperty("roomId", roomId);
            response.addProperty("wantsContinue", choice == JOptionPane.YES_OPTION);
            socketHandler.sendMessage("CONTINUE_RESPONSE", response);
        }
    }
    
    private void handleReturnToLobby(JsonObject data) {
        // Đóng GameFrame và hiển thị LobbyFrame
        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }
        
        if (lobbyFrame != null) {
            lobbyFrame.setVisible(true);
            lobbyFrame.showInfo("Trận đấu đã kết thúc. Chào mừng bạn quay lại sảnh chờ!");
        }
    }
}



