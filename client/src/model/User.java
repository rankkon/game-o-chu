package model;

public class User {
    private int id;
    private String username = "#"; // giá trị mặc định
    private String name = "#";
    private String avatar = "default_avatar.png";
    private String gender = "Nam";
    private int yearOfBirth = 2004;

    private double score = 1000;
    private int matchCount = 0;
    private int winCount = 0;
    private int drawCount = 0;
    private int loseCount = 0;
    private int currentStreak = 0;
    private int rank = -1;
    private boolean blocked = false;

    public User() {
    }

    public User(int id, String username, String name, String avatar, String gender, 
                int yearOfBirth, double score, int matchCount, int winCount, 
                int drawCount, int loseCount, int currentStreak, int rank, boolean blocked) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.avatar = avatar;
        this.gender = gender;
        this.yearOfBirth = yearOfBirth;
        this.score = score;
        this.matchCount = matchCount;
        this.winCount = winCount;
        this.drawCount = drawCount;
        this.loseCount = loseCount;
        this.currentStreak = currentStreak;
        this.rank = rank;
        this.blocked = blocked;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public int getYearOfBirth() { return yearOfBirth; }
    public void setYearOfBirth(int yearOfBirth) { this.yearOfBirth = yearOfBirth; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    
    public int getMatchCount() { return matchCount; }
    public void setMatchCount(int matchCount) { this.matchCount = matchCount; }
    
    public int getWinCount() { return winCount; }
    public void setWinCount(int winCount) { this.winCount = winCount; }
    
    public int getDrawCount() { return drawCount; }
    public void setDrawCount(int drawCount) { this.drawCount = drawCount; }
    
    public int getLoseCount() { return loseCount; }
    public void setLoseCount(int loseCount) { this.loseCount = loseCount; }
    
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    
    // Tính toán thêm
    public float getWinRate() {
        if (matchCount == 0) return 0;
        return (float) winCount / matchCount * 100;
    }
    
    @Override
    public String toString() {
        return name + " (" + username + ")";
    }
}