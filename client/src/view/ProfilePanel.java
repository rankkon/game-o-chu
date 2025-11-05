package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.User;

public class ProfilePanel extends JPanel {
    private final JPanel infoPanel;
    
    public ProfilePanel(ActionListener backListener) {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel chính chứa thông tin
        infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));
        
        // Panel nút điều khiển
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(backListener);
        controlPanel.add(backButton);
        
        add(infoPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    public void updateProfile(User user) {
        infoPanel.removeAll();
        
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font valueFont = new Font("Arial", Font.PLAIN, 14);
        
        // Thông tin cá nhân
        addField(infoPanel, "Tên người dùng:", user.getUsername(), labelFont, valueFont);
        addField(infoPanel, "Tên hiển thị:", user.getName(), labelFont, valueFont);
        addField(infoPanel, "Giới tính:", user.getGender(), labelFont, valueFont);
        addField(infoPanel, "Năm sinh:", String.valueOf(user.getYearOfBirth()), labelFont, valueFont);
        
        // Thông tin thành tích
        addField(infoPanel, "Điểm số:", String.format("%,.0f", user.getScore()), labelFont, valueFont);
        if (user.getRank() > 0) {
            addField(infoPanel, "Xếp hạng:", "#" + user.getRank(), labelFont, valueFont);
        }
        addField(infoPanel, "Số trận thắng:", String.format("%,d", user.getWinCount()), labelFont, valueFont);
        addField(infoPanel, "Số trận thua:", String.format("%,d", user.getLoseCount()), labelFont, valueFont);
        addField(infoPanel, "Tổng số trận:", String.format("%,d", user.getMatchCount()), labelFont, valueFont);
        
        // Tỷ lệ thắng
        if (user.getMatchCount() > 0) {
            double winRate = (user.getWinCount() * 100.0) / user.getMatchCount();
            addField(infoPanel, "Tỷ lệ thắng:", String.format("%.1f%%", winRate), labelFont, valueFont);
        }
        
        revalidate();
        repaint();
    }
    
    private void addField(JPanel panel, String label, String value, Font labelFont, Font valueFont) {
        JLabel lblField = new JLabel(label);
        lblField.setFont(labelFont);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(valueFont);
        
        panel.add(lblField);
        panel.add(lblValue);
    }
}