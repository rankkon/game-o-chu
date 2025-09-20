package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.User;
import util.DBConnection;

public class UserDAO {
    
    public User login(String username, String password) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM users WHERE Username=? AND Password=? AND Blocked=0";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);  // Note: In production, you should use hashed passwords
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUserById(int id) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM users WHERE ID=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUserByUsername(String username) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM users WHERE Username=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            String sql = "SELECT * FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public boolean register(String username, String password) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            // Check if username already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE Username=?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, username);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Username already exists
                    }
                }
            }
            
            // Insert new user
            String insertSql = "INSERT INTO users (Username, Password, Name, Avatar, Gender, YearOfBirth, Score, MatchCount, WinCount, DrawCount, LoseCount, CurrentStreak, Rank, Blocked) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, username);
                insertPs.setString(2, password); // Store plain text password for BTL
                insertPs.setString(3, username); // Default name same as username
                insertPs.setString(4, "default_avatar.png");
                insertPs.setString(5, "Nam");
                insertPs.setInt(6, 2000);
                insertPs.setDouble(7, 1000.0);
                insertPs.setInt(8, 0);
                insertPs.setInt(9, 0);
                insertPs.setInt(10, 0);
                insertPs.setInt(11, 0);
                insertPs.setInt(12, 0);
                insertPs.setInt(13, -1);
                insertPs.setBoolean(14, false);
                
                return insertPs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("ID"));
        user.setUsername(rs.getString("Username"));
        user.setPassword(rs.getString("Password"));
        user.setAvatar(rs.getString("Avatar"));
        user.setName(rs.getString("Name"));
        user.setGender(rs.getString("Gender"));
        user.setYearOfBirth(rs.getInt("YearOfBirth"));
        user.setScore(rs.getDouble("Score"));
        user.setMatchCount(rs.getInt("MatchCount"));
        user.setWinCount(rs.getInt("WinCount"));
        user.setDrawCount(rs.getInt("DrawCount"));
        user.setLoseCount(rs.getInt("LoseCount"));
        user.setCurrentStreak(rs.getInt("CurrentStreak"));
        user.setRank(rs.getInt("Rank"));
        user.setBlocked(rs.getBoolean("Blocked"));
        return user;
    }
}