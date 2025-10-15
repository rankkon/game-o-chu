package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
}


