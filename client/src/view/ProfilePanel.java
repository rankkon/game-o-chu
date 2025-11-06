package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout; 
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder; 

import com.google.gson.JsonObject;

import controller.SocketHandler;
import model.User;

public class ProfilePanel extends JPanel {
    private final JPanel contentPanel; // Panel chính chứa avatar và thông tin
    private JLabel lblAvatar;
    private JLabel lblUsernameTop; // Thêm label cho tên người dùng ở trên cùng
    private JLabel lblNameTop;     // Thêm label cho tên hiển thị/email ở trên cùng
    
    // Labels cho thông tin chi tiết
    private JLabel lblNameValue;
    private JLabel lblGenderValue;
    private JLabel lblYearOfBirthValue;
    private JLabel lblScoreValue;
    private JLabel lblRankValue;
    private JLabel lblWinCountValue;
    private JLabel lblLoseCountValue;
    private JLabel lblMatchCountValue;
    private JLabel lblWinRateValue;

    private User currentUser; 
    private JPopupMenu avatarMenu; // Menu bật lên để chứa các avatar
    private JPanel avatarGridPanel; // Panel dạng lưới chứa các ảnh
    private JButton changeAvatarButton;

    public ProfilePanel(ActionListener backListener) {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(15, 0)); 
        
        // Avatar
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(JLabel.CENTER);
        lblAvatar.setVerticalAlignment(JLabel.CENTER);
        lblAvatar.setPreferredSize(new Dimension(80, 80));
        avatarWrapper.add(lblAvatar);
        topPanel.add(avatarWrapper, BorderLayout.WEST);

        JPanel userInfoTopPanel = new JPanel(new GridLayout(2, 1)); 
        lblUsernameTop = new JLabel("Your Name");
        lblUsernameTop.setFont(new Font("Arial", Font.BOLD, 16));
        
        lblNameTop = new JLabel("yourname");
        lblNameTop.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNameTop.setForeground(java.awt.Color.GRAY);

        userInfoTopPanel.add(lblUsernameTop);
        userInfoTopPanel.add(lblNameTop);
        topPanel.add(userInfoTopPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH); // Đặt topPanel lên trên cùng

        // --- CONTENT PANEL: Thông tin chi tiết ---
        contentPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        contentPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Khởi tạo các JLabel cho giá trị
        lblNameValue = new JLabel();
        lblGenderValue = new JLabel();
        lblYearOfBirthValue = new JLabel();
        lblScoreValue = new JLabel();
        lblRankValue = new JLabel();
        lblWinCountValue = new JLabel();
        lblLoseCountValue = new JLabel();
        lblMatchCountValue = new JLabel();
        lblWinRateValue = new JLabel();
        
        Font labelFont = new Font("Arial", Font.PLAIN, 13); // Font nhỏ hơn cho label
        Font valueFont = new Font("Arial", Font.PLAIN, 13); // Font nhỏ hơn cho value
        
        // Thêm các cặp Label/Value vào contentPanel
        addDetailField(contentPanel, "Tên hiển thị", lblNameValue, labelFont, valueFont);
        addDetailField(contentPanel, "Giới tính", lblGenderValue, labelFont, valueFont);
        addDetailField(contentPanel, "Năm sinh", lblYearOfBirthValue, labelFont, valueFont);
        addDetailField(contentPanel, "Điểm số", lblScoreValue, labelFont, valueFont);
        addDetailField(contentPanel, "Xếp hạng", lblRankValue, labelFont, valueFont);
        addDetailField(contentPanel, "Số trận thắng", lblWinCountValue, labelFont, valueFont);
        addDetailField(contentPanel, "Số trận thua", lblLoseCountValue, labelFont, valueFont);
        addDetailField(contentPanel, "Tổng số trận", lblMatchCountValue, labelFont, valueFont);
        addDetailField(contentPanel, "Tỷ lệ thắng", lblWinRateValue, labelFont, valueFont);
        
        add(contentPanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL: Nút Quay lại/Đổi avatar ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        changeAvatarButton = new JButton("Đổi Avatar");
        changeAvatarButton.setFont(new Font("Arial", Font.BOLD, 14));
        changeAvatarButton.addActionListener(this::showAvatarMenu);
        controlPanel.add(changeAvatarButton);

        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(backListener);
        controlPanel.add(backButton);
        add(controlPanel, BorderLayout.SOUTH);

        createAvatarMenu();
    }
    
    public void updateProfile(User user) {
        this.currentUser = user;
        loadAvatar(user.getAvatar());

        // Cập nhật thông tin trên Top Panel
        lblUsernameTop.setText(user.getUsername()); 
        lblNameTop.setText(user.getName()); 

        // Cập nhật thông tin chi tiết
        lblNameValue.setText(user.getName());
        lblGenderValue.setText(user.getGender());
        lblYearOfBirthValue.setText(String.valueOf(user.getYearOfBirth()));
        lblScoreValue.setText(String.format("%,.0f", user.getScore()));
        
        if (user.getRank() > 0) {
            lblRankValue.setText("#" + user.getRank());
        } else {
            lblRankValue.setText("N/A");
        }
        
        lblWinCountValue.setText(String.format("%,d", user.getWinCount()));
        lblLoseCountValue.setText(String.format("%,d", user.getLoseCount()));
        lblMatchCountValue.setText(String.format("%,d", user.getMatchCount()));
        
        if (user.getMatchCount() > 0) {
            double winRate = (user.getWinCount() * 100.0) / user.getMatchCount();
            lblWinRateValue.setText(String.format("%.1f%%", winRate));
        } else {
            lblWinRateValue.setText("0.0%");
        }
        
        revalidate();
        repaint();
    }
    
    private void addDetailField(JPanel parentPanel, String labelText, JLabel valueLabel, Font labelFont, Font valueFont) {
        JLabel lblField = new JLabel(labelText);
        lblField.setFont(labelFont);
        lblField.setHorizontalAlignment(SwingConstants.LEFT);

        valueLabel.setFont(valueFont);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        parentPanel.add(lblField);
        parentPanel.add(valueLabel);
    }

    private void loadAvatar(String avatarFilename) {
        if (avatarFilename == null || avatarFilename.isEmpty()) {
            avatarFilename = "default_avatar.png"; 
        }

        String imagePath = "client/asset/avatar/" + avatarFilename;

        try {
            ImageIcon icon = new ImageIcon(imagePath);
            
            if (icon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                System.err.println("Không tìm thấy file avatar: " + imagePath + ". Đang thử load default.");
                String defaultPath = "client/asset/avatar/default_avatar.png"; 
                icon = new ImageIcon(defaultPath);

                if (icon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                    System.err.println("Không tìm thấy file default_avatar.png tại: " + defaultPath);
                    lblAvatar.setText("No Avatar");
                    return;
                }
            }
            
            Dimension size = lblAvatar.getPreferredSize();
            Image image = icon.getImage().getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
            lblAvatar.setIcon(new ImageIcon(image));
            lblAvatar.setText(null);
            
        } catch (Exception e) {
            e.printStackTrace();
            lblAvatar.setText("Lỗi load ảnh");
        }
    }

    private void createAvatarMenu() {
        avatarMenu = new JPopupMenu();
        
        // Panel chứa các nút avatar, dùng GridLayout
        avatarGridPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        avatarGridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(avatarGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setBorder(null);

        avatarMenu.add(scrollPane);
    }

    private void loadAvatarsIntoMenu() {
        // Chỉ load nếu grid panel chưa có ảnh
        if (avatarGridPanel.getComponentCount() > 0) {
            return;
        }

        File avatarDir = new File("client/asset/avatar"); 
        if (!avatarDir.exists() || !avatarDir.isDirectory()) {
            System.err.println("Không tìm thấy thư mục: " + avatarDir.getAbsolutePath());
            avatarGridPanel.add(new JLabel("Lỗi: Không tìm thấy thư mục avatar"));
            return;
        }

        File[] avatarFiles = avatarDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".png") || 
            name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg")
        );

        if (avatarFiles == null || avatarFiles.length == 0) {
            avatarGridPanel.add(new JLabel("Không có ảnh nào"));
            return;
        }

        int previewSize = 60; 

        for (File file : avatarFiles) {
            try {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image image = icon.getImage().getScaledInstance(previewSize, previewSize, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(image);

                JButton avatarButton = new JButton(scaledIcon);
                avatarButton.setToolTipText(file.getName());
                avatarButton.setActionCommand(file.getName());
                avatarButton.setPreferredSize(new Dimension(previewSize, previewSize));

                avatarButton.addActionListener(this::handleAvatarSelected);
                
                avatarGridPanel.add(avatarButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAvatarMenu(ActionEvent e) {
        loadAvatarsIntoMenu();
        avatarMenu.show(lblAvatar, lblAvatar.getWidth(), 0);
    }

    private void handleAvatarSelected(ActionEvent e) {
        String selectedAvatar = e.getActionCommand();

        avatarMenu.setVisible(false);

        loadAvatar(selectedAvatar); 
        if (this.currentUser != null) {
            this.currentUser.setAvatar(selectedAvatar);
        } else {
            System.err.println("Lỗi: currentUser là null, không thể gửi update.");
            return;
        }

        System.out.println("Đang gửi lên server: " + selectedAvatar + " cho UserID: " + currentUser.getId());

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("avatar", selectedAvatar);
            SocketHandler.getInstance().sendMessage("UPDATE_AVATAR", payload);
            
            System.out.println("Đã gửi yêu cầu UPDATE_AVATAR.");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Lỗi khi gửi message cập nhật avatar!");
        }
    }
}