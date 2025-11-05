package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import util.DBConnection;

public class MatchDAO {
    public Integer insertMatchStart(int p1Id, int p2Id, String categoryName, String wordsJson, Timestamp start) {
        String sql = "INSERT INTO game_match (player1_id, player2_id, category_name, words_json, start_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p1Id);
            ps.setInt(2, p2Id);
            ps.setString(3, categoryName);
            ps.setString(4, wordsJson);
            ps.setTimestamp(5, start);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMatchEnd(int matchId, Integer winnerId, Integer loserId, int p1Score, int p2Score, Integer winnerTimeRemaining, Timestamp endTime) {
        String sql = "UPDATE game_match SET winner_id=?, loser_id=?, player1_score=?, player2_score=?, winner_time_remaining=?, end_time=? WHERE match_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (winnerId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, winnerId);
            if (loserId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, loserId);
            ps.setInt(3, p1Score);
            ps.setInt(4, p2Score);
            if (winnerTimeRemaining == null) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, winnerTimeRemaining);
            ps.setTimestamp(6, endTime);
            ps.setInt(7, matchId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void appendChatLog(int matchId, String senderName, String message, Timestamp time) {
        String chatEntry = String.format("[%s] %s: %s\n", time.toString(), senderName, message);
        
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            // Đọc chat_log hiện tại
            String currentLog = "";
            String selectSql = "SELECT chat_log FROM game_match WHERE match_id=?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, matchId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String existing = rs.getString("chat_log");
                    if (existing != null) {
                        currentLog = existing;
                    }
                }
            }

            // Thêm tin nhắn mới vào cuối
            String newLog = currentLog + chatEntry;
            
            // Cập nhật chat_log
            String updateSql = "UPDATE game_match SET chat_log=? WHERE match_id=?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newLog);
                ps.setInt(2, matchId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getChatLog(int matchId) {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT chat_log FROM game_match WHERE match_id=?")) {
            ps.setInt(1, matchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("chat_log");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<JsonObject> getMatchHistory(int userId) {
        List<JsonObject> matches = new ArrayList<>();
        String sql = "SELECT m.*, " +
                    "u1.Name as player1_name, u2.Name as player2_name, " +
                    "m.category_name, m.chat_log " +
                    "FROM game_match m " +
                    "INNER JOIN users u1 ON m.player1_id = u1.ID " +
                    "INNER JOIN users u2 ON m.player2_id = u2.ID " +
                    "WHERE m.player1_id = ? OR m.player2_id = ? " +
                    "ORDER BY m.start_time DESC " +
                    "LIMIT 20";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JsonObject match = new JsonObject();
                int matchId = rs.getInt("match_id");
                int player1Id = rs.getInt("player1_id");
                int player2Id = rs.getInt("player2_id");
                String player1Name = rs.getString("player1_name");
                String player2Name = rs.getString("player2_name");
                int player1Score = rs.getInt("player1_score");
                int player2Score = rs.getInt("player2_score");
                
                match.addProperty("matchId", matchId);
                match.addProperty("player1Name", player1Name);
                match.addProperty("player2Name", player2Name);
                match.addProperty("player1Score", player1Score);
                match.addProperty("player2Score", player2Score);
                match.addProperty("matchDate", rs.getTimestamp("start_time").getTime());
                match.addProperty("startTime", rs.getTimestamp("start_time").getTime());
                match.addProperty("category", rs.getString("category_name"));
                match.addProperty("chatLog", rs.getString("chat_log"));

                // Xác định đối thủ
                String opponentName;
                if (userId == player1Id) {
                    opponentName = player2Name;
                } else {
                    opponentName = player1Name;
                }
                match.addProperty("opponentName", opponentName);
                
                // Xác định kết quả trận đấu dựa vào winner_id
                Integer winnerId = rs.getInt("winner_id");
                if (!rs.wasNull()) {
                    // Nếu người chơi hiện tại là người thắng
                    if ((userId == player1Id && winnerId == player1Id) ||
                        (userId == player2Id && winnerId == player2Id)) {
                        match.addProperty("result", "WIN");
                    } else {
                        match.addProperty("result", "LOSE");
                    }
                } else {
                    // Nếu không có winner_id, xác định dựa vào điểm số
                    if (player1Score == player2Score) {
                        match.addProperty("result", "DRAW");
                    } else if ((userId == player1Id && player1Score > player2Score) ||
                             (userId == player2Id && player2Score > player1Score)) {
                        match.addProperty("result", "WIN");
                    } else {
                        match.addProperty("result", "LOSE");
                    }
                }
                
                matches.add(match);
            }
        } catch (SQLException e) {
            System.err.println("SQL error while fetching match history for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return matches;
    }
}


