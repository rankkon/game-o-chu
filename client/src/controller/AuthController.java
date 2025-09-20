package controller;

import com.google.gson.JsonObject;

import model.User;
import util.UserParser;
import view.LoginFrame;

public class AuthController implements SocketHandler.SocketListener {
    private final SocketHandler socketHandler;
    private LoginFrame loginFrame;
    private User currentUser;
    private LobbyController lobbyController;
    
    public AuthController(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        this.socketHandler.addListener(this);
    }
    
    public void showLogin() {
        loginFrame = new LoginFrame(this);
        loginFrame.setVisible(true);
    }
    
    public void login(String username, String password) {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        socketHandler.sendMessage("LOGIN", data);
    }
    
    public void logout() {
        socketHandler.sendMessage("LOGOUT", null);
        currentUser = null;
        
        // Close lobby if open and show login screen
        if (lobbyController != null) {
            lobbyController.closeLobby();
            lobbyController = null;
        }
        showLogin();
    }
    
    @Override
    public void onMessage(String type, JsonObject data) {
        if ("LOGIN_RESPONSE".equals(type)) {
            handleLoginResponse(data);
        } else if ("LOGOUT_RESPONSE".equals(type)) {
            handleLogoutResponse(data);
        }
    }
    
    private void handleLoginResponse(JsonObject data) {
        String status = data.get("status").getAsString();
        
        if ("success".equals(status)) {
            // Parse user information using utility class
            JsonObject userData = data.getAsJsonObject("user");
            this.currentUser = UserParser.parseFromJson(userData);
            
            // Close login frame
            loginFrame.dispose();
            loginFrame = null;
            
            // Open lobby
            lobbyController = new LobbyController(socketHandler, currentUser, this);
            lobbyController.openLobby();
        } else {
            // Show error message
            String errorMessage = data.get("message").getAsString();
            loginFrame.showError(errorMessage);
        }
    }
    
    private void handleLogoutResponse(JsonObject data) {
        // User already logged out in the logout() method
        // We could handle any special server response here if needed
    }
    
    @Override
    public void onDisconnect(String reason) {
        // Handle disconnect, e.g. show reconnect dialog
        if (lobbyController != null) {
            lobbyController.closeLobby();
            lobbyController = null;
        }
        
        if (loginFrame != null) {
            loginFrame.showError("Mất kết nối đến máy chủ: " + reason);
        } else {
            showLogin();
            loginFrame.showError("Mất kết nối đến máy chủ: " + reason);
        }
        
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
}