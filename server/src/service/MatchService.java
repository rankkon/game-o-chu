package service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import model.MatchRoom;
import service.PlayerState;
import service.WordInstance;
import util.JsonUtil;

/**
 * MatchService xử lý toàn bộ logic tạo phòng, bắt đầu trận đấu, chấm điểm và kết thúc.
 */
public class MatchService {

    // Danh sách phòng đang hoạt động
    private Map<String, MatchRoom> rooms = new ConcurrentHashMap<>();

    private final DictionaryService dictionaryService;
    private final UserService userService;

    public MatchService(DictionaryService dictionaryService, UserService userService) {
        this.dictionaryService = dictionaryService;
        this.userService = userService;
    }

    /**
     * Tạo một phòng mới bởi người tạo
     */
    public MatchRoom createRoom(int creatorId) {
        String roomId = UUID.randomUUID().toString();
        MatchRoom room = new MatchRoom(roomId, creatorId);
        rooms.put(roomId, room);
        return room;
    }

    /**
     * Ghép thêm người chơi thứ hai và bắt đầu trận đấu
     */
    public void startMatch(String roomId, int opponentId) {
        MatchRoom room = rooms.get(roomId);
        if (room == null) return;

        room.setOpponentId(opponentId);
        room.setStartTime(System.currentTimeMillis());
        room.setEndTime(room.getStartTime() + 90_000); // 90s

        // Lấy 5 từ ngẫu nhiên
        List<WordInstance> words = dictionaryService.getRandomWords(5);
        room.setWords(words);

        // Khởi tạo điểm
        room.setPlayers(new HashMap<>());
        room.getPlayers().put(room.getCreatorId(), new PlayerState());
        room.getPlayers().put(room.getOpponentId(), new PlayerState());

        room.setStatus("PLAYING");

        // Gửi message match_start cho cả 2
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_start");
        payload.put("roomId", room.getRoomId());
        payload.put("data", room.toDto());
        String json = JsonUtil.toJson(payload);
        userService.sendToUser(room.getCreatorId(), json);
        userService.sendToUser(room.getOpponentId(), json);
    }

    /**
     * Khi người chơi nhập chữ trong 1 từ
     */
    public void handleLetterInput(String roomId, int playerId, int wordIdx, int charIdx, char ch) {
        MatchRoom room = rooms.get(roomId);
        if (room == null || !"PLAYING".equals(room.getStatus())) return;

        WordInstance word = room.getWords().get(wordIdx);
        boolean correct = word.handleInput(charIdx, ch);

        PlayerState ps = room.getPlayers().get(playerId);

        if (correct && !word.wasTried(charIdx)) {
            ps.addScore(1);
            word.setTried(charIdx, true);
        }

        if (word.isFullyCorrect() && !word.isBonusGiven()) {
            ps.addScore(2);
            ps.addCorrectWord();
            word.setBonusGiven(true);
        }

        broadcastMatchUpdate(room);
    }

    /**
     * Gửi cập nhật realtime đến cả 2 người chơi
     */
    public void broadcastMatchUpdate(MatchRoom room) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_update");
        payload.put("data", room.toDto());
        String json = JsonUtil.toJson(payload);
        userService.sendToUser(room.getCreatorId(), json);
        userService.sendToUser(room.getOpponentId(), json);
    }

    /**
     * Kết thúc trận đấu (hết thời gian hoặc hoàn tất)
     */
    public void endMatch(String roomId) {
        MatchRoom room = rooms.get(roomId);
        if (room == null || "FINISHED".equals(room.getStatus())) return;

        room.setStatus("FINISHED");
        room.setEndTime(System.currentTimeMillis());

        PlayerState p1 = room.getPlayers().get(room.getCreatorId());
        PlayerState p2 = room.getPlayers().get(room.getOpponentId());

        int winner;
        if (p1.getScore() > p2.getScore()) winner = room.getCreatorId();
        else if (p2.getScore() > p1.getScore()) winner = room.getOpponentId();
        else winner = (p1.getLastScoreAt() < p2.getLastScoreAt())
                    ? room.getCreatorId()
                    : room.getOpponentId();

        // +100 điểm cho người thắng
        userService.addScore(winner, 100);

        // Gửi kết quả
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_result");
        payload.put("roomId", roomId);
        payload.put("winner", winner);
        payload.put("p1Score", p1.getScore());
        payload.put("p2Score", p2.getScore());
        String json = JsonUtil.toJson(payload);
        userService.sendToUser(room.getCreatorId(), json);
        userService.sendToUser(room.getOpponentId(), json);
    }

    /**
     * Kiểm tra phòng tồn tại
     */
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    /**
     * Xóa phòng sau khi xong
     */
    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    /**
     * Lấy thông tin phòng
     */
    public MatchRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }
}
