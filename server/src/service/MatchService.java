package service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import model.MatchRoom;

public class MatchService {

    private Map<String, MatchRoom> rooms = new HashMap<>();

    /**
     * Tạo một phòng mới
     * @param creatorId id người tạo phòng
     * @return MatchRoom vừa tạo
     */
    public MatchRoom createRoom(String creatorId) {
        String roomId = UUID.randomUUID().toString(); // tạo id ngẫu nhiên
        MatchRoom room = new MatchRoom(roomId, creatorId);
        rooms.put(roomId, room);
        return room;
    }

    /**
     * Lấy thông tin phòng
     * @param roomId id của phòng
     * @return MatchRoom hoặc null nếu không tồn tại
     */
    public MatchRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    /**
     * Kiểm tra phòng có tồn tại không
     */
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    /**
     * Xóa phòng
     */
    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }
}