package controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.JsonObject;

import model.User;
import util.UserParser;
import view.LoginFrame;

public class AuthController implements SocketHandler.SocketListener {
    private final SocketHandler socketHandler;
    private LoginFrame loginFrame;
    private User currentUser;
    private final UIController uiController;
    
    public AuthController(SocketHandler socketHandler, UIController uiController) {
        this.socketHandler = socketHandler;
        this.uiController = uiController;
        this.socketHandler.addListener(this);
    }
    
    public void showLogin() {
        SwingUtilities.invokeLater(() -> {
            if (loginFrame != null) {
                loginFrame.dispose();
            }
            loginFrame = new LoginFrame(this);
            loginFrame.setVisible(true);
        });
    }
    
    public void login(String username, String password) {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        socketHandler.sendMessage("LOGIN", data);
    }
    
    public void register(String username, String password, String fullName, int yearOfBirth, String gender) {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        data.addProperty("fullName", fullName); 
        data.addProperty("yearOfBirth", yearOfBirth);
        data.addProperty("gender", gender);
        SocketHandler.getInstance().sendMessage("REGISTER", data);
    }

    public void logout() {
        socketHandler.sendMessage("LOGOUT", null);
        currentUser = null;
        showLogin();
    }
    
    @Override
    public void onMessage(String type, JsonObject data) {
        if ("LOGIN_RESPONSE".equals(type)) {
            handleLoginResponse(data);
        } else if ("LOGOUT_RESPONSE".equals(type)) {
            handleLogoutResponse(data);
        } else if ("REGISTER_RESPONSE".equals(type)) {
            handleRegisterResponse(data);
        }
    }
    
    private void handleRegisterResponse(JsonObject data) {
        final String status = data.get("status").getAsString();
        final String message = data.get("message").getAsString();
        SwingUtilities.invokeLater(() -> {
            if ("success".equals(status)) {
                JOptionPane.showMessageDialog(loginFrame,
                        message,
                        "Dang ky thanh cong",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (loginFrame != null) {
                loginFrame.showError(message);
            }
        });
    }

    private void handleLoginResponse(JsonObject data) {
        String status = data.get("status").getAsString();
        if ("success".equals(status)) {
            JsonObject userData = data.getAsJsonObject("user");
            User parsedUser = UserParser.parseFromJson(userData);
            String reconnectMatchId = null;
            if (data.has("reconnectMatchId") && !data.get("reconnectMatchId").isJsonNull()) {
                reconnectMatchId = data.get("reconnectMatchId").getAsString();
                System.out.println("[AuthController] Login success, found reconnectMatchId: " + reconnectMatchId);
            }
            final String reconnectId = reconnectMatchId;
            final User userFinal = parsedUser;
            SwingUtilities.invokeLater(() -> {
                this.currentUser = userFinal;
                if (loginFrame != null) {
                    loginFrame.dispose();
                    loginFrame = null;
                }
                uiController.openLobby(userFinal, socketHandler, reconnectId);
            });
        } else {
            String errorMessage = data.get("message").getAsString();
            SwingUtilities.invokeLater(() -> {
                if (loginFrame != null) {
                    loginFrame.showError(errorMessage);
                }
            });
        }
    }
    
    private void handleLogoutResponse(JsonObject data) {
        // No-op: logout already processed locally.
    }
    
    @Override
    public void onDisconnect(String reason) {
        SwingUtilities.invokeLater(() -> {
            currentUser = null;
            if (loginFrame != null) {
                loginFrame.showError("Mat ket noi den may chu: " + reason);
            } else {
                showLogin();
                if (loginFrame != null) {
                    loginFrame.showError("Mat ket noi den may chu: " + reason);
                }
            }
        });
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
}
