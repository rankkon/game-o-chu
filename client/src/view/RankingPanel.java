package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
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

import model.Ranking;

public class RankingPanel extends JPanel {
    private final JTable rankingTable;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final String[] columnNames = {
        "Hạng", "Tên người chơi", "Tổng điểm", "Số trận thắng", "Tổng số trận", "Tỷ lệ thắng", "Thời gian TB còn lại"
    };

    public RankingPanel(ActionListener backButtonListener) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tạo panel tiêu đề với padding
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Title label với font lớn hơn
        JLabel titleLabel = new JLabel("Bảng Xếp Hạng", SwingConstants.CENTER);
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
        rankingTable = new JTable(tableModel);
        rankingTable.setFont(new Font("Arial", Font.PLAIN, 14));
        rankingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        rankingTable.setRowHeight(30);
        rankingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankingTable.setAutoCreateRowSorter(true);

        // Thiết lập độ rộng các cột
        int[] columnWidths = {60, 200, 100, 100, 100, 100, 150};
        for (int i = 0; i < columnWidths.length; i++) {
            rankingTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Canh giữa nội dung các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < rankingTable.getColumnCount(); i++) {
            rankingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Thêm bảng vào scroll pane
        JScrollPane scrollPane = new JScrollPane(rankingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Panel thông tin phụ
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.add(new JLabel("* Bảng xếp hạng được cập nhật theo thời gian thực"));
        infoPanel.add(new JLabel("* Tiêu chí: Tổng điểm → Số trận thắng → Thời gian còn lại trung bình"));
        add(infoPanel, BorderLayout.SOUTH);
    }

    public void updateRankings(List<Ranking> rankings) {
        // Xóa dữ liệu cũ
        tableModel.setRowCount(0);

        // Thêm dữ liệu mới
        for (Ranking rank : rankings) {
            double winRate = rank.getTotalMatches() > 0 
                ? (double) rank.getWonMatches() * 100 / rank.getTotalMatches() 
                : 0.0;
                
            tableModel.addRow(new Object[]{
                "#" + rank.getRank(),
                rank.getUsername(),
                String.format("%,d", rank.getTotalScore()),
                String.format("%,d", rank.getWonMatches()),
                String.format("%,d", rank.getTotalMatches()),
                df.format(winRate) + "%",
                df.format(rank.getAvgTimeRemaining()) + "s"
            });
        }
        
        // Notify the table that data has changed
        tableModel.fireTableDataChanged();
    }

    // Thêm hiệu ứng highlight cho người chơi hiện tại
    public void highlightCurrentPlayer(String username) {
        for (int i = 0; i < rankingTable.getRowCount(); i++) {
            if (rankingTable.getValueAt(i, 1).equals(username)) {
                rankingTable.setRowSelectionInterval(i, i);
                rankingTable.scrollRectToVisible(rankingTable.getCellRect(i, 0, true));
                break;
            }
        }
    }
}