package model;

public class PlayerState {
    private int score = 0;
    private int correctWords = 0;
    private long lastScoreAt = 0;
    private long completionTime = 0; // Thời gian hoàn thành hết tất cả từ (0 nếu chưa hoàn thành)
    private User user; // Thông tin người chơi
    // Per-player copy of word instances (revealed/fill state). Mark transient so
    // it won't be serialized accidentally into DTOs. MatchRoom.toDto will
    // produce explicit per-player revealed patterns instead.
    private transient java.util.List<WordInstance> personalWords = new java.util.ArrayList<>();

    public PlayerState() {}
    
    public PlayerState(User user) {
        this.user = user;
    }

    public void addScore(int points) {
        score += points;
        lastScoreAt = System.currentTimeMillis();
    }

    public void addCorrectWord() {
            correctWords += 1;
            lastScoreAt = System.currentTimeMillis();
    }

    public int getScore() { return score; }
    public int getCorrectWords() { return correctWords; }
    public long getLastScoreAt() { return lastScoreAt; }
    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public java.util.List<WordInstance> getPersonalWords() { return personalWords; }
    public void setPersonalWords(java.util.List<WordInstance> personalWords) { this.personalWords = personalWords; }
}


