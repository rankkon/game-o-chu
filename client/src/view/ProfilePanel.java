package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.User;

public class ProfilePanel extends JPanel {
    private final JPanel infoPanel;
    private JLabel lblAvatar;
    
    public ProfilePanel(ActionListener backListener) {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tạo một panel riêng cho avatar ở bên trái
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setBorder(BorderFactory.createTitledBorder("Ảnh đại diện"));
        
        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(JLabel.CENTER);
        lblAvatar.setVerticalAlignment(JLabel.CENTER);
        // Set kích thước cố định để giữ layout ổn định
        lblAvatar.setPreferredSize(new Dimension(150, 150)); 
        avatarPanel.add(lblAvatar, BorderLayout.CENTER);
        
        // Panel chính chứa thông tin
        infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));
        
        // Panel nút điều khiển
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(backListener);
        controlPanel.add(backButton);
        
        add(avatarPanel, BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    public void updateProfile(User user) {
        loadAvatar(user.getAvatar());
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

    private void loadAvatar(String avatarFilename) {
        if (avatarFilename == null || avatarFilename.isEmpty()) {
            avatarFilename = "icons8_alien_96px.png";
        }

        String imagePath = "asset/avatar/" + avatarFilename;

        try {
            // Load ảnh trực tiếp từ đường dẫn file
            ImageIcon icon = new ImageIcon(imagePath);
            
            // Kiểm tra load ảnh
            if (icon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                System.err.println("Không tìm thấy file avatar: " + imagePath + ". Đang thử load default.");
                String defaultPath = "asset/avatar/icons8_alien_96px.png";
                icon = new ImageIcon(defaultPath);

                if (icon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                    System.err.println("Không tìm thấy file default_avatar.png tại: " + defaultPath);
                    lblAvatar.setText("No Avatar");
                    return;
                }
            }
            
            // Scale ảnh
            Dimension size = lblAvatar.getPreferredSize();
            Image image = icon.getImage().getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
            lblAvatar.setIcon(new ImageIcon(image));
            lblAvatar.setText(null);    
            
        } catch (Exception e) {
            e.printStackTrace();
            lblAvatar.setText("Lỗi load ảnh");
        }
    }
}