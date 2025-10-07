package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import service.AuthService;
import service.DictionaryService;
import service.MatchService;
import service.UserService;
import util.Logger;

public class ServerMain {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService threadPool;
    private AuthService authService;
    private UserService userService;
    private DictionaryService dictionaryService;
    private MatchService matchService;
    private final List<ClientHandler> clientHandlers;
    private final Map<Integer, ClientHandler> userIdToHandler;
    
    public ServerMain() {
        threadPool = Executors.newCachedThreadPool();
        authService = new AuthService();
        userService = new UserService();
        dictionaryService = new DictionaryService();
        matchService = new MatchService(dictionaryService, userService);
        clientHandlers = new ArrayList<>();
        userIdToHandler = new HashMap<>();

        // Provide a sender so services can push messages to users
        userService.setMessageSender((userId, message) -> sendToUser(userId, message));
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            Logger.info("Server started on port " + PORT);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                Logger.info("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // Create a new handler for this client and submit it to the thread pool
                ClientHandler clientHandler = new ClientHandler(clientSocket, authService, userService, matchService, this);
                clientHandlers.add(clientHandler);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        } finally {
            shutdown();
        }
    }
    
    public void shutdown() {
        running = false;
        threadPool.shutdown();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Logger.info("Server shut down");
    }
    
    /**
     * Broadcast message to all connected clients
     */
    public void broadcastToAll(String message) {
        synchronized (clientHandlers) {
            clientHandlers.removeIf(handler -> {
                try {
                    handler.sendMessage(message);
                    return false;
                } catch (Exception e) {
                    return true; // Remove disconnected clients
                }
            });
        }
    }

    // ---------------- Messaging helpers ----------------

    public void registerClient(int userId, ClientHandler handler) {
        synchronized (userIdToHandler) {
            userIdToHandler.put(userId, handler);
        }
    }

    public void unregisterClient(int userId) {
        synchronized (userIdToHandler) {
            userIdToHandler.remove(userId);
        }
    }

    public void sendToUser(int userId, String message) {
        ClientHandler handler;
        synchronized (userIdToHandler) {
            handler = userIdToHandler.get(userId);
        }
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    public void sendToUsers(int userId1, int userId2, String message) {
        sendToUser(userId1, message);
        if (userId2 != userId1) sendToUser(userId2, message);
    }
    
    /**
     * Remove a client handler from the list
     */
    public void removeClientHandler(ClientHandler handler) {
        synchronized (clientHandlers) {
            clientHandlers.remove(handler);
        }
    }
    
    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        server.start();
    }
}