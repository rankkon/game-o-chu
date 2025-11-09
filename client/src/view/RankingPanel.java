package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension; 
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import model.Ranking;

public class RankingPanel extends JPanel {
    private final JTable rankingTable;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final String[] columnNames = {
        "Hạng", "Tên người chơi", "Tổng điểm", "Số trận thắng", "Tổng số trận", "Tỷ lệ thắng"
    };

    // --- Biến cho panel thông tin user ---
    private JPanel currentUserInfoPanel; 
    private JLabel lblInfoName;
    private JLabel lblInfoRank;
    private JLabel lblInfoScore;
    private JLabel lblInfoMatches;
    private JLabel lblInfoWins;

    public RankingPanel(ActionListener backButtonListener) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Theme.COLOR_BACKGROUND);

        // --- 1. HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.setBackground(Theme.COLOR_BACKGROUND);
        
        JLabel titleLabel = new JLabel("Bảng Xếp Hạng", SwingConstants.CENTER);
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
        Dimension buttonPanelSize = buttonPanel.getPreferredSize();
        dummyPanel.setPreferredSize(buttonPanelSize);
        headerPanel.add(dummyPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. BẢNG  ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rankingTable = new JTable(tableModel);
        rankingTable.setFont(Theme.FONT_BUTTON_SMALL);
        rankingTable.getTableHeader().setFont(Theme.FONT_BUTTON_SMALL);
        rankingTable.setRowHeight(40); 
        rankingTable.setBackground(Theme.COLOR_WHITE);
        rankingTable.setForeground(Theme.COLOR_TEXT_DARK);
        rankingTable.setGridColor(Theme.COLOR_BORDER);
        rankingTable.setSelectionBackground(Theme.COLOR_ACCENT);
        rankingTable.setSelectionForeground(Theme.COLOR_TEXT_DARK);
        rankingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankingTable.setAutoCreateRowSorter(true);
        int[] columnWidths = {60, 200, 100, 100, 100, 100};
        for (int i = 0; i < columnWidths.length; i++) {
            rankingTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < rankingTable.getColumnCount(); i++) {
            rankingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        rankingTable.getTableHeader().setDefaultRenderer(new ThemedHeaderRenderer());
        rankingTable.getTableHeader().setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(rankingTable);
        scrollPane.setBorder(new Theme.RoundedBorder(Theme.COLOR_BORDER, 1, Theme.CORNER_RADIUS));
        scrollPane.getViewport().setBackground(Theme.COLOR_WHITE); 
        
        add(scrollPane, BorderLayout.CENTER); 

        JPanel bottomPanel = new JPanel(new BorderLayout(20, 0)); 
        bottomPanel.setBackground(Theme.COLOR_BACKGROUND);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); 

        // Tạo panel thông tin user 
        currentUserInfoPanel = createUserInfoPanel(); 
        currentUserInfoPanel.setVisible(false);
        bottomPanel.add(currentUserInfoPanel, BorderLayout.WEST);

        //Tạo panel ghi chú 
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        infoPanel.setBackground(Theme.COLOR_BACKGROUND);
        infoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        bottomPanel.add(infoPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Tạo panel thông tin cho người dùng hiện tại
     */
    private JPanel createUserInfoPanel() {
        Theme.RoundedPanel panel = new Theme.RoundedPanel(new BorderLayout());
        panel.setBackground(Theme.COLOR_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("Thông tin của bạn");
        title.setFont(Theme.FONT_LABEL); 
        title.setForeground(Theme.COLOR_PRIMARY);
        title.setBorder(new EmptyBorder(0, 5, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.setBackground(Theme.COLOR_WHITE);

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setBackground(Theme.COLOR_WHITE);

        lblInfoName = new JLabel("-");
        lblInfoRank = new JLabel("-");
        lblInfoScore = new JLabel("-");

        Font labelFont = Theme.FONT_INPUT.deriveFont(Font.BOLD, 16f); 
        Font valueFont = Theme.FONT_INPUT.deriveFont(16f); 

        leftColumn.add(createDetailRow("Tên:", lblInfoName, labelFont, valueFont));
        leftColumn.add(Box.createVerticalStrut(5)); 
        leftColumn.add(createDetailRow("Hạng:", lblInfoRank, labelFont, valueFont));
        leftColumn.add(Box.createVerticalStrut(5));
        leftColumn.add(createDetailRow("Điểm:", lblInfoScore, labelFont, valueFont));
        leftColumn.add(Box.createVerticalGlue()); 

        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setBackground(Theme.COLOR_WHITE);

        lblInfoMatches = new JLabel("-");
        lblInfoWins = new JLabel("-");

        rightColumn.add(createDetailRow("Tổng trận:", lblInfoMatches, labelFont, valueFont));
        rightColumn.add(Box.createVerticalStrut(5));
        rightColumn.add(createDetailRow("Thắng:", lblInfoWins, labelFont, valueFont));
        rightColumn.add(Box.createVerticalGlue()); 

        content.add(leftColumn);
        content.add(rightColumn);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDetailRow(String labelText, JLabel valueLabel, Font labelFont, Font valueFont) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Theme.COLOR_WHITE);
        rowPanel.setBorder(new EmptyBorder(0, 5, 5, 5)); 

        JLabel lblField = new JLabel(labelText);
        lblField.setFont(labelFont);
        lblField.setForeground(Theme.COLOR_TEXT_DARK);
        
        lblField.setPreferredSize(new Dimension(100, 25)); 

        valueLabel.setFont(valueFont);
        valueLabel.setForeground(Theme.COLOR_PRIMARY);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT); 

        rowPanel.add(lblField, BorderLayout.WEST);
        rowPanel.add(valueLabel, BorderLayout.CENTER);
        return rowPanel;
    }


    public void updateRankings(List<Ranking> rankings) {

        tableModel.setRowCount(0);

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
                df.format(winRate) + "%"
            });
        }
        
        tableModel.fireTableDataChanged();
    }

    public void highlightCurrentPlayer(String username) {
        boolean userFound = false;
        for (int i = 0; i < rankingTable.getRowCount(); i++) {
            int modelIndex = rankingTable.convertRowIndexToModel(i);
            String usernameInTable = (String) tableModel.getValueAt(modelIndex, 1);
            
            if (usernameInTable.equals(username)) {
                rankingTable.setRowSelectionInterval(i, i);
                rankingTable.scrollRectToVisible(rankingTable.getCellRect(i, 0, true));

                lblInfoName.setText((String) tableModel.getValueAt(modelIndex, 1)); 
                lblInfoRank.setText((String) tableModel.getValueAt(modelIndex, 0));
                lblInfoScore.setText((String) tableModel.getValueAt(modelIndex, 2));
                lblInfoWins.setText((String) tableModel.getValueAt(modelIndex, 3)); 
                lblInfoMatches.setText((String) tableModel.getValueAt(modelIndex, 4));
                
                userFound = true;
                break; 
            }
        }
        
        currentUserInfoPanel.setVisible(userFound);
    }
    
    private static class ThemedHeaderRenderer extends DefaultTableCellRenderer {
        public ThemedHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(Theme.COLOR_PRIMARY);
            setForeground(Theme.COLOR_WHITE);
            setFont(Theme.FONT_BUTTON_SMALL); 
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
            
            return this;
        }
    }
}