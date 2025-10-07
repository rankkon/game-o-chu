package controller;

import com.google.gson.JsonObject;
import model.User;
import util.UserParser;
import view.LoginFrame;

import javax.swing.SwingUtilities;

public class AuthController implements SocketHandler.SocketListener {
    private final SocketHandler socketHandler;
    private LoginFrame loginFrame;
    private User currentUser;
    private LobbyController lobbyController;

    public AuthController(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
        this.socketHandler.addListener(this);
    }

    /** Hiển thị màn hình đăng nhập (chạy trên luồng UI) */
    public void showLogin() {
        SwingUtilities.invokeLater(() -> {
            if (loginFrame != null) {
                loginFrame.dispose();
            }
            loginFrame = new LoginFrame(this);
            loginFrame.setVisible(true);
        });
    }

    /** Gửi yêu cầu đăng nhập đến server */
    public void login(String username, String password) {
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        socketHandler.sendMessage("LOGIN", data);
    }

    /** Gửi yêu cầu đăng xuất và quay lại màn hình đăng nhập */
    public void logout() {
        socketHandler.sendMessage("LOGOUT", null);
        currentUser = null;

        if (lobbyController != null) {
            lobbyController.closeLobby();
            lobbyController = null;
        }

        showLogin();
    }

    /** Xử lý các gói tin từ server */
    @Override
    public void onMessage(String type, JsonObject data) {
        switch (type) {
            case "LOGIN_RESPONSE" -> handleLoginResponse(data);
            case "LOGOUT_RESPONSE" -> handleLogoutResponse(data);
        }
    }

    /** Xử lý phản hồi đăng nhập từ server */
    private void handleLoginResponse(JsonObject data) {
        String status = data.get("status").getAsString();

        if ("success".equals(status)) {
            JsonObject userData = data.getAsJsonObject("user");
            this.currentUser = UserParser.parseFromJson(userData);

            // Đảm bảo cập nhật UI trên luồng giao diện
            SwingUtilities.invokeLater(() -> {
                if (loginFrame != null) {
                    loginFrame.dispose();
                    loginFrame = null;
                }

                lobbyController = new LobbyController(socketHandler, currentUser, this);
                lobbyController.openLobby();
            });
        } else {
            String errorMessage = data.get("message").getAsString();
            if (loginFrame != null) {
                loginFrame.showError(errorMessage);
            }
        }
    }

    /** Xử lý phản hồi đăng xuất */
    private void handleLogoutResponse(JsonObject data) {
        // Server có thể gửi thêm thông tin nếu cần
    }

    /** Xử lý khi bị mất kết nối với server */
    @Override
    public void onDisconnect(String reason) {
        currentUser = null;

        SwingUtilities.invokeLater(() -> {
            if (lobbyController != null) {
                lobbyController.closeLobby();
                lobbyController = null;
            }

            if (loginFrame == null) {
                loginFrame = new LoginFrame(this);
            }

            loginFrame.setVisible(true);
            loginFrame.showError("Mất kết nối đến máy chủ: " + reason);
        });
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
