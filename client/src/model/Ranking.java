package model;

public class Ranking {
    private final int rank;
    private final String username;
    private final int totalMatches;
    private final int wonMatches;
    private final int totalScore;
    private final double winRate;
    private final double avgTimeRemaining;

    public Ranking(int rank, String username, int totalMatches, int wonMatches, int totalScore, double avgTimeRemaining) {
        this.rank = rank;
        this.username = username;
        this.totalMatches = totalMatches;
        this.wonMatches = wonMatches;
        this.totalScore = totalScore;
        this.winRate = totalMatches > 0 ? (wonMatches * 100.0 / totalMatches) : 0;
        this.avgTimeRemaining = avgTimeRemaining;
    }

    // Getters
    public int getRank() { return rank; }
    public String getUsername() { return username; }
    public int getTotalMatches() { return totalMatches; }
    public int getWonMatches() { return wonMatches; }
    public int getTotalScore() { return totalScore; }
    public double getWinRate() { return winRate; }
    public double getAvgTimeRemaining() { return avgTimeRemaining; }
}