package view;

import java.awt.BorderLayout;
import java.awt.Component; 
import java.awt.FlowLayout;
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
        "Thời gian", "Chủ đề", "Đối thủ", "Điểm của bạn", "Điểm đối thủ", "Kết quả"
    };

    public MatchHistoryPanel(ActionListener backButtonListener) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Theme.COLOR_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        headerPanel.setBackground(Theme.COLOR_BACKGROUND);

        JLabel titleLabel = new JLabel("Lịch Sử Trận Đấu", SwingConstants.CENTER);

        titleLabel.setFont(Theme.FONT_SUBTITLE); 
        titleLabel.setForeground(Theme.COLOR_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backButton = new JButton("Quay lại");

        Theme.styleButtonSecondary(backButton); 
        backButton.addActionListener(backButtonListener);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
        buttonPanel.setBackground(Theme.COLOR_BACKGROUND); 
        buttonPanel.add(backButton);
        headerPanel.add(buttonPanel, BorderLayout.WEST);

        JPanel dummyPanel = new JPanel();
        dummyPanel.setBackground(Theme.COLOR_BACKGROUND);
        java.awt.Dimension buttonPanelSize = buttonPanel.getPreferredSize();
        dummyPanel.setPreferredSize(buttonPanelSize);
        headerPanel.add(dummyPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);

        historyTable.setFont(Theme.FONT_BUTTON_SMALL); 
        historyTable.getTableHeader().setFont(Theme.FONT_BUTTON_SMALL);
        historyTable.setRowHeight(40); 
        historyTable.setBackground(Theme.COLOR_WHITE);
        historyTable.setForeground(Theme.COLOR_TEXT_DARK);
        historyTable.setGridColor(Theme.COLOR_BORDER);
        historyTable.setSelectionBackground(Theme.COLOR_ACCENT);
        historyTable.setSelectionForeground(Theme.COLOR_TEXT_DARK);
        
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoCreateRowSorter(true);

        int[] columnWidths = {150, 100, 100, 100, 100, 80};
        for (int i = 0; i < columnWidths.length; i++) {
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        historyTable.getTableHeader().setDefaultRenderer(new ThemedHeaderRenderer());
        historyTable.getTableHeader().setOpaque(false); 

        JScrollPane scrollPane = new JScrollPane(historyTable);

        scrollPane.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, Theme.CORNER_RADIUS));
        scrollPane.getViewport().setBackground(Theme.COLOR_WHITE); 
        
        add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        infoPanel.setBackground(Theme.COLOR_BACKGROUND);
        
        JLabel infoLabel = new JLabel("* Hiển thị 20 trận đấu gần nhất");

        infoLabel.setFont(Theme.FONT_BUTTON_SMALL); 
        infoLabel.setForeground(Theme.COLOR_TEXT_DARK.brighter()); 
        infoPanel.add(infoLabel);
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
                formatResult(match.getResult())
            });
        }
        
        // Notify the table that data has changed
        tableModel.fireTableDataChanged();
    }

    private String formatResult(String result) {
        switch (result) {
            case "WIN": return "Thắng";
            case "LOSE": return "Thua";
            default: return result;
        }
    }

    // Phương thức để lấy thông tin trận đấu được chọn
    public Match getSelectedMatch() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedRow = historyTable.convertRowIndexToModel(selectedRow);
            // Trả về đối tượng Match tương ứng với dòng được chọn
            return null; 
        }
        return null;
    }

    // --- LỚP NỘI BỘ ĐỂ STYLE HEADER CHO BẢNG ---
    private static class ThemedHeaderRenderer extends DefaultTableCellRenderer {
        public ThemedHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Lấy component mặc định
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBackground(Theme.COLOR_PRIMARY);
            setForeground(Theme.COLOR_WHITE);
            setFont(Theme.FONT_BUTTON_SMALL); 
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
            
            return this;
        }
    }
}