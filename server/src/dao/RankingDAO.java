package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Ranking;
import util.DBConnection;
import util.Logger;

/**
 * DAO class for handling ranking-related database operations.
 * This class is read-only and only provides methods to retrieve ranking information.
 */
public class RankingDAO {
    private static RankingDAO instance;
    private final DBConnection dbConnection = DBConnection.getInstance();

    private RankingDAO() {
    }

    public static synchronized RankingDAO getInstance() {
        if (instance == null) {
            instance = new RankingDAO();
        }
        return instance;
    }

    /**
     * Retrieves the top players ordered by their total score, win count, and average remaining time.
     * @param limit The maximum number of players to retrieve
     * @return List of rankings for top players
     */
    public List<Ranking> getTopPlayers(int limit) {
        List<Ranking> rankings = new ArrayList<>();
        String sql = """
            SELECT 
                u.Username,
                u.Score as total_score,
                u.WinCount as won_matches,
                u.MatchCount as total_matches,
                ROUND(COALESCE(
                    (SELECT AVG(m2.winner_time_remaining)
                     FROM game_match m2
                     WHERE m2.winner_id = u.ID
                       AND m2.winner_time_remaining IS NOT NULL), 0
                ), 2) as avg_time_remaining
            FROM users u
            WHERE u.Blocked = 0
              AND u.MatchCount >= 0
              AND u.Name != 'Administrator'
            ORDER BY 
                u.Score DESC,
                u.WinCount DESC,
                avg_time_remaining DESC
            LIMIT ?
        """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                Ranking ranking = new Ranking(
                    rank++,
                    rs.getString("Username"),
                    rs.getInt("total_matches"),
                    rs.getInt("won_matches"),
                    rs.getInt("total_score"),
                    rs.getDouble("avg_time_remaining")
                );
                rankings.add(ranking);
            }
        } catch (SQLException e) {
            Logger.error("Error retrieving top players", e);
        }
        
        return rankings;
    }
}