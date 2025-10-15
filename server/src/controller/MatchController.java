package controller;

import model.MatchRoom;
import service.MatchService;
import service.UserService;
import service.DictionaryService;

/**
 * MatchController
 * ------------------
 * Điều phối luồng xử lý của các trận đấu:
 * - Tạo phòng
 * - Bắt đầu trận (ghép đối thủ)
 * - Xử lý nhập ký tự (điền ô chữ)
 * - Kết thúc trận
 */
public class MatchController {
    private final MatchService matchService;

    /**
     * Constructor inject các service cần thiết
     */
    public MatchController(DictionaryService dictionaryService, UserService userService) {
        this.matchService = new MatchService(dictionaryService, userService);
    }

    /**
     * Tạo một phòng mới (người tạo ở trạng thái chờ)
     */
    public MatchRoom createRoom(int creatorId) {
        return matchService.createRoom(creatorId);
    }

    /**
     * Ghép người chơi thứ hai và bắt đầu trận đấu
     */
    public void startMatch(String roomId, int opponentId) {
        matchService.startMatch(roomId, opponentId);
    }

    /**
     * Khi người chơi nhập chữ tại một ô
     */
    public void handleLetterInput(String roomId, int playerId, int wordIdx, int charIdx, char ch) {
        matchService.handleLetterInput(roomId, playerId, wordIdx, charIdx, ch);
    }

    /**
     * Kết thúc trận đấu (do hết giờ hoặc người chơi rời)
     */
    public void endMatch(String roomId) {
        matchService.endMatch(roomId);
    }

    /**
     * Lấy thông tin phòng theo id
     */
    public MatchRoom getRoom(String roomId) {
        return matchService.getRoom(roomId);
    }

    /**
     * Kiểm tra phòng có tồn tại hay không
     */
    public boolean roomExists(String roomId) {
        return matchService.roomExists(roomId);
    }

    /**
     * Xóa phòng (sau khi kết thúc)
     */
    public void removeRoom(String roomId) {
        matchService.removeRoom(roomId);
    }
}
