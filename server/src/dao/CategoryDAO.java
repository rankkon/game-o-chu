package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.DBConnection;

public class CategoryDAO {
    public Integer getCategoryIdByCode(String categoryCode) {
        String sql = "SELECT category_id FROM category WHERE category_code=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCategoryNameById(int categoryId) {
        String sql = "SELECT category_name FROM category WHERE category_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<String> getAllCategoryCodes() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT category_code FROM category";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("category_code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    
    public String getRandomCategoryCode() {
        List<String> categories = getAllCategoryCodes();
        if (categories.isEmpty()) {
            return "HOAQUA"; // fallback default
        }
        int randomIndex = (int) (Math.random() * categories.size());
        return categories.get(randomIndex);
    }
}


