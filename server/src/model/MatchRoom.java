package model;

public class MatchRoom {
    private String roomId;
    private String creatorId;

    public MatchRoom(String roomId, String creatorId) {
        this.roomId = roomId;
        this.creatorId = creatorId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getCreatorId() {
        return creatorId;
    }
}
