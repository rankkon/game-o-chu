package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.User;

public class ProfileDialog extends JDialog {
    private final User user;
    
    public ProfileDialog(JFrame parent, User user) {
        super(parent, "Hồ sơ người chơi", true);
        this.user = user;
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // User info
        JPanel infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Close button
        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Avatar and name at top
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel avatarLabel = new JLabel();
        
        // Load avatar if available, otherwise use placeholder
        try {
            ImageIcon avatarIcon = new ImageIcon(getClass().getResource("/images/" + user.getAvatar()));
            // Resize to reasonable dimensions if needed
            Image img = avatarIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            avatarLabel.setText("[Ảnh đại diện]");
            avatarLabel.setPreferredSize(new Dimension(100, 100));
            avatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            avatarLabel.setHorizontalAlignment(JLabel.CENTER);
        }
        avatarPanel.add(avatarLabel);
        
        JLabel nameLabel = new JLabel(user.getName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel usernameLabel = new JLabel("@" + user.getUsername(), JLabel.CENTER);
        usernameLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        usernameLabel.setForeground(Color.GRAY);
        
        JPanel namePanel = new JPanel(new GridLayout(2, 1));
        namePanel.add(nameLabel);
        namePanel.add(usernameLabel);
        
        topPanel.add(avatarPanel, BorderLayout.NORTH);
        topPanel.add(namePanel, BorderLayout.CENTER);
        
        // Basic info panel
        JPanel basicInfoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        basicInfoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin cơ bản"));
        
        basicInfoPanel.add(new JLabel("Giới tính:"));
        basicInfoPanel.add(new JLabel(user.getGender()));
        
        basicInfoPanel.add(new JLabel("Năm sinh:"));
        basicInfoPanel.add(new JLabel(String.valueOf(user.getYearOfBirth())));
        
        basicInfoPanel.add(new JLabel("Điểm số:"));
        basicInfoPanel.add(new JLabel(String.valueOf(user.getScore())));
        
        // Game stats panel
        JPanel statsPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê trận đấu"));
        
        statsPanel.add(new JLabel("Tổng số trận:"));
        statsPanel.add(new JLabel(String.valueOf(user.getMatchCount())));
        
        statsPanel.add(new JLabel("Thắng:"));
        statsPanel.add(new JLabel(String.valueOf(user.getWinCount())));
        
        statsPanel.add(new JLabel("Hòa:"));
        statsPanel.add(new JLabel(String.valueOf(user.getDrawCount())));
        
        statsPanel.add(new JLabel("Thua:"));
        statsPanel.add(new JLabel(String.valueOf(user.getLoseCount())));
        
        statsPanel.add(new JLabel("Tỷ lệ thắng:"));
        statsPanel.add(new JLabel(String.format("%.1f%%", user.getWinRate())));
        
        statsPanel.add(new JLabel("Chuỗi thắng hiện tại:"));
        statsPanel.add(new JLabel(String.valueOf(user.getCurrentStreak())));
        
        // Add all sections to main panel
        JPanel infoContainer = new JPanel(new BorderLayout(10, 10));
        infoContainer.add(topPanel, BorderLayout.NORTH);
        
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        detailsPanel.add(basicInfoPanel);
        detailsPanel.add(statsPanel);
        
        infoContainer.add(detailsPanel, BorderLayout.CENTER);
        
        panel.add(infoContainer, BorderLayout.CENTER);
        
        return panel;
    }
}