package controller;

import javax.swing.SwingUtilities;

import model.User;

/**
 * UIController đóng vai trò trung gian quản lý logic UI và network.
 * Nó khởi tạo AuthController để xử lý login và LobbyController để xử lý lobby.
 */
public class UIController {
    private final AuthController authController;
    private LobbyController lobbyController;

    public UIController(SocketHandler socketHandler) {
        // Tạo AuthController và show login
        this.authController = new AuthController(socketHandler, this);
        authController.showLogin();
    }

    /**
     * Gọi khi login thành công. AuthController sẽ pass User về đây để mở lobby.
     */
    public void openLobby(User currentUser, SocketHandler socketHandler, String reconnectMatchId) {
        SwingUtilities.invokeLater(() -> {
            // Nếu đã có lobby cũ, đóng trước
            if (lobbyController != null) {
                lobbyController.closeLobby();
            }
            // Tạo LobbyController mới
            lobbyController = new LobbyController(socketHandler, currentUser, authController, reconnectMatchId);
            lobbyController.openLobby();
        });
    }

    /**
     * Gọi để logout từ UI.
     */
    public void logout() {
        SwingUtilities.invokeLater(() -> {
            if (lobbyController != null) {
                lobbyController.closeLobby();
                lobbyController = null;
            }
            authController.logout();
        });
    }

    /**
     * Lấy User hiện tại đang login.
     */
    public User getCurrentUser() {
        return authController.getCurrentUser();
    }

    /**
     * Lấy LobbyController hiện tại (nếu cần để thao tác trực tiếp với lobby).
     */
    public LobbyController getLobbyController() {
        return lobbyController;
    }

    /**
     * Lấy AuthController hiện tại (nếu cần thao tác với login).
     */
    public AuthController getAuthController() {
        return authController;
    }
}
