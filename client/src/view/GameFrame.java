/*
 * Giao diện GameFrame được viết lại
 * Bỏ qua code auto-gen của NetBeans, sử dụng layout manager tiêu chuẩn.
 */
package view;

// Import Theme
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel; // Import layout mới
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane; // Import layout mới
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import controller.SocketHandler;

/**
 *
 * @author Admin
 */
public class GameFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GameFrame.class.getName());
    
    // Game state variables
    private String roomId = "";
    private int selfUserId = 0;
    private SocketHandler socketHandler;
    private final List<List<JTextField>> wordInputs = new ArrayList<>();
    // Countdown timer fields
    private javax.swing.Timer countdownTimer;
    private long matchEndTime = 0L; // epoch millis when match ends

    // --- BIẾN KHAI BÁO BẰNG TAY (THAY CHO NetBeans) ---
    private javax.swing.JPanel boardPanel;
    private javax.swing.JButton btnSend;
    private javax.swing.JPanel chatPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1; // (Không dùng nhưng giữ lại)
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblName1;
    private javax.swing.JLabel lblName2;
    private javax.swing.JLabel lblScore1;
    private javax.swing.JLabel lblScore2;
    private javax.swing.JTextArea txtChatArea;
    private javax.swing.JTextField txtMessage;
    
    // Panel phụ để bố cục
    private JPanel eastPanel;
    private JPanel playerPanel;
    private JPanel namePanel;
    private JPanel scorePanel;
    private JPanel chatInputPanel;
    // --- KẾT THÚC BIẾN KHAI BÁO ---


    /**
     * Creates new form GameFrame
     */
    public GameFrame() {
        // Bỏ initComponents(), gọi hàm viết tay
        initManualComponents(); 
        applyTheme();
        
        // Ensure boardPanel can host dynamic grids in the left area
        boardPanel.setLayout(new java.awt.BorderLayout());
        renderPlaceholderBoard();
    }

    /**
     * Khởi tạo thủ công các components, thay thế cho initComponents()
     */
    private void initManualComponents() {
        setTitle("Game Ô Chữ");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 720); // Đặt kích thước cửa sổ
        setLocationRelativeTo(null);

        // Layout chính của Frame
        getContentPane().setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. boardPanel (Bên trái)
        boardPanel = new JPanel();
        getContentPane().add(boardPanel, BorderLayout.CENTER);

        // 2. eastPanel (Bên phải, chứa info và chat)
        eastPanel = new JPanel(new BorderLayout(10, 10));
        eastPanel.setPreferredSize(new Dimension(280, 700)); // Đặt chiều rộng cố định
        
        // 3. infoPanel (Bên trên-phải)
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        jLabel1 = new JLabel("Chức năng");
        jButton1 = new JButton("Thoát phòng");
        jButton1.addActionListener(this::jButton1ActionPerformed); // Gán sự kiện

        jLabel2 = new JLabel("Người chơi");

        // Panel con cho 2 avatar và chữ 'x'
        playerPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        jLabel3 = new JLabel("Avt 1", SwingConstants.CENTER);
        jLabel5 = new JLabel("x", SwingConstants.CENTER);
        jLabel4 = new JLabel("Avt 2", SwingConstants.CENTER);
        playerPanel.add(jLabel3);
        playerPanel.add(jLabel5);
        playerPanel.add(jLabel4);

        // Panel con cho 2 tên
        namePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        lblName1 = new JLabel("tên 1", SwingConstants.CENTER);
        lblName2 = new JLabel("tên 2", SwingConstants.CENTER);
        namePanel.add(lblName1);
        namePanel.add(lblName2);
        
        // Panel con cho 2 điểm
        scorePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        lblScore1 = new JLabel("score 1", SwingConstants.CENTER);
        lblScore2 = new JLabel("score 2", SwingConstants.CENTER);
        scorePanel.add(lblScore1);
        scorePanel.add(lblScore2);
        
        jLabel6 = new JLabel("Thời gian");
        jLabel7 = new JLabel("00:00");

        // Thêm các thành phần vào infoPanel (với các khoảng đệm)
        infoPanel.add(jLabel1);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(jButton1);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        infoPanel.add(jLabel2);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(playerPanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(namePanel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(scorePanel);
        infoPanel.add(Box.createVerticalGlue()); // Đẩy các thành phần sau xuống dưới
        infoPanel.add(jLabel6);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(jLabel7);

        // 4. chatPanel (Bên dưới-phải)
        chatPanel = new JPanel(new BorderLayout(5, 5));
        chatPanel.setPreferredSize(new Dimension(280, 300)); // Chiều cao cố định cho chat
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        jLabel8 = new JLabel("Nhắn tin", SwingConstants.CENTER);
        chatPanel.add(jLabel8, BorderLayout.NORTH);

        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setColumns(20);
        txtChatArea.setLineWrap(true);
        txtChatArea.setRows(5);
        txtChatArea.setWrapStyleWord(true);
        jScrollPane1 = new JScrollPane(txtChatArea);
        chatPanel.add(jScrollPane1, BorderLayout.CENTER);

        // Panel con cho ô nhập và nút gửi
        chatInputPanel = new JPanel(new BorderLayout(5, 0));
        txtMessage = new JTextField();
        txtMessage.addActionListener(this::txtMessageActionPerformed); // Gán sự kiện

        btnSend = new JButton("Gửi");
        btnSend.addActionListener(this::btnSendActionPerformed); // Gán sự kiện

        chatInputPanel.add(txtMessage, BorderLayout.CENTER);
        chatInputPanel.add(btnSend, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);

        // Thêm info và chat vào panel bên phải
        eastPanel.add(infoPanel, BorderLayout.CENTER);
        eastPanel.add(chatPanel, BorderLayout.SOUTH);

        // Thêm panel bên phải vào layout chính
        getContentPane().add(eastPanel, BorderLayout.EAST);

        // (jPanel1 không dùng đến)
        jPanel1 = new JPanel();
    }


    /**
     * Áp dụng Theme - ĐÃ XÓA BỎ CÁC HACK setPreferredSize
     */
    private void applyTheme() {
        // Nền chính
        getContentPane().setBackground(Theme.COLOR_BACKGROUND);
        boardPanel.setBackground(Theme.COLOR_BACKGROUND);
        infoPanel.setBackground(Theme.COLOR_BACKGROUND);
        chatPanel.setBackground(Theme.COLOR_BACKGROUND);
        // Nền panel phụ
        eastPanel.setBackground(Theme.COLOR_BACKGROUND);
        playerPanel.setBackground(Theme.COLOR_BACKGROUND);
        namePanel.setBackground(Theme.COLOR_BACKGROUND);
        scorePanel.setBackground(Theme.COLOR_BACKGROUND);
        chatInputPanel.setBackground(Theme.COLOR_BACKGROUND);

        // === Panel Thông Tin ===
        jLabel1.setFont(Theme.FONT_BUTTON_SMALL); 
        jLabel1.setForeground(Theme.COLOR_PRIMARY);

        Theme.styleButtonPrimary(jButton1);
        Color exitButtonColor = new Color(220, 53, 69); 
        jButton1.setBackground(exitButtonColor);
        jButton1.setUI(new Theme.RoundedButtonUI()); 
        ((Theme.RoundedBorder)jButton1.getBorder()).setColor(exitButtonColor);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton1.setBackground(exitButtonColor.darker());
                jButton1.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton1.setBackground(exitButtonColor);
                jButton1.repaint();
            }
        });

        jLabel2.setFont(Theme.FONT_BUTTON_SMALL); 
        jLabel2.setForeground(Theme.COLOR_PRIMARY);
        
        jLabel3.setText("AV1"); // Tạm
        jLabel4.setText("AV2"); // Tạm
        jLabel5.setFont(Theme.FONT_LABEL); 
        jLabel5.setForeground(Theme.COLOR_TEXT_DARK);

        jLabel6.setFont(Theme.FONT_BUTTON_SMALL); 
        jLabel6.setForeground(Theme.COLOR_PRIMARY);

        jLabel7.setFont(Theme.FONT_LABEL); 
        jLabel7.setForeground(Theme.COLOR_TEXT_DARK);
        
        Font playerInfoFont = Theme.FONT_INPUT.deriveFont(16f);
        Font playerScoreFont = Theme.FONT_INPUT.deriveFont(Font.BOLD, 16f);
        
        lblName1.setFont(playerInfoFont);
        lblName1.setForeground(Theme.COLOR_TEXT_DARK);
        lblName2.setFont(playerInfoFont);
        lblName2.setForeground(Theme.COLOR_TEXT_DARK);
        
        lblScore1.setFont(playerScoreFont);
        lblScore1.setForeground(Theme.COLOR_PRIMARY);
        lblScore2.setFont(playerScoreFont);
        lblScore2.setForeground(Theme.COLOR_PRIMARY);

        // === Panel Chat ===
        jLabel8.setFont(Theme.FONT_BUTTON_SMALL); 
        jLabel8.setForeground(Theme.COLOR_PRIMARY);
        
        txtChatArea.setFont(Theme.FONT_INPUT.deriveFont(16f)); 
        txtChatArea.setBackground(Theme.COLOR_WHITE);
        txtChatArea.setForeground(Theme.COLOR_TEXT_DARK);
        txtChatArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        jScrollPane1.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, Theme.CORNER_RADIUS));
        jScrollPane1.getViewport().setBackground(Theme.COLOR_WHITE);
        
        txtMessage.setFont(Theme.FONT_INPUT.deriveFont(16f)); 
        txtMessage.setForeground(Theme.COLOR_TEXT_DARK);
        txtMessage.setBackground(Theme.COLOR_WHITE);
        txtMessage.setBorder(Theme.BORDER_ROUNDED_INPUT);
        
        Theme.styleButtonPrimary(btnSend);
        btnSend.setPreferredSize(new Dimension(80, 40)); // Cho nút Gửi kích thước hợp lý
    }
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        JLabel label = new JLabel("Bạn có chắc chắn muốn thoát phòng không?");
        label.setFont(Theme.FONT_INPUT);
        int result = JOptionPane.showConfirmDialog(rootPane, label,
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // Send leave room message to server
            if (socketHandler != null) {
                JsonObject payload = new JsonObject();
                payload.addProperty("roomId", roomId);
                socketHandler.sendMessage("REQUEST_EXIT_MATCH", payload);
            }
            setVisible(false);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMessageActionPerformed
        sendChatMessage();
    }//GEN-LAST:event_txtMessageActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        sendChatMessage();
    }//GEN-LAST:event_btnSendActionPerformed

    private void sendChatMessage() {
        String message = txtMessage.getText().trim();
        if (!message.isEmpty()) {
            txtMessage.setText(""); // xóa ô nhập
            
            // Send chat message to server
            if (socketHandler != null) {
                JsonObject payload = new JsonObject();
                payload.addProperty("roomId", roomId);
                payload.addProperty("message", message);
                socketHandler.sendMessage("CHAT_MESSAGE", payload);
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new GameFrame().setVisible(true));
    }

    // Game methods for handling match data
    public void loadMatchStart(JsonObject envelope) {
        this.roomId = envelope.has("roomId") ? envelope.get("roomId").getAsString() : this.roomId;
        JsonObject data = envelope.has("data") ? envelope.get("data").getAsJsonObject() : envelope;
        renderWords(data);
        updateScores(data);
        updateTimer(data);
    }

    public void loadMatchUpdate(JsonObject envelope) {
        JsonObject data = envelope.has("data") ? envelope.get("data").getAsJsonObject() : envelope;
        refreshFilled(data);
        updateScores(data);
        updateTimer(data);
    }

    private void renderWords(JsonObject data) {
        boardPanel.removeAll();
        wordInputs.clear();

        JsonArray words = data.getAsJsonArray("words");
        if (words == null || words.size() == 0) {
            renderPlaceholderBoard();
            boardPanel.revalidate();
            boardPanel.repaint();
            return;
        }

        JPanel wordsContainer = new JPanel();
        wordsContainer.setLayout(new GridLayout(words.size(), 1, 5, 5));
        wordsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wordsContainer.setBackground(Theme.COLOR_BACKGROUND);

        JsonObject playerRevealed = data.has("playerRevealed") ? data.getAsJsonObject("playerRevealed") : null;
        JsonArray myRevealedArray = null;
        if (playerRevealed != null && selfUserId > 0) {
            String key = String.valueOf(selfUserId);
            if (playerRevealed.has(key)) {
                try { myRevealedArray = playerRevealed.getAsJsonArray(key); } catch (Exception ignored) {}
            }
        }

        for (int wi = 0; wi < words.size(); wi++) {
            JsonObject w = words.get(wi).getAsJsonObject();
            String hint = w.has("hint") ? w.get("hint").getAsString() : "";
            int len = w.has("length") ? w.get("length").getAsInt() : 0;

            Theme.RoundedPanel row = new Theme.RoundedPanel(new BorderLayout(10, 10));
            row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            row.setBackground(Theme.COLOR_WHITE);
            
            JLabel hintLabel = new JLabel(hint);
            hintLabel.setPreferredSize(new Dimension(200, 30));
            hintLabel.setFont(Theme.FONT_INPUT); 
            hintLabel.setForeground(Theme.COLOR_TEXT_DARK);
            hintLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            row.add(hintLabel, BorderLayout.WEST);

            JPanel cells = new JPanel(new GridLayout(1, Math.max(1, len), 3, 3));
            cells.setBackground(Theme.COLOR_WHITE);
            List<JTextField> inputs = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                JTextField tf = new JTextField(1);
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(Theme.FONT_INPUT);
                tf.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, 8)); 
                tf.setPreferredSize(new Dimension(35, 35));
                tf.setBackground(Theme.COLOR_WHITE);
                
                String revealed = null;
                if (myRevealedArray != null && wi < myRevealedArray.size()) {
                    try { revealed = myRevealedArray.get(wi).getAsString(); } catch (Exception ignored) { revealed = null; }
                }
                if ((revealed == null || revealed.isEmpty()) && w.has("revealed")) {
                    revealed = w.get("revealed").getAsString();
                }

                if (revealed != null && revealed.length() > i) {
                    char rc = revealed.charAt(i);
                    if (rc != '_' && rc != '\u0000') {
                        tf.setText(String.valueOf(rc));
                        tf.setEditable(false);
                        // Sửa màu ô đã lộ
                        tf.setBackground(Theme.COLOR_BACKGROUND); 
                        tf.setForeground(Theme.COLOR_TEXT_DARK);
                    }
                }
                final int wordIdx = wordInputs.size();
                final int charIdx = i;
                tf.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (!tf.isEditable()) { e.consume(); return; }
                        char ch = e.getKeyChar();
                        if (Character.isLetter(ch)) {
                            tf.setText(("" + ch).toUpperCase());
                            tf.setBackground(Theme.COLOR_ACCENT.brighter());
                            sendMatchInput(wordIdx, charIdx, Character.toUpperCase(ch));
                            SwingUtilities.invokeLater(() -> focusNext(inputs, charIdx));
                            e.consume();
                        } else {
                            e.consume();
                        }
                    }
                    
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                            if (!tf.isEditable()) { e.consume(); return; }
                            tf.setText("");
                            tf.setBackground(Color.WHITE);
                            SwingUtilities.invokeLater(() -> focusPrevious(inputs, charIdx));
                        }
                    }
                });

                inputs.add(tf);
                cells.add(tf);
            }
            row.add(cells, BorderLayout.CENTER);
            wordsContainer.add(row);
            wordInputs.add(inputs);
        }

        boardPanel.add(wordsContainer, java.awt.BorderLayout.CENTER);
        boardPanel.revalidate();
        boardPanel.repaint();
        normalizeUpperLowerLabels(boardPanel);
    }

    private void renderPlaceholderBoard() {
        boardPanel.removeAll();
        wordInputs.clear();

        javax.swing.JPanel wordsContainer = new javax.swing.JPanel();
        wordsContainer.setLayout(new java.awt.GridLayout(5, 1, 5, 5));
        wordsContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wordsContainer.setBackground(Theme.COLOR_BACKGROUND);

        for (int r = 0; r < 5; r++) {
            Theme.RoundedPanel row = new Theme.RoundedPanel(new java.awt.BorderLayout(10, 10));
            row.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
            row.setBackground(Theme.COLOR_WHITE);

            javax.swing.JLabel hintLabel = new javax.swing.JLabel("Từ " + (r + 1));
            hintLabel.setPreferredSize(new java.awt.Dimension(200, 30));
            hintLabel.setFont(Theme.FONT_INPUT); 
            hintLabel.setForeground(Theme.COLOR_TEXT_DARK);
            hintLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
            row.add(hintLabel, java.awt.BorderLayout.WEST);

            int len = 8;
            javax.swing.JPanel cells = new javax.swing.JPanel(new java.awt.GridLayout(1, len, 3, 3));
            cells.setBackground(Theme.COLOR_WHITE);
            java.util.List<javax.swing.JTextField> inputs = new java.util.ArrayList<>();
            for (int i = 0; i < len; i++) {
                javax.swing.JTextField tf = new javax.swing.JTextField(1);
                tf.setHorizontalAlignment(javax.swing.JTextField.CENTER);
                tf.setFont(Theme.FONT_INPUT); 
                tf.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, 8)); 
                tf.setPreferredSize(new java.awt.Dimension(35, 35));
                tf.setBackground(java.awt.Color.WHITE);
                tf.setText("");
                inputs.add(tf);
                cells.add(tf);
            }
            row.add(cells, java.awt.BorderLayout.CENTER);
            wordsContainer.add(row);
            wordInputs.add(inputs);
        }

        boardPanel.add(wordsContainer, java.awt.BorderLayout.CENTER);
        boardPanel.revalidate();
        boardPanel.repaint();
        normalizeUpperLowerLabels(boardPanel);
    }

    private void normalizeUpperLowerLabels(java.awt.Container root) {
        for (java.awt.Component comp : root.getComponents()) {
            if (comp instanceof javax.swing.JLabel) {
                javax.swing.JLabel lbl = (javax.swing.JLabel) comp;
                String t = lbl.getText();
                if (t != null && t.length() == 2) {
                    char a = t.charAt(0);
                    char b = t.charAt(1);
                    if (Character.isUpperCase(a) && Character.isLowerCase(b) && Character.toUpperCase(b) == a) {
                        lbl.setText(String.valueOf(a));
                    }
                }
            } else if (comp instanceof java.awt.Container) {
                normalizeUpperLowerLabels((java.awt.Container) comp);
            }
        }
    }

    private void refreshFilled(JsonObject data) {
        if (wordInputs.isEmpty()) return;

        JsonObject playerRevealed = data.has("playerRevealed") ? data.getAsJsonObject("playerRevealed") : null;
        JsonArray myRevealedArray = null;
        if (playerRevealed != null && selfUserId > 0) {
            String key = String.valueOf(selfUserId);
            if (playerRevealed.has(key)) {
                try { myRevealedArray = playerRevealed.getAsJsonArray(key); } catch (Exception ignored) {}
            }
        }

        for (int wi = 0; wi < wordInputs.size(); wi++) {
            List<JTextField> inputs = wordInputs.get(wi);
            String revealed = null;
            if (myRevealedArray != null && wi < myRevealedArray.size()) {
                try { revealed = myRevealedArray.get(wi).getAsString(); } catch (Exception ignored) { revealed = null; }
            }

            boolean localComplete = true;
            for (JTextField tf : inputs) {
                if (tf.isEditable()) {
                    if (tf.getText() == null || tf.getText().trim().isEmpty()) { localComplete = false; break; }
                }
            }

            for (int ci = 0; ci < inputs.size(); ci++) {
                JTextField tf = inputs.get(ci);
                char rc = '_';
                if (revealed != null && revealed.length() > ci) rc = revealed.charAt(ci);

                if (rc != '_' && rc != '\u0000') {
                    tf.setText(String.valueOf(rc));
                    tf.setEditable(false);
                    tf.setBackground(Theme.COLOR_BACKGROUND); 
                    tf.setForeground(Theme.COLOR_TEXT_DARK);
                } else {
                    if (localComplete) {
                        String cur = tf.getText();
                        if (cur != null && !cur.trim().isEmpty()) {
                            tf.setText("");
                            tf.setBackground(Color.WHITE);
                            tf.setEditable(true);
                            tf.setForeground(Theme.COLOR_TEXT_DARK);
                        } else {
                            tf.setEditable(true);
                            tf.setBackground(Color.WHITE);
                            tf.setForeground(Theme.COLOR_TEXT_DARK);
                        }
                    } else {
                        if (tf.isEditable()) {
                            tf.setBackground(Color.WHITE);
                            tf.setForeground(Theme.COLOR_TEXT_DARK);
                        }
                    }
                }
            }
        }
    }

    private void updateScores(JsonObject data) {
        JsonObject players = data.has("players") ? data.get("players").getAsJsonObject() : null;
        if (players == null) return;
        
        for (Map.Entry<String, JsonElement> e : players.entrySet()) {
            JsonObject ps = e.getValue().getAsJsonObject();
            int score = ps.has("score") ? ps.get("score").getAsInt() : 0;
            try {
                int uid = Integer.parseInt(e.getKey());
                if (uid == selfUserId) {
                    lblScore1.setText("" + score);
                    lblScore1.setFont(Theme.FONT_INPUT.deriveFont(Font.BOLD, 16f)); 
                    lblScore1.setForeground(Theme.COLOR_PRIMARY);
                } else {
                    lblScore2.setText("" + score);
                    lblScore2.setFont(Theme.FONT_INPUT.deriveFont(Font.BOLD, 16f)); 
                    lblScore2.setForeground(Theme.COLOR_PRIMARY);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void updateTimer(JsonObject data) {
        long now = System.currentTimeMillis();
        long end = data.has("endTime") ? data.get("endTime").getAsLong() : now;
        matchEndTime = end;
        long remain = Math.max(0, matchEndTime - now);

        updateTimerFromMillis(remain);
        startCountdown();
    }

    private void updateTimerFromMillis(long remain) {
        long sec = remain / 1000;
        long min = sec / 60;
        sec = sec % 60;

        String timeText = String.format("%02d:%02d", min, sec);
        jLabel7.setText(timeText);
        jLabel7.setFont(Theme.FONT_LABEL); // Dùng 22pt

        if (remain <= 0) {
            jLabel7.setForeground(Color.RED);
        } else if (remain < 30000) { 
            jLabel7.setForeground(Color.RED);
        } else if (remain < 60000) { 
            jLabel7.setForeground(Color.ORANGE);
        } else {
            jLabel7.setForeground(Theme.COLOR_TEXT_DARK);
        }
    }

    private void startCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) return;

        countdownTimer = new javax.swing.Timer(500, (java.awt.event.ActionEvent evt) -> {
            long now = System.currentTimeMillis();
            long remain = Math.max(0, matchEndTime - now);
            updateTimerFromMillis(remain);
            if (remain <= 0) {
                stopCountdown();
            }
        });
        countdownTimer.setCoalesce(true);
        countdownTimer.start();
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void focusNext(List<JTextField> inputs, int currentIdx) {
        int next = Math.min(currentIdx + 1, inputs.size() - 1);
        if (next >= 0 && next < inputs.size()) {
            inputs.get(next).requestFocusInWindow();
            inputs.get(next).selectAll();
        }
    }

    private void focusPrevious(List<JTextField> inputs, int currentIdx) {
        int prev = Math.max(currentIdx - 1, 0);
        if (prev >= 0 && prev < inputs.size()) {
            inputs.get(prev).requestFocusInWindow();
            inputs.get(prev).selectAll();
        }
    }

    private void sendMatchInput(int wordIdx, int charIdx, char ch) {
        if (socketHandler != null) {
            JsonObject payload = new JsonObject();
            payload.addProperty("roomId", roomId);
            payload.addProperty("wordIdx", wordIdx);
            payload.addProperty("charIdx", charIdx);
            payload.addProperty("ch", String.valueOf(ch));
            socketHandler.sendMessage("MATCH_INPUT", payload);
        } else {
            logger.info("Match input: wordIdx=" + wordIdx + ", charIdx=" + charIdx + ", ch=" + ch);
        }
    }

    public void setSelfUserId(int userId) {
        this.selfUserId = userId;
    }

    public void setSocketHandler(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    public void setPlayerNames(String name1, String name2) {
        lblName1.setText(name1);
        lblName2.setText(name2);
    }

    public void receiveChatMessage(String senderName, String message) {
        txtChatArea.append(senderName + ": " + message + "\n");
        txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
    }

    public void showGameMessage(String message) {
        txtChatArea.append("Hệ thống: " + message + "\n");
        txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
    }

    public void clearBoard() {
        boardPanel.removeAll();
        wordInputs.clear();
        boardPanel.revalidate();
        boardPanel.repaint();
        stopCountdown();
    }

    @Override
    public void dispose() {
        stopCountdown();
        super.dispose();
    }

    public void highlightCorrectWord(int wordIdx) {
        if (wordIdx >= 0 && wordIdx < wordInputs.size()) {
            List<JTextField> inputs = wordInputs.get(wordIdx);
            for (JTextField tf : inputs) {
                tf.setBackground(Theme.COLOR_PRIMARY); 
                tf.setForeground(Theme.COLOR_WHITE);
            }
        }
    }

    public void highlightIncorrectWord(int wordIdx) {
        if (wordIdx >= 0 && wordIdx < wordInputs.size()) {
            List<JTextField> inputs = wordInputs.get(wordIdx);
            for (JTextField tf : inputs) {
                tf.setBackground(Color.RED.darker());
                tf.setForeground(Color.WHITE);
            }
        }
    }

}