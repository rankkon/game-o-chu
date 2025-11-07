package view;

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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import controller.SocketHandler;

/**
 * MatchFrame hiển thị ván đấu: bảng ô chữ, timer và điểm số.
 * Nhận dữ liệu từ server qua LobbyController và gửi MATCH_INPUT khi người chơi gõ chữ.
 */
public class MatchFrame extends JFrame {
    private final SocketHandler socketHandler;
    private final int selfUserId;

    private String roomId = "";
    private JLabel timerLabel;
    private JLabel scoreLabelSelf;
    private JLabel scoreLabelOpponent;
    private JPanel wordsPanel;

    private final List<List<JTextField>> wordInputs = new ArrayList<>();

    public MatchFrame(SocketHandler socketHandler, int selfUserId) {
        this.socketHandler = socketHandler;
        this.selfUserId = selfUserId;

        setTitle("Ván đấu - Game Ô Chữ");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top: timer + scores
        JPanel top = new JPanel(new GridLayout(1, 3, 10, 0));
        timerLabel = makeInfoLabel("Thời gian: 90s");
        scoreLabelSelf = makeInfoLabel("Điểm của bạn: 0");
        scoreLabelOpponent = makeInfoLabel("Điểm đối thủ: 0");
        top.add(timerLabel);
        top.add(scoreLabelSelf);
        top.add(scoreLabelOpponent);
        main.add(top, BorderLayout.NORTH);

        // Center: words grid
        wordsPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        main.add(wordsPanel, BorderLayout.CENTER);

        // Bottom: end match button
        JButton endBtn = new JButton("Về sảnh");
        endBtn.addActionListener(e -> {
            sendLeaveMatch();

            this.dispose(); 
        });
        main.add(endBtn, BorderLayout.SOUTH);
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        return label;
    }

    public void loadMatchStart(JsonObject envelope) {
        // envelope: { action:"match_start", roomId, data: { words, players, startTime, endTime, ... } }
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
        wordsPanel.removeAll();
        wordInputs.clear();

        JsonArray words = data.getAsJsonArray("words");
        if (words == null) return;

        for (JsonElement we : words) {
            JsonObject w = we.getAsJsonObject();
            String hint = w.has("hint") ? w.get("hint").getAsString() : "";
            int len = w.has("length") ? w.get("length").getAsInt() : 0;

            JPanel row = new JPanel(new BorderLayout(10, 10));
            JLabel hintLabel = new JLabel(hint);
            hintLabel.setPreferredSize(new Dimension(220, 30));
            row.add(hintLabel, BorderLayout.WEST);

            JPanel cells = new JPanel(new GridLayout(1, Math.max(1, len), 5, 5));
            List<JTextField> inputs = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                JTextField tf = new JTextField(1);
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(new Font("Consolas", Font.BOLD, 20));
                tf.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                // If server provided revealed pattern, prefill revealed letters and disable editing for them
                String revealed = w.has("revealed") ? w.get("revealed").getAsString() : null;
                if (revealed != null && revealed.length() > i) {
                    char rc = revealed.charAt(i);
                    if (rc != '_' && rc != '\u0000') {
                        tf.setText(String.valueOf(rc));
                        tf.setEditable(false);
                        tf.setBackground(new Color(220, 220, 220));
                        tf.setForeground(Color.BLACK);
                    }
                }
                final int wordIdx = wordInputs.size();
                final int charIdx = i;
                tf.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char ch = e.getKeyChar();
                        if (Character.isLetter(ch)) {
                            tf.setText(("" + ch).toUpperCase());
                            sendMatchInput(wordIdx, charIdx, Character.toUpperCase(ch));
                            // Move focus to next cell
                            SwingUtilities.invokeLater(() -> focusNext(inputs, charIdx));
                            // Prevent default insertion of the typed char (avoids duplicate lowercase)
                            e.consume();
                        } else {
                            e.consume();
                        }
                    }
                });

                // keep existing text (revealed letter or underscore) so hint is visible

                inputs.add(tf);
                cells.add(tf);
            }
            row.add(cells, BorderLayout.CENTER);
            wordsPanel.add(row);
            wordInputs.add(inputs);
        }

        wordsPanel.revalidate();
        wordsPanel.repaint();
    }

    private void refreshFilled(JsonObject data) {
        // Không hiển thị chữ đã điền từ server để tránh đối thủ nhìn thấy; chỉ cập nhật điểm/timer.
    }

    private void updateScores(JsonObject data) {
        JsonObject players = data.has("players") ? data.get("players").getAsJsonObject() : null;
        if (players == null) return;
        // players is a map keyed by userId (numbers become strings in JSON)
        for (Map.Entry<String, JsonElement> e : players.entrySet()) {
            JsonObject ps = e.getValue().getAsJsonObject();
            int score = ps.has("score") ? ps.get("score").getAsInt() : 0;
            String status = ps.has("connectionStatus") ? ps.get("connectionStatus").getAsString() : "CONNECTED";
            try {
                int uid = Integer.parseInt(e.getKey());
                if (uid == selfUserId) {
                    scoreLabelSelf.setText("Điểm của bạn: " + score);
                } else {
                    String statusText = status.equals("DISCONNECTED") ? " (Đã thoát)" : "";
                    scoreLabelOpponent.setText("Điểm đối thủ: " + score + statusText);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void updateTimer(JsonObject data) {
        long now = System.currentTimeMillis();
        long end = data.has("endTime") ? data.get("endTime").getAsLong() : now;
        long remain = Math.max(0, end - now);
        long sec = remain / 1000;
        timerLabel.setText("Thời gian: " + sec + "s");
    }

    private void focusNext(List<JTextField> inputs, int currentIdx) {
        int next = Math.min(currentIdx + 1, inputs.size() - 1);
        if (next >= 0 && next < inputs.size()) {
            inputs.get(next).requestFocusInWindow();
            inputs.get(next).selectAll();
        }
    }

    private void sendMatchInput(int wordIdx, int charIdx, char ch) {
        JsonObject payload = new JsonObject();
        payload.addProperty("roomId", roomId);
        payload.addProperty("wordIdx", wordIdx);
        payload.addProperty("charIdx", charIdx);
        payload.addProperty("ch", String.valueOf(ch));
        socketHandler.sendMessage("MATCH_INPUT", payload);
    }

    private void sendLeaveMatch() {
        JsonObject payload = new JsonObject();
        payload.addProperty("roomId", roomId);
        socketHandler.sendMessage("LEAVE_MATCH", payload); 
    }
}


