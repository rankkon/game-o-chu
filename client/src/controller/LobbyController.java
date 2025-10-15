package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import model.User;
import util.UserParser;
import view.LobbyFrame;
import view.ProfileDialog;
import javax.swing.JOptionPane;

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
        onlineUsers.clear();
        JsonArray usersArray = data.getAsJsonArray("users");
        
        for (JsonElement userElement : usersArray) {
            JsonObject userObject = userElement.getAsJsonObject();
            User user = UserParser.parseFromJson(userObject);
            onlineUsers.add(user);
        }
        
        if (lobbyFrame != null) {
            lobbyFrame.updateOnlineUsers(onlineUsers);
        }
    }
    
    private void handleUserProfileResponse(JsonObject data) {
        String status = data.get("status").getAsString();
        
        if ("success".equals(status) && lobbyFrame != null) {
            JsonObject userObject = data.getAsJsonObject("user");
            User user = UserParser.parseFromJson(userObject);
            
            ProfileDialog dialog = new ProfileDialog(lobbyFrame, user);
            dialog.setVisible(true);
        } else if (!"success".equals(status) && lobbyFrame != null) {
            String errorMessage = data.get("message").getAsString();
            lobbyFrame.showError(errorMessage);
        }
    }
    
    private void handleUserOnline(JsonObject data) {
        JsonObject userObject = data.getAsJsonObject("user");
        int userId = userObject.get("id").getAsInt();
        
        // Check if user already in list
        boolean found = false;
        for (User user : onlineUsers) {
            if (user.getId() == userId) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            User user = UserParser.parseFromJson(userObject);
            onlineUsers.add(user);
            
            if (lobbyFrame != null) {
                lobbyFrame.updateOnlineUsers(onlineUsers);
            }
        }
    }
    
    private void handleUserOffline(JsonObject data) {
        int userId = data.get("userId").getAsInt();
        
        User userToRemove = null;
        for (User user : onlineUsers) {
            if (user.getId() == userId) {
                userToRemove = user;
                break;
            }
        }
        
        if (userToRemove != null) {
            onlineUsers.remove(userToRemove);
            
            if (lobbyFrame != null) {
                lobbyFrame.updateOnlineUsers(onlineUsers);
            }
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
        if (gameFrame != null) {
            gameFrame.dispose();
            gameFrame = null;
        }
        if (lobbyFrame != null) {
            lobbyFrame.showInfo("Đã thoát phòng trận đấu");
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
}