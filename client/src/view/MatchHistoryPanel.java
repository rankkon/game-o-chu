package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.Match;

public class MatchHistoryPanel extends JPanel {
    private final JTable historyTable;
    private final DefaultTableModel tableModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final String[] columnNames = {
        "Thời gian", "Chủ đề", "Đối thủ", "Điểm của bạn", "Điểm đối thủ", "Kết quả", "Chat Log"
    };

    public MatchHistoryPanel(ActionListener backButtonListener) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tạo panel tiêu đề với padding
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Title label với font lớn
        JLabel titleLabel = new JLabel("Lịch Sử Trận Đấu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Nút quay lại
        JButton backButton = new JButton("Quay lại");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(backButtonListener);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(backButton);
        headerPanel.add(buttonPanel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Tạo table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Tạo bảng
        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 14));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        historyTable.setRowHeight(30);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoCreateRowSorter(true);

        // Thiết lập độ rộng các cột
        int[] columnWidths = {150, 100, 100, 100, 100, 80, 200};
        for (int i = 0; i < columnWidths.length; i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Canh giữa nội dung các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Thêm bảng vào scroll pane
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Panel thông tin phụ
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.add(new JLabel("* Hiển thị 20 trận đấu gần nhất"));
        add(infoPanel, BorderLayout.SOUTH);
    }

    public void updateMatchHistory(List<Match> matches) {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        // Thêm dữ liệu mới
        for (Match match : matches) {
            tableModel.addRow(new Object[]{
                dateFormat.format(match.getMatchDate()),
                match.getCategory(),
                match.getOpponentName(),
                String.format("%,d", match.getPlayer1Score()),
                String.format("%,d", match.getPlayer2Score()),
                formatResult(match.getResult()),
                formatChatLog(match.getChatLog())
            });
        }
        
        // Notify the table that data has changed
        tableModel.fireTableDataChanged();
    }

    private String formatResult(String result) {
        switch (result) {
            case "WIN": return "Thắng";
            case "LOSE": return "Thua";
            case "DRAW": return "Hòa";
            default: return result;
        }
    }

    private String formatChatLog(String chatLog) {
        if (chatLog == null || chatLog.trim().isEmpty()) {
            return "Không có";
        }
        // Giới hạn độ dài chat log hiển thị
        if (chatLog.length() > 50) {
            return chatLog.substring(0, 47) + "...";
        }
        return chatLog;
    }

    // Phương thức để lấy thông tin trận đấu được chọn (nếu cần)
    public Match getSelectedMatch() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedRow = historyTable.convertRowIndexToModel(selectedRow);
            // Trả về đối tượng Match tương ứng với dòng được chọn
            return null; // TODO: Implement this if needed
        }
        return null;
    }
}