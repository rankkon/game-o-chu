package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.User;
import service.AuthService;
import service.MatchService;
import service.UserService;
import util.JsonUtil;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final AuthService authService;
    private final UserService userService;
    private final MatchService matchService;
    private final ServerMain serverMain;
    private User currentUser;
    private boolean running = true;

    private final MatchHistoryController matchHistoryController;

    public ClientHandler(Socket socket, AuthService authService, UserService userService, MatchService matchService, ServerMain serverMain) throws IOException {
        this.clientSocket = socket;
        this.authService = authService;
        this.userService = userService;
        this.matchService = matchService;
        this.serverMain = serverMain;
        this.matchHistoryController = new MatchHistoryController(userService);

        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private final RankingController rankingController = RankingController.getInstance();

    private void handleMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "LOGIN":
                    handleLogin(json);
                    break;
                case "REGISTER":
                    handleRegister(json);
                    break;
                case "GET_RANKINGS":
                    rankingController.handleGetRankings(this);
                    break;
                case "LOGOUT":
                    handleLogout();
                    break;
                case "INVITE_SEND":
                    handleInviteSend(json);
                    break;
                case "INVITE_RESPONSE":
                    handleInviteResponse(json);
                    break;
                case "MATCH_INPUT":
                    handleMatchInput(json);
                    break;
                case "MATCH_END":
                    handleMatchEnd(json);
                    break;
                case "GET_ONLINE_USERS":
                    handleGetOnlineUsers();
                    break;
                case "GET_USER_PROFILE":
                    handleGetUserProfile(json);
                    break;
                case "CHAT_MESSAGE":
                    handleChatMessage(json);
                    break;
                case "GET_MATCH_HISTORY":
                    handleGetMatchHistory(json);
                    break;
                default:
                    sendError("Unknown request type: " + type);
                    break;
            }
        } catch (Exception e) {
            sendError("Error processing request: " + e.getMessage());
        }
    }

    // ------------------- Handlers -------------------

    private void handleRegister(JsonObject json) {
        try {
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            String fullName = json.get("fullName").getAsString();
            int yearOfBirth = json.get("yearOfBirth").getAsInt(); 
            String gender = json.get("gender").getAsString();

            // Validation cơ bản
            if (username == null || username.trim().isEmpty()) {
                send(makeResponse("REGISTER_RESPONSE", "error", "Tài khoản không được để trống"));
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                send(makeResponse("REGISTER_RESPONSE", "error", "Mật khẩu không được để trống"));
                return;
            }
            if (fullName == null || fullName.trim().isEmpty()) {
                send(makeResponse("REGISTER_RESPONSE", "error", "Họ tên không được để trống"));
                return;
            }
            if (yearOfBirth < 1900 || yearOfBirth > 2024) {
                send(makeResponse("REGISTER_RESPONSE", "error", "Năm sinh không hợp lệ"));
                return;
            }
            
            // Gọi AuthService (đã có sẵn)
            boolean success = authService.register(username.trim(), password, fullName.trim(), yearOfBirth, gender);

            if (success) {
                send(makeResponse("REGISTER_RESPONSE", "success", "Đăng ký thành công!"));
            } else {
                // UserDAO của bạn đã xử lý việc kiểm tra trùng lặp
                send(makeResponse("REGISTER_RESPONSE", "error", "Tên tài khoản đã tồn tại"));
            }
        } catch (Exception e) {
            System.err.println("Error handling register: " + e.getMessage());
            send(makeResponse("REGISTER_RESPONSE", "error", "Đăng ký thất bại do lỗi server"));
        }
    }

    private void handleLogin(JsonObject json) {
        try {
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();

            // Basic validation
            if (username == null || username.trim().isEmpty()) {
                send(makeResponse("LOGIN_RESPONSE", "error", "Username cannot be empty"));
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                send(makeResponse("LOGIN_RESPONSE", "error", "Password cannot be empty"));
                return;
            }

            User user = authService.login(username.trim(), password);

            if (user != null) {
                this.currentUser = user;
                userService.addOnlineUser(user);

                // Register mapping for direct messaging
                serverMain.registerClient(user.getId(), this);

                JsonObject response = makeResponse("LOGIN_RESPONSE", "success");
                response.add("user", JsonParser.parseString(JsonUtil.toJson(user)));
                send(response);

                broadcastUserOnline(user);
            } else {
                send(makeResponse("LOGIN_RESPONSE", "error", "Invalid username or password"));
            }
        } catch (Exception e) {
            System.err.println("Error handling login: " + e.getMessage());
            send(makeResponse("LOGIN_RESPONSE", "error", "Login failed due to server error"));
        }
    }

    private void handleLogout() {
        if (currentUser != null) {
            userService.removeOnlineUser(currentUser.getId());
            broadcastUserOffline(currentUser);
            serverMain.unregisterClient(currentUser.getId());
            currentUser = null;

            send(makeResponse("LOGOUT_RESPONSE", "success"));
        }
    }

    private void handleGetOnlineUsers() {
        List<User> onlineUsers = userService.getOnlineUsers();
        JsonObject response = new JsonObject();
        response.addProperty("type", "ONLINE_USERS_RESPONSE");
        response.add("users", JsonParser.parseString(JsonUtil.toJson(onlineUsers)));
        send(response);
    }

    private void handleGetUserProfile(JsonObject json) {
        int userId = json.get("userId").getAsInt();
        User user = userService.getUserById(userId);

        if (user != null) {
            JsonObject response = makeResponse("USER_PROFILE_RESPONSE", "success");
            response.add("user", JsonParser.parseString(JsonUtil.toJson(user)));
            send(response);
        } else {
            sendError("User not found");
        }
    }

    // ------------------- Invite handlers -------------------

    private void handleInviteSend(JsonObject json) {
        if (currentUser == null) {
            sendError("Chưa đăng nhập");
            return;
        }

        int toUserId = json.get("toUserId").getAsInt();
        User target = userService.getUserById(toUserId);
        if (target == null || !userService.isUserOnline(toUserId)) {
            JsonObject resp = new JsonObject();
            resp.addProperty("type", "INVITE_STATUS");
            resp.addProperty("status", "error");
            resp.addProperty("message", "Người chơi không online");
            send(resp);
            return;
        }

        JsonObject invite = new JsonObject();
        invite.addProperty("type", "INVITE_REQUEST");
        invite.addProperty("fromUserId", currentUser.getId());
        invite.addProperty("fromUsername", currentUser.getUsername());
        serverMain.sendToUser(toUserId, invite.toString());

        JsonObject ack = new JsonObject();
        ack.addProperty("type", "INVITE_STATUS");
        ack.addProperty("status", "sent");
        send(ack);
    }

    private void handleInviteResponse(JsonObject json) {
        if (currentUser == null) {
            sendError("Chưa đăng nhập");
            return;
        }

        int fromUserId = json.get("fromUserId").getAsInt();
        boolean accepted = json.get("accepted").getAsBoolean();

        JsonObject notify = new JsonObject();
        notify.addProperty("type", "INVITE_RESPONSE");
        notify.addProperty("fromUserId", currentUser.getId());
        notify.addProperty("accepted", accepted);
        serverMain.sendToUser(fromUserId, notify.toString());

        // If accepted, create a match and notify both players
        if (accepted) {
            model.MatchRoom room = matchService.createRoom(fromUserId);
            matchService.startMatch(room.getRoomId(), currentUser.getId());
        }

        JsonObject ack = new JsonObject();
        ack.addProperty("type", "INVITE_STATUS");
        ack.addProperty("status", accepted ? "accepted" : "rejected");
        send(ack);
    }

    private void handleMatchInput(JsonObject json) {
        if (currentUser == null) {
            sendError("Chưa đăng nhập");
            return;
        }
        String roomId = json.get("roomId").getAsString();
        int wordIdx = json.get("wordIdx").getAsInt();
        int charIdx = json.get("charIdx").getAsInt();
        String chStr = json.get("ch").getAsString();
        char ch = chStr != null && chStr.length() > 0 ? chStr.charAt(0) : '\0';
        matchService.handleLetterInput(roomId, currentUser.getId(), wordIdx, charIdx, ch);
    }

    private void handleMatchEnd(JsonObject json) {
        if (currentUser == null) {
            sendError("Chưa đăng nhập");
            return;
        }
        String roomId = json.get("roomId").getAsString();
        matchService.endMatch(roomId);
    }

    private void handleChatMessage(JsonObject json) {
        if (currentUser == null) {
            sendError("Chưa đăng nhập");
            return;
        }
        
        String roomId = json.get("roomId").getAsString();
        String message = json.get("message").getAsString();
        
        // Gửi tới MatchService để xử lý và lưu chat log
        matchService.handleChatMessage(roomId, currentUser.getId(), message);
    }

    private void handleGetMatchHistory(JsonObject json) {
        if (currentUser == null) {
            sendError("Chưa đăng nhập");
            return;
        }
        matchHistoryController.handleGetMatchHistory(currentUser.getId());
    }

    // ------------------- Broadcast -------------------

    private void broadcastUserOnline(User user) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "USER_ONLINE");
        message.add("user", JsonParser.parseString(JsonUtil.toJson(user)));
        serverMain.broadcastToAll(message.toString());
    }

    private void broadcastUserOffline(User user) {
        JsonObject message = new JsonObject();
        message.addProperty("type", "USER_OFFLINE");
        message.addProperty("userId", user.getId());
        serverMain.broadcastToAll(message.toString());
    }

    // ------------------- Helpers -------------------

    private void send(JsonObject json) {
        out.println(json.toString());
    }

    private JsonObject makeResponse(String type, String status) {
        JsonObject response = new JsonObject();
        response.addProperty("type", type);
        response.addProperty("status", status);
        return response;
    }

    private JsonObject makeResponse(String type, String status, String message) {
        JsonObject response = makeResponse(type, status);
        response.addProperty("message", message);
        return response;
    }

    private void sendError(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("type", "ERROR");
        response.addProperty("message", message);
        send(response);
    }

    private void cleanup() {
        if (currentUser != null) {
            userService.removeOnlineUser(currentUser.getId());
            broadcastUserOffline(currentUser);
            serverMain.unregisterClient(currentUser.getId());
        }

        running = false;
        serverMain.removeClientHandler(this);

        try {
            in.close();
            out.close();
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
    
    /**
     * Send a message to this client with type and data
     */
    public void sendMessage(String type, JsonObject data) {
        JsonObject message = new JsonObject();
        message.addProperty("type", type);
        message.add("data", data);
        out.println(message.toString());
    }

    /**
     * Send a raw message to this client (used by broadcast)
     */
    public void sendMessage(String message) {
        out.println(message);
    }
}
