package model;

import java.util.Date;

public class Match {
    private int id;
    private String category;          // Chủ đề của trận đấu
    private String player1Name;       // Tên người chơi 1
    private String player2Name;       // Tên người chơi 2
    private int player1Score;         // Điểm người chơi 1
    private int player2Score;         // Điểm người chơi 2
    private String opponentName;      // Tên đối thủ (sẽ là player1Name hoặc player2Name tùy vào góc nhìn)
    private Date matchDate;           // Thời gian bắt đầu trận
    private String result;            // Kết quả: "WIN", "LOSE", "DRAW"
    private String chatLog;           // Lịch sử chat của trận đấu

    public Match(int id, String category, String player1Name, String player2Name, 
                int player1Score, int player2Score, Date matchDate, 
                String result, String chatLog, String opponentName) {
        this.id = id;
        this.category = category;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.matchDate = matchDate;
        this.result = result;
        this.chatLog = chatLog;
        this.opponentName = opponentName;
    }

    // Getters
    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public String getOpponentName() { return opponentName; }
    public Date getMatchDate() { return matchDate; }
    public String getResult() { return result; }
    public String getChatLog() { return chatLog; }

    @Override
    public String toString() {
        return String.format("Match[id=%d, category=%s, opponent=%s, result=%s]",
            id, category, opponentName, result);
    }
}