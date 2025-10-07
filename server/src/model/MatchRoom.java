package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.PlayerState;
import service.WordInstance;

public class MatchRoom {
    private String roomId;
    private int creatorId;
    private int opponentId;
    private long startTime;
    private long endTime;
    private String status; // WAITING, PLAYING, FINISHED
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

    public Map<String, Object> toDto() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("roomId", roomId);
        dto.put("creatorId", creatorId);
        dto.put("opponentId", opponentId);
        dto.put("startTime", startTime);
        dto.put("endTime", endTime);
        dto.put("status", status);
        dto.put("words", words); // WordInstance should be JSON serializable
        dto.put("players", players);
        return dto;
    }
}
