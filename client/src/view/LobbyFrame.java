package view;

import java.awt.BorderLayout;
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

        // Top panel
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel: game options + online users
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));

        // Left: Game options
        JPanel gameOptionsPanel = createGameOptionsPanel();
        centerPanel.add(gameOptionsPanel);

        // Right: Online users
        JPanel onlineUsersPanel = createOnlineUsersPanel();
        centerPanel.add(onlineUsersPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Đã kết nối. Chào mừng " + currentUser.getName() + "!");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new java.awt.Color(230, 230, 230));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin người chơi"));

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        Font infoFont = new Font("Arial", Font.PLAIN, 18);
        userInfoPanel.add(new JLabel("Tên: " + currentUser.getName())).setFont(infoFont);
        userInfoPanel.add(new JLabel("Điểm: " + currentUser.getScore())).setFont(infoFont);
        userInfoPanel.add(new JLabel("Xếp hạng: " + (currentUser.getRank() == -1 ? "Chưa xếp hạng" : currentUser.getRank()))).setFont(infoFont);
        panel.add(userInfoPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        Font smallButtonFont = new Font("Arial", Font.BOLD, 16);

        JButton profileButton = new JButton("Hồ sơ");
        profileButton.setFont(smallButtonFont);
        profileButton.setPreferredSize(new Dimension(120, 40));
        profileButton.addActionListener(e -> controller.viewProfile(currentUser.getId()));

        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(smallButtonFont);
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.addActionListener(e -> controller.logout());

        controlPanel.add(profileButton);
        controlPanel.add(logoutButton);
        panel.add(controlPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGameOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tùy chọn trận đấu"));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        buttonPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        Font mainButtonFont = new Font("Arial", Font.BOLD, 20);
        Dimension mainButtonSize = new Dimension(250, 50);

        JButton createGameButton = new JButton("Tạo trận đấu mới");
        createGameButton.setFont(mainButtonFont);
        createGameButton.setPreferredSize(mainButtonSize);

        JButton quickMatchButton = new JButton("Ghép đấu nhanh");
        quickMatchButton.setFont(mainButtonFont);
        quickMatchButton.setPreferredSize(mainButtonSize);

        JButton rankingButton = new JButton("Xem bảng xếp hạng");
        rankingButton.setFont(mainButtonFont);
        rankingButton.setPreferredSize(mainButtonSize);

        buttonPanel.add(createGameButton);
        buttonPanel.add(quickMatchButton);
        buttonPanel.add(rankingButton);

        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOnlineUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Người chơi trực tuyến"));

        onlineUsersModel = new DefaultListModel<>();
        onlineUsersList = new JList<>(onlineUsersModel);
        onlineUsersList.setCellRenderer(new UserListCellRenderer());
        onlineUsersList.setFixedCellHeight(45);
        onlineUsersList.setFont(new Font("Arial", Font.PLAIN, 16));
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
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        Font smallButtonFont = new Font("Arial", Font.BOLD, 16);
        Dimension smallButtonSize = new Dimension(120, 40);

        JButton inviteButton = new JButton("Mời đấu");
        inviteButton.setFont(smallButtonFont);
        inviteButton.setPreferredSize(smallButtonSize);
        inviteButton.addActionListener(e -> {
            User selectedUser = onlineUsersList.getSelectedValue();
            if (selectedUser != null && selectedUser.getId() != currentUser.getId()) {
                JOptionPane.showMessageDialog(this, "Đã gửi lời mời đấu tới " + selectedUser.getName());
            } else if (selectedUser != null && selectedUser.getId() == currentUser.getId()) {
                JOptionPane.showMessageDialog(this, "Bạn không thể tự mời chính mình!");
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người chơi trước!");
            }
        });

        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setFont(smallButtonFont);
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

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof User) {
                User user = (User) value;
                label.setText(user.getName() + " (" + user.getScore() + ")");
                if (user.getId() == currentUser.getId()) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    label.setText(label.getText() + " (Bạn)");
                }
            }

            return label;
        }
    }
}
