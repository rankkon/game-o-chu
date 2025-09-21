package controller;

import model.MatchRoom;
import service.MatchService;

public class MatchController {
    private MatchService matchService;

    public MatchController() {
        this.matchService = new MatchService();
    }

    /**
     * API tạo phòng
     * @param creatorId id người tạo
     * @return thông tin phòng vừa tạo
     */
    public MatchRoom createRoom(String creatorId) {
        return matchService.createRoom(creatorId);
    }

    /**
     * API lấy thông tin phòng
     * @param roomId id phòng
     * @return thông tin phòng hoặc null nếu không có
     */
    public MatchRoom getRoom(String roomId) {
        return matchService.getRoom(roomId);
    }

    /**
     * API kiểm tra phòng tồn tại
     */
    public boolean roomExists(String roomId) {
        return matchService.roomExists(roomId);
    }

    /**
     * API xóa phòng
     */
    public void removeRoom(String roomId) {
        matchService.removeRoom(roomId);
    }
}