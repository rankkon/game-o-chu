package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.PlayerState;
import model.WordInstance;

public class MatchRoom {
    private String roomId;
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
                // include revealed letters pattern, e.g. "A__B_"
                char[] filled = w.getFilled();
                StringBuilder sb = new StringBuilder();
                for (char c : filled) sb.append(c == 0 || c == '_' ? '_' : c);
                wd.put("revealed", sb.toString());
                wordDtos.add(wd);
            }
        }
        dto.put("words", wordDtos);
        dto.put("players", players);
        return dto;
    }
}
