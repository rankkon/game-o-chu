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
import service.UserService;
import util.JsonUtil;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final AuthService authService;
    private final UserService userService;
    private final ServerMain serverMain;
    private User currentUser;
    private boolean running = true;

    public ClientHandler(Socket socket, AuthService authService, UserService userService, ServerMain serverMain) throws IOException {
        this.clientSocket = socket;
        this.authService = authService;
        this.userService = userService;
        this.serverMain = serverMain;

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

    private void handleMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "LOGIN":
                    handleLogin(json);
                    break;
                case "LOGOUT":
                    handleLogout();
                    break;
                case "GET_ONLINE_USERS":
                    handleGetOnlineUsers();
                    break;
                case "GET_USER_PROFILE":
                    handleGetUserProfile(json);
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
     * Send a message to this client (used by broadcast)
     */
    public void sendMessage(String message) {
        out.println(message);
    }
}
