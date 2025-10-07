package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dao.UserDAO;
import model.User;

public class UserService {
    private final UserDAO userDAO;
    private final Map<Integer, User> onlineUsers;
    private MessageSender messageSender;

    public UserService() {
        this.userDAO = new UserDAO();
        this.onlineUsers = new ConcurrentHashMap<>();
    }

    // ------------------ Database operations ------------------
    public User login(String username, String password) {
        return userDAO.login(username, password);
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    public List<User> getAllUsers() {
        List<User> users = userDAO.getAllUsers();
        return users != null ? users : new ArrayList<>();
    }

    // ------------------ Online user management ------------------
    public boolean addOnlineUser(User user) {
        return onlineUsers.putIfAbsent(user.getId(), user) == null;
    }

    public void removeOnlineUser(int userId) {
        onlineUsers.remove(userId);
    }

    public boolean isUserOnline(int userId) {
        return onlineUsers.containsKey(userId);
    }

    public List<User> getOnlineUsers() {
        return new ArrayList<>(onlineUsers.values());
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }

    // ------------------ Messaging hooks ------------------
    public interface MessageSender {
        void send(int userId, String message);
    }

    public void setMessageSender(MessageSender sender) {
        this.messageSender = sender;
    }

    public void sendToUser(int userId, String message) {
        if (messageSender != null) messageSender.send(userId, message);
    }

    public void sendToUsers(int userId1, int userId2, String message) {
        sendToUser(userId1, message);
        if (userId2 != userId1) sendToUser(userId2, message);
    }

    // ------------------ Score update ------------------
    public void addScore(int userId, int delta) {
        User user = onlineUsers.get(userId);
        if (user != null) {
            user.setScore(user.getScore() + delta);
        } else {
            User dbUser = userDAO.getUserById(userId);
            if (dbUser != null) {
                dbUser.setScore(dbUser.getScore() + delta);
                // Persist if needed (not implemented in this simplified version)
            }
        }
    }
}
