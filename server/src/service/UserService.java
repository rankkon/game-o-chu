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

    //Cập nhật avatar cho user trong CSDL.
    public boolean updateUserAvatar(int userId, String avatarFilename) {
        // Gọi hàm đã có sẵn trong UserDAO
        return userDAO.updateAvatar(userId, avatarFilename);
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

    // Backward compatibility with username-based messaging
    public void sendToUser(String username, String message) {
        User user = getUserByUsername(username);
        if (user != null) {
            sendToUser(user.getId(), message);
        }
    }

    public void sendToUsers(String username1, String username2, String message) {
        User u1 = getUserByUsername(username1);
        User u2 = getUserByUsername(username2);
        if (u1 != null) sendToUser(u1.getId(), message);
        if (u2 != null && (u1 == null || u2.getId() != u1.getId())) sendToUser(u2.getId(), message);
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

    /**
     * Record the match result: update DB and in-memory user objects.
     * winnerScoreDelta: e.g. +100 for winner; loser gets 0.
     */
    public void recordMatchResult(int winnerId, int loserId, int winnerScoreDelta) {
        // Persist increments to DB
        try {
            // Winner: +score, +1 match, +1 win
            userDAO.updateStats(winnerId, (double) winnerScoreDelta, 1, 1, 0);
            // Loser: +0 score, +1 match, +1 lose
            userDAO.updateStats(loserId, 0.0, 1, 0, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Update in-memory online users if present
        User w = onlineUsers.get(winnerId);
        if (w != null) {
            w.setScore(w.getScore() + winnerScoreDelta);
            w.setMatchCount(w.getMatchCount() + 1);
            w.setWinCount(w.getWinCount() + 1);
        }
        User l = onlineUsers.get(loserId);
        if (l != null) {
            l.setMatchCount(l.getMatchCount() + 1);
            l.setLoseCount(l.getLoseCount() + 1);
        }
    }
}
