package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import service.AuthService;
import service.UserService;
import util.Logger;

public class ServerMain {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running;
    private ExecutorService threadPool;
    private AuthService authService;
    private UserService userService;
    private final List<ClientHandler> clientHandlers;
    
    public ServerMain() {
        threadPool = Executors.newCachedThreadPool();
        authService = new AuthService();
        userService = new UserService();
        clientHandlers = new ArrayList<>();
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
                ClientHandler clientHandler = new ClientHandler(clientSocket, authService, userService, this);
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