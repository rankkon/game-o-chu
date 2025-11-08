package view;

// Import tất cả các thành phần từ Theme
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import controller.LobbyController;
import model.User;

public class LobbyFrame extends JFrame {
    private final LobbyController controller;
    private final User currentUser;

    private final JLabel statusLabel;
    private JList<User> onlineUsersList;
    private DefaultListModel<User> onlineUsersModel;
    private CardLayout cardLayout;
    private JPanel centerCardPanel;
    private RankingPanel rankingPanel;
    private ProfilePanel profilePanel;
    private MatchHistoryPanel historyPanel;
    private JPanel matchmakingCardPanel;
    private CardLayout matchmakingCardLayout;
    private JButton btnMatchmake;
    private JButton btnCancelMatchmake;
    private JLabel lblMatchmakeStatus;
    private JButton inviteButton;

    public LobbyFrame(LobbyController controller, User currentUser) {
        this.controller = controller;
        this.currentUser = currentUser;

        setTitle("Sảnh chờ - Game Ô Chữ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setMinimumSize(new Dimension(1280, 720));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        // Áp dụng Theme
        mainPanel.setBackground(Theme.COLOR_BACKGROUND);

        // Top panel
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel with CardLayout
        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        // Áp dụng Theme
        centerCardPanel.setBackground(Theme.COLOR_BACKGROUND);
        centerCardPanel.setOpaque(false);

        // Game panel
        JPanel gamePanel = new JPanel(new GridLayout(1, 2, 30, 0));
        // Áp dụng Theme
        gamePanel.setBackground(Theme.COLOR_BACKGROUND);
        gamePanel.setOpaque(false); 
        
        JPanel gameOptionsPanel = createGameOptionsPanel();
        JPanel onlineUsersPanel = createOnlineUsersPanel();
        gamePanel.add(gameOptionsPanel);
        gamePanel.add(onlineUsersPanel);
        
        JScrollPane gameScrollPane = new JScrollPane(gamePanel);
        gameScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gameScrollPane.getViewport().setBackground(Theme.COLOR_BACKGROUND);
        centerCardPanel.add(gameScrollPane, "GAME");

        // Ranking panel
        rankingPanel = new RankingPanel(e -> showGamePanel());
        centerCardPanel.add(rankingPanel, "RANKING");

        // Profile panel
        profilePanel = new ProfilePanel(e -> showGamePanel());
        centerCardPanel.add(profilePanel, "PROFILE");

        // Match History panel
        historyPanel = new MatchHistoryPanel(e -> showGamePanel());
        centerCardPanel.add(historyPanel, "HISTORY");

        mainPanel.add(centerCardPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Đã kết nối. Chào mừng " + currentUser.getName() + "!");
        // Áp dụng Theme
        statusLabel.setFont(Theme.FONT_INPUT); 
        statusLabel.setForeground(Theme.COLOR_TEXT_DARK);
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Theme.COLOR_BACKGROUND); 
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
        
        // Request initial data
        controller.requestOnlineUsers();
        controller.requestRankingUpdate();
    }

    private JPanel createTopPanel() {
        JPanel panel = new Theme.RoundedPanel(new BorderLayout());
        panel.setBackground(Theme.COLOR_WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        // Áp dụng Theme
        userInfoPanel.setBackground(Theme.COLOR_WHITE); 
        
        JLabel nameLabel = new JLabel("Tên: " + currentUser.getName());
        nameLabel.setFont(Theme.FONT_INPUT);
        nameLabel.setForeground(Theme.COLOR_TEXT_DARK);
        
        JLabel scoreLabel = new JLabel("Điểm: " + currentUser.getScore());
        scoreLabel.setFont(Theme.FONT_INPUT);
        scoreLabel.setForeground(Theme.COLOR_TEXT_DARK);
        
        JLabel rankLabel = new JLabel("Xếp hạng: " + (currentUser.getRank() == -1 ? "Chưa xếp hạng" : currentUser.getRank()));
        rankLabel.setFont(Theme.FONT_INPUT);
        rankLabel.setForeground(Theme.COLOR_TEXT_DARK);

        userInfoPanel.add(nameLabel);
        userInfoPanel.add(scoreLabel);
        userInfoPanel.add(rankLabel);
        panel.add(userInfoPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        controlPanel.setBackground(Theme.COLOR_WHITE); 

        JButton profileButton = new JButton("Hồ sơ");
        Theme.styleButtonSecondary(profileButton); 
        profileButton.setPreferredSize(new Dimension(120, 40)); // Điều chỉnh kích thước nếu cần
        profileButton.addActionListener(e -> controller.viewProfile(currentUser.getId()));

        JButton logoutButton = new JButton("Đăng xuất");
        Theme.styleButtonSecondary(logoutButton); 
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.addActionListener(e -> controller.logout());

        controlPanel.add(profileButton);
        controlPanel.add(logoutButton);
        panel.add(controlPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGameOptionsPanel() {
        JPanel panel = new Theme.RoundedPanel(new BorderLayout());
        panel.setBackground(Theme.COLOR_WHITE);
        
        JLabel titleLabel = new JLabel("Tùy chọn trận đấu", JLabel.CENTER);
        titleLabel.setFont(Theme.FONT_LABEL);
        titleLabel.setForeground(Theme.COLOR_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.add(titleLabel, BorderLayout.NORTH);

        matchmakingCardLayout = new CardLayout();
        matchmakingCardPanel = new JPanel(matchmakingCardLayout);
        matchmakingCardPanel.setBackground(Theme.COLOR_WHITE); 

        Dimension mainButtonSize = new Dimension(250, 50);

        // --- Panel chờ ---
        JPanel matchmakeIdlePanel = new JPanel(new BorderLayout());
        matchmakeIdlePanel.setBackground(Theme.COLOR_WHITE);
        matchmakeIdlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        btnMatchmake = new JButton("Ghép đấu");

        Theme.styleButtonPrimary(btnMatchmake); 
        btnMatchmake.setBackground(Theme.COLOR_ACCENT); 
        btnMatchmake.setUI(new Theme.RoundedButtonUI()); 
        ((Theme.RoundedBorder)btnMatchmake.getBorder()).setColor(Theme.COLOR_ACCENT);
        btnMatchmake.addMouseListener(createHoverEffect(btnMatchmake, Theme.COLOR_ACCENT));
        
        btnMatchmake.setPreferredSize(mainButtonSize);
        btnMatchmake.addActionListener(e -> controller.requestMatchmaking());
        matchmakeIdlePanel.add(btnMatchmake, BorderLayout.CENTER);

        // --- Panel đang tìm trận ---
        JPanel matchmakeWaitingPanel = new JPanel(new BorderLayout(10, 10));
        matchmakeWaitingPanel.setBackground(Theme.COLOR_WHITE);
        matchmakeWaitingPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        btnCancelMatchmake = new JButton("Hủy tìm trận");

        Theme.styleButtonPrimary(btnCancelMatchmake); 
        Color cancelColor = new Color(220, 53, 69);
        btnCancelMatchmake.setBackground(cancelColor);
        btnCancelMatchmake.setUI(new Theme.RoundedButtonUI()); 
        ((Theme.RoundedBorder)btnCancelMatchmake.getBorder()).setColor(cancelColor);
        btnCancelMatchmake.addMouseListener(createHoverEffect(btnCancelMatchmake, cancelColor));

        btnCancelMatchmake.setPreferredSize(mainButtonSize);
        btnCancelMatchmake.addActionListener(e -> controller.cancelMatchmaking());

        lblMatchmakeStatus = new JLabel("Đang tìm trận...", JLabel.CENTER);
 
        lblMatchmakeStatus.setFont(Theme.FONT_INPUT); 
        lblMatchmakeStatus.setForeground(Theme.COLOR_TEXT_DARK);

        matchmakeWaitingPanel.add(lblMatchmakeStatus, BorderLayout.NORTH);
        matchmakeWaitingPanel.add(btnCancelMatchmake, BorderLayout.CENTER);

        matchmakingCardPanel.add(matchmakeIdlePanel, "IDLE");
        matchmakingCardPanel.add(matchmakeWaitingPanel, "WAITING");
        matchmakingCardLayout.show(matchmakingCardPanel, "IDLE");

        // --- Panel chứa các nút còn lại ---
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        buttonPanel.setBackground(Theme.COLOR_WHITE);
        buttonPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JButton historyButton = new JButton("Lịch sử trận đấu");

        Theme.styleButtonPrimary(historyButton); 
        historyButton.setBackground(Theme.COLOR_ACCENT);
        historyButton.setUI(new Theme.RoundedButtonUI()); 
        ((Theme.RoundedBorder)historyButton.getBorder()).setColor(Theme.COLOR_ACCENT);
        historyButton.addMouseListener(createHoverEffect(historyButton, Theme.COLOR_ACCENT));
        
        historyButton.setPreferredSize(mainButtonSize);
        historyButton.addActionListener(e -> controller.showMatchHistory());

        JButton rankingButton = new JButton("Xem bảng xếp hạng");

        Theme.styleButtonPrimary(rankingButton); 
        rankingButton.setBackground(Theme.COLOR_ACCENT); 
        rankingButton.setUI(new Theme.RoundedButtonUI());
        ((Theme.RoundedBorder)rankingButton.getBorder()).setColor(Theme.COLOR_ACCENT);
        rankingButton.addMouseListener(createHoverEffect(rankingButton, Theme.COLOR_ACCENT));

        rankingButton.setPreferredSize(mainButtonSize);
        rankingButton.addActionListener(e -> showRankingPanel());

        buttonPanel.add(matchmakingCardPanel);
        buttonPanel.add(historyButton);
        buttonPanel.add(rankingButton);

        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private MouseAdapter createHoverEffect(JButton button, Color baseColor) {
        return new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(baseColor.darker());
                button.repaint();
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(baseColor);
                button.repaint();
            }
        };
    }

    private JPanel createOnlineUsersPanel() {
        JPanel panel = new Theme.RoundedPanel(new BorderLayout());
        panel.setBackground(Theme.COLOR_WHITE);

        JLabel titleLabel = new JLabel("Người chơi trực tuyến", JLabel.CENTER);
        titleLabel.setFont(Theme.FONT_LABEL);
        titleLabel.setForeground(Theme.COLOR_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.add(titleLabel, BorderLayout.NORTH);

        onlineUsersModel = new DefaultListModel<>();
        onlineUsersList = new JList<>(onlineUsersModel);
        onlineUsersList.setCellRenderer(new UserListCellRenderer());
        onlineUsersList.setFixedCellHeight(45);

        onlineUsersList.setFont(Theme.FONT_INPUT); 
        onlineUsersList.setBackground(Theme.COLOR_WHITE);
        onlineUsersList.setForeground(Theme.COLOR_TEXT_DARK);
        onlineUsersList.setSelectionBackground(Theme.COLOR_ACCENT);
        onlineUsersList.setSelectionForeground(Theme.COLOR_TEXT_DARK);
        
        onlineUsersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = onlineUsersList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        User selectedUser = onlineUsersList.getModel().getElementAt(index);
                        controller.viewProfile(selectedUser.getId());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(onlineUsersList);

        scrollPane.getViewport().setBackground(Theme.COLOR_WHITE);
        scrollPane.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, Theme.CORNER_RADIUS));
        scrollPane.setBackground(Theme.COLOR_WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));

        actionPanel.setBackground(Theme.COLOR_WHITE); 
        
        Dimension smallButtonSize = new Dimension(140, 45); 

        inviteButton = new JButton("Mời đấu");

        Theme.styleButtonPrimary(inviteButton); 
        inviteButton.setPreferredSize(smallButtonSize);
        inviteButton.addActionListener(e -> {
            User selectedUser = onlineUsersList.getSelectedValue();
            if (selectedUser != null && selectedUser.getId() != currentUser.getId()) {
                controller.sendInvite(selectedUser.getId());
                showInfo("Đã gửi lời mời đấu tới " + selectedUser.getName());
            } else if (selectedUser != null && selectedUser.getId() == currentUser.getId()) {
                showError("Bạn không thể tự mời chính mình!");
            } else {
                showError("Vui lòng chọn một người chơi trước!");
            }
        });

        JButton refreshButton = new JButton("Làm mới");

        Theme.styleButtonSecondary(refreshButton); 
        refreshButton.setPreferredSize(smallButtonSize);
        refreshButton.addActionListener(e -> controller.requestOnlineUsers());

        actionPanel.add(inviteButton);
        actionPanel.add(refreshButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void updateOnlineUsers(List<User> users) {
        onlineUsersModel.clear();
        for (User user : users) {
            onlineUsersModel.addElement(user);
        }
    }

    public void setMatchmakingStatus(boolean isWaiting) {
        if (controller != null && controller.getReconnectMatchId() != null) {
            matchmakingCardLayout.show(matchmakingCardPanel, "IDLE");
            return; 
        }
        if (isWaiting) {
            matchmakingCardLayout.show(matchmakingCardPanel, "WAITING");
        } else {
            matchmakingCardLayout.show(matchmakingCardPanel, "IDLE");
        }
    }

    public void updateButtonsForReconnect(String reconnectMatchId) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            
            // TRƯỜNG HỢP 1: CÓ TRẬN ĐỂ KẾT NỐI LẠI
            if (reconnectMatchId != null) {
                if (inviteButton != null) {
                    inviteButton.setVisible(false);
                }
                matchmakingCardLayout.show(matchmakingCardPanel, "IDLE");
                btnMatchmake.setText("Kết nối lại trận đấu");
                
                // Đảm bảo nút có màu đỏ cảnh báo
                btnMatchmake.setBackground(Color.RED.darker());
                btnMatchmake.setUI(new Theme.RoundedButtonUI());
                ((Theme.RoundedBorder)btnMatchmake.getBorder()).setColor(Color.RED.darker());
                btnMatchmake.addMouseListener(createHoverEffect(btnMatchmake, Color.RED.darker()));

                for (java.awt.event.ActionListener al : btnMatchmake.getActionListeners()) {
                    btnMatchmake.removeActionListener(al);
                }
                btnMatchmake.addActionListener(e -> controller.requestReconnect());

            } 
            // TRƯỜNG HỢP 2: TRẠNG THÁI BÌNH THƯỜNG
            else { 
                if (inviteButton != null) {
                    inviteButton.setVisible(true);
                }
                for (java.awt.event.ActionListener al : btnMatchmake.getActionListeners()) {
                    btnMatchmake.removeActionListener(al);
                }
                btnMatchmake.addActionListener(e -> controller.requestMatchmaking());
                btnMatchmake.setText("Ghép đấu"); 

                btnMatchmake.setBackground(Theme.COLOR_PRIMARY);
                btnMatchmake.setUI(new Theme.RoundedButtonUI());
                ((Theme.RoundedBorder)btnMatchmake.getBorder()).setColor(Theme.COLOR_PRIMARY);
                btnMatchmake.addMouseListener(createHoverEffect(btnMatchmake, Theme.COLOR_PRIMARY));

                setMatchmakingStatus(false); 
            }
        });
    }

    public void showError(String message) {
        JLabel label = new JLabel(message);
        label.setFont(Theme.FONT_INPUT);
        JOptionPane.showMessageDialog(this, label, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JLabel label = new JLabel(message);
        label.setFont(Theme.FONT_INPUT);
        JOptionPane.showMessageDialog(this, label, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showGamePanel() {
        cardLayout.show(centerCardPanel, "GAME");
    }

    private void showRankingPanel() {
        controller.requestRankingUpdate();
        cardLayout.show(centerCardPanel, "RANKING");
    }

    public void showProfile(User user) {
        profilePanel.updateProfile(user);
        cardLayout.show(centerCardPanel, "PROFILE");
    }

    public void updateRankings(List<model.Ranking> rankings) {
        if (rankings != null && !rankings.isEmpty()) {
            rankingPanel.updateRankings(rankings);
            rankingPanel.highlightCurrentPlayer(currentUser.getUsername());
        }
    }

    public void showMatchHistory(List<model.Match> matches) {
        if (matches != null) {
            historyPanel.updateMatchHistory(matches);
            cardLayout.show(centerCardPanel, "HISTORY");
        }
    }

    // Tùy chỉnh Cell Renderer để áp dụng Theme
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            label.setFont(Theme.FONT_INPUT);
            label.setBorder(new EmptyBorder(5, 15, 5, 15));

            if (value instanceof User) {
                User user = (User) value;
                label.setText(user.getName() + " (" + user.getScore() + ")");
                
                if (user.getId() == currentUser.getId()) {
                    label.setFont(Theme.FONT_INPUT.deriveFont(Font.BOLD));
                    label.setText(label.getText() + " (Bạn)");
                }
            }          
            return label;
        }
    }
}