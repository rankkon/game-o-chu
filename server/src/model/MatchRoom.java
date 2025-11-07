package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchRoom {
    private String roomId;
    private Integer matchId; // DB-generated id for the persisted match record
    private int creatorId;
    private int opponentId;
    private long startTime;
    private long endTime;
    private String status; // WAITING, PLAYING, FINISHED
    private String categoryCode;
    private String categoryName;
    private List<WordInstance> words;
    private Map<Integer, PlayerState> players = new HashMap<>();

    public MatchRoom(String roomId, int creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
        this.status = "WAITING";
    }

    public String getRoomId() { return roomId; }
    public Integer getMatchId() { return matchId; }
    public void setMatchId(Integer matchId) { this.matchId = matchId; }
    public int getCreatorId() { return creatorId; }
    public int getOpponentId() { return opponentId; }
    public void setOpponentId(int opponentId) { this.opponentId = opponentId; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<WordInstance> getWords() { return words; }
    public void setWords(List<WordInstance> words) { this.words = words; }
    public Map<Integer, PlayerState> getPlayers() { return players; }
    public void setPlayers(Map<Integer, PlayerState> players) { this.players = players; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public boolean isPlayer(int userId) {return this.creatorId == userId || this.opponentId == userId;}

    public Map<String, Object> toDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("roomId", roomId);
        dto.put("creatorId", creatorId);
        dto.put("opponentId", opponentId);
        dto.put("startTime", startTime);
        dto.put("endTime", endTime);
        dto.put("status", status);
        dto.put("categoryCode", categoryCode);
        dto.put("categoryName", categoryName);
        // Only expose hint and length to clients; do not expose filled/answer to prevent opponent peeking
        java.util.List<java.util.Map<String, Object>> wordDtos = new java.util.ArrayList<>();
        if (words != null) {
            for (WordInstance w : words) {
                java.util.Map<String, Object> wd = new java.util.HashMap<>();
                wd.put("hint", w.getHint());
                wd.put("length", w.getFilled().length);
                // For canonical word object we include only hint/length. Per-player
                // revealed patterns are exposed separately via playerRevealed map.
                wordDtos.add(wd);
            }
        }
        dto.put("words", wordDtos);
        // Build a concise players DTO (don't accidentally serialize personalWords)
        java.util.Map<String, java.util.Map<String, Object>> playersDto = new java.util.HashMap<>();
        for (Map.Entry<Integer, PlayerState> e : players.entrySet()) {
            Integer uid = e.getKey();
            PlayerState ps = e.getValue();
            java.util.Map<String, Object> pd = new java.util.HashMap<>();
            pd.put("score", ps.getScore());
            pd.put("correctWords", ps.getCorrectWords());
            pd.put("lastScoreAt", ps.getLastScoreAt());
            playersDto.put(String.valueOf(uid), pd);
        }
        dto.put("players", playersDto);

        // Include per-player revealed patterns so each client can render only their own filled letters
        java.util.Map<String, java.util.List<String>> playerRevealed = new java.util.HashMap<>();
        for (Map.Entry<Integer, PlayerState> e : players.entrySet()) {
            Integer uid = e.getKey();
            PlayerState ps = e.getValue();
            java.util.List<String> reveals = new java.util.ArrayList<>();
            java.util.List<WordInstance> pWords = ps.getPersonalWords();
            if (pWords != null) {
                for (WordInstance pw : pWords) {
                    char[] f = pw.getFilled();
                    StringBuilder sb = new StringBuilder();
                    for (char c : f) sb.append(c == 0 || c == '_' ? '_' : c);
                    reveals.add(sb.toString());
                }
            } else {
                // Fallback: expose underscores for each canonical word length
                if (words != null) {
                    for (WordInstance w : words) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < w.getAnswer().length(); i++) sb.append('_');
                        reveals.add(sb.toString());
                    }
                }
            }
            playerRevealed.put(String.valueOf(uid), reveals);
        }
        dto.put("playerRevealed", playerRevealed);
        return dto;
    }
}
