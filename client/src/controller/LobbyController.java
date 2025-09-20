package controller;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import model.User;
import util.UserParser;
import view.LobbyFrame;
import view.ProfileDialog;

public class LobbyController implements SocketHandler.SocketListener {
    private final SocketHandler socketHandler;
    private final User currentUser;
    private final AuthController authController;
    private LobbyFrame lobbyFrame;
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