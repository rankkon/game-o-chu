package view;

// Import các thành phần của Theme và các class AWT/Swing cần thiết
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension; // Cần cho Cursor.HAND_CURSOR
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout; 
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon; // Cần cho createLineBorder
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
        setBackground(Theme.COLOR_BACKGROUND);

        JPanel topPanel = new JPanel(new BorderLayout(15, 0)); 
        topPanel.setBackground(Theme.COLOR_BACKGROUND);

        JPanel avatarWrapper = new JPanel(new BorderLayout(0, 5));
        avatarWrapper.setBackground(Theme.COLOR_BACKGROUND);
        
        // Avatar
        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(JLabel.CENTER);
        lblAvatar.setVerticalAlignment(JLabel.CENTER);
        lblAvatar.setPreferredSize(new Dimension(80, 80));
        avatarWrapper.add(lblAvatar, BorderLayout.CENTER);

        changeAvatarButton = new JButton("Đổi Avatar");
        changeAvatarButton.setFont(Theme.FONT_BUTTON_SMALL.deriveFont(13f)); 
        changeAvatarButton.setForeground(Theme.COLOR_PRIMARY);
        changeAvatarButton.setOpaque(false);
        changeAvatarButton.setContentAreaFilled(false);
        changeAvatarButton.setBorderPainted(false);
        changeAvatarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changeAvatarButton.addActionListener(this::showAvatarMenu);
        avatarWrapper.add(changeAvatarButton, BorderLayout.SOUTH);

        topPanel.add(avatarWrapper, BorderLayout.WEST);

        JPanel userInfoTopPanel = new JPanel(new GridLayout(2, 1)); 
        userInfoTopPanel.setBackground(Theme.COLOR_BACKGROUND);
        
        lblUsernameTop = new JLabel("Your Name");
        lblUsernameTop.setFont(Theme.FONT_LABEL); 
        lblUsernameTop.setForeground(Theme.COLOR_TEXT_DARK);
        
        lblNameTop = new JLabel("yourname");
        lblNameTop.setFont(Theme.FONT_INPUT); 
        lblNameTop.setForeground(Theme.COLOR_TEXT_DARK.brighter()); 

        userInfoTopPanel.add(lblUsernameTop);
        userInfoTopPanel.add(lblNameTop);
        topPanel.add(userInfoTopPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // --- CONTENT PANEL: Thông tin chi tiết ---
        contentPanel = new JPanel(new GridLayout(0, 2, 50, 15)); 
        contentPanel.setBackground(Theme.COLOR_WHITE); 
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 

        lblNameValue = new JLabel();
        lblGenderValue = new JLabel();
        lblYearOfBirthValue = new JLabel();
        lblScoreValue = new JLabel();
        lblWinCountValue = new JLabel();
        lblLoseCountValue = new JLabel();
        lblMatchCountValue = new JLabel();
        lblWinRateValue = new JLabel();
        
        Font labelFont = Theme.FONT_INPUT.deriveFont(Font.BOLD); 
        Font valueFont = Theme.FONT_INPUT; 

        addDetailField(contentPanel, "Tên hiển thị", lblNameValue, labelFont, valueFont);
        addDetailField(contentPanel, "Giới tính", lblGenderValue, labelFont, valueFont);
        addDetailField(contentPanel, "Năm sinh", lblYearOfBirthValue, labelFont, valueFont);
        addDetailField(contentPanel, "Điểm số", lblScoreValue, labelFont, valueFont);
        addDetailField(contentPanel, "Số trận thắng", lblWinCountValue, labelFont, valueFont);
        addDetailField(contentPanel, "Số trận thua", lblLoseCountValue, labelFont, valueFont);
        addDetailField(contentPanel, "Tổng số trận", lblMatchCountValue, labelFont, valueFont);
        addDetailField(contentPanel, "Tỷ lệ thắng", lblWinRateValue, labelFont, valueFont);
        
        Theme.RoundedPanel roundedContentPanel = new Theme.RoundedPanel(new BorderLayout());
        roundedContentPanel.setBackground(Theme.COLOR_WHITE); 
        roundedContentPanel.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, Theme.CORNER_RADIUS));
        roundedContentPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(roundedContentPanel, BorderLayout.CENTER);


        // --- BOTTOM PANEL: Nút Quay lại ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(Theme.COLOR_BACKGROUND);

        JButton backButton = new JButton("Quay lại");
        Theme.styleButtonSecondary(backButton); 
        backButton.addActionListener(backListener);
        controlPanel.add(backButton);
        add(controlPanel, BorderLayout.SOUTH);

        createAvatarMenu();
    }
    
    public void updateProfile(User userToShow, int currentLoggedInUserId) {
        // Lưu người dùng đang xem
        this.currentUser = userToShow; 
        loadAvatar(userToShow.getAvatar());

        // Cập nhật thông tin trên Top Panel
        lblUsernameTop.setText(userToShow.getUsername()); 
        lblNameTop.setText(userToShow.getName()); 

        // Cập nhật thông tin chi tiết
        lblNameValue.setText(userToShow.getName());
        lblGenderValue.setText(userToShow.getGender());
        lblYearOfBirthValue.setText(String.valueOf(userToShow.getYearOfBirth()));
        lblScoreValue.setText(String.format("%,.0f", userToShow.getScore()));
        
        lblWinCountValue.setText(String.format("%,d", userToShow.getWinCount()));
        lblLoseCountValue.setText(String.format("%,d", userToShow.getLoseCount()));
        lblMatchCountValue.setText(String.format("%,d", userToShow.getMatchCount()));
        
        if (userToShow.getMatchCount() > 0) {
            double winRate = (userToShow.getWinCount() * 100.0) / userToShow.getMatchCount();
            lblWinRateValue.setText(String.format("%.1f%%", winRate));
        } else {
            lblWinRateValue.setText("0.0%");
        }

        if (userToShow.getId() == currentLoggedInUserId) {
            changeAvatarButton.setVisible(true);
        } else {
            changeAvatarButton.setVisible(false);
        }
        
        revalidate();
        repaint();
    }
    
    private void addDetailField(JPanel parentPanel, String labelText, JLabel valueLabel, Font labelFont, Font valueFont) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0)); 
        rowPanel.setBackground(parentPanel.getBackground()); 
        rowPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); 

        JLabel lblField = new JLabel(labelText);
        lblField.setFont(labelFont);
        lblField.setHorizontalAlignment(SwingConstants.LEFT);
        lblField.setForeground(Theme.COLOR_TEXT_DARK); 

        valueLabel.setFont(valueFont);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setForeground(Theme.COLOR_PRIMARY); 

        rowPanel.add(lblField, BorderLayout.WEST);
        rowPanel.add(valueLabel, BorderLayout.EAST);

        parentPanel.add(rowPanel);
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
        avatarMenu.setBackground(Theme.COLOR_WHITE);
        avatarMenu.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, Theme.CORNER_RADIUS));
        
        avatarGridPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        avatarGridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        avatarGridPanel.setBackground(Theme.COLOR_WHITE); 

        JScrollPane scrollPane = new JScrollPane(avatarGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setBorder(null);
        scrollPane.setBackground(Theme.COLOR_WHITE);
        scrollPane.getViewport().setBackground(Theme.COLOR_WHITE); 

        avatarMenu.add(scrollPane);
    }

    private void loadAvatarsIntoMenu() {
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

                avatarButton.setBackground(Theme.COLOR_WHITE);
                avatarButton.setBorder(BorderFactory.createLineBorder(Theme.COLOR_WHITE, 2));
                avatarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

                avatarButton.addActionListener(this::handleAvatarSelected);
                
                avatarButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        avatarButton.setBorder(BorderFactory.createLineBorder(Theme.COLOR_PRIMARY, 2));
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        avatarButton.setBorder(BorderFactory.createLineBorder(Theme.COLOR_WHITE, 2));
                    }
                });
                
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