package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonNull;
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

    public void updateMatchEnd(int matchId, Integer winnerId, Integer loserId, int p1Score, int p2Score, Timestamp endTime) {
        String sql = "UPDATE game_match SET winner_id=?, loser_id=?, player1_score=?, player2_score=?, end_time=? WHERE match_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (winnerId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, winnerId);
            if (loserId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, loserId);
            ps.setInt(3, p1Score);
            ps.setInt(4, p2Score);
            ps.setTimestamp(5, endTime);
            ps.setInt(6, matchId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                match.addProperty("player1Id", player1Id);
                match.addProperty("player2Id", player2Id);
                match.addProperty("player1Name", player1Name);
                match.addProperty("player2Name", player2Name);
                match.addProperty("player1Score", player1Score);
                match.addProperty("player2Score", player2Score);
                match.addProperty("matchDate", rs.getTimestamp("start_time").getTime());
                match.addProperty("startTime", rs.getTimestamp("start_time").getTime());
                match.addProperty("category", rs.getString("category_name"));
                match.addProperty("chatLog", rs.getString("chat_log"));
                
                Integer winnerId = rs.getInt("winner_id");
                if (rs.wasNull()) {
                    match.add("winnerId", com.google.gson.JsonNull.INSTANCE);
                } else {
                    match.addProperty("winnerId", winnerId);
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


