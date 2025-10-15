package model;

public class PlayerState {
    private int score = 0;
    private int correctWords = 0;
    private long lastScoreAt = 0;

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
}


