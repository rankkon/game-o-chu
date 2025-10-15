package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.WordInstance;
import util.DBConnection;

public class DictionaryDAO {
    public List<WordInstance> getRandomWordsByCategoryCode(String categoryCode, int limit) {
        String sql = "SELECT d.word_code, d.word_text FROM dictionary d JOIN category c ON d.category_id=c.category_id WHERE c.category_code=? ORDER BY RAND() LIMIT ?";
        List<WordInstance> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryCode);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    String text = rs.getString(2);
                    list.add(new WordInstance(code, text));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}


