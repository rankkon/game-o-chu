package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SocketHandler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final List<SocketListener> listeners = new ArrayList<>();
    private boolean running = false;
    private final Gson gson = new Gson();
    
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        running = true;
        new Thread(this::listenForMessages).start();
    }
    
    private void listenForMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                String type = json.get("type").getAsString();
                
                // Broadcast message to all listeners (create copy to avoid ConcurrentModificationException)
                List<SocketListener> listenersCopy = new ArrayList<>(listeners);
                for (SocketListener listener : listenersCopy) {
                    listener.onMessage(type, json);
                }
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
                List<SocketListener> listenersCopy = new ArrayList<>(listeners);
                for (SocketListener listener : listenersCopy) {
                    listener.onDisconnect("Mất kết nối: " + e.getMessage());
                }
            }
        } finally {
            disconnect();
        }
    }
    
    public void sendMessage(String type, JsonObject data) {
        if (out == null || !running) {
            throw new IllegalStateException("Socket không được kết nối");
        }
        
        JsonObject message = new JsonObject();
        message.addProperty("type", type);
        if (data != null) {
            // Add data fields directly to message instead of wrapping in "data"
            for (String key : data.keySet()) {
                message.add(key, data.get(key));
            }
        }
        
        out.println(gson.toJson(message));
    }
    
    public synchronized void addListener(SocketListener listener) {
        listeners.add(listener);
    }
    
    public synchronized void removeListener(SocketListener listener) {
        listeners.remove(listener);
    }
    
    public void disconnect() {
        running = false;
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        in = null;
        out = null;
        socket = null;
    }
    
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    public interface SocketListener {
        void onMessage(String type, JsonObject data);
        void onDisconnect(String reason);
    }
}