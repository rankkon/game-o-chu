package service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import model.MatchRoom;
import model.PlayerState;
import model.WordInstance;
import util.JsonUtil;

/**
 * MatchService xử lý toàn bộ logic tạo phòng, bắt đầu trận đấu, chấm điểm và kết thúc.
 */
public class MatchService {

    // Danh sách phòng đang hoạt động
    private Map<String, MatchRoom> rooms = new ConcurrentHashMap<>();
    // Scheduler for auto-ending matches when time expires
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> scheduledEndTasks = new ConcurrentHashMap<>();

    private final DictionaryService dictionaryService;
    private final UserService userService;
    private final dao.MatchDAO matchDAO = new dao.MatchDAO();
    private final dao.CategoryDAO categoryDAO = new dao.CategoryDAO();

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

    // Backward-compatible overload for String creatorId
    public MatchRoom createRoom(String creatorId) {
        try {
            return createRoom(Integer.parseInt(creatorId));
        } catch (NumberFormatException e) {
            return null;
        }
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

        // Lấy 5 từ ngẫu nhiên theo chủ đề (mặc định HOAQUA nếu chưa set)
        String categoryCode = room.getCategoryCode() != null ? room.getCategoryCode() : "HOAQUA";
        List<WordInstance> words = dictionaryService.getRandomWordsByCategory(categoryCode, 5);
        room.setWords(words);
        room.setCategoryCode(categoryCode);
        String categoryName = categoryDAO.getCategoryNameById(categoryDAO.getCategoryIdByCode(categoryCode));
        room.setCategoryName(categoryName);

        // Khởi tạo điểm
        room.setPlayers(new HashMap<Integer, PlayerState>());
        room.getPlayers().put(room.getCreatorId(), new PlayerState());
        room.getPlayers().put(room.getOpponentId(), new PlayerState());

        room.setStatus("PLAYING");

        for (WordInstance w : words) {
            int len = w.getAnswer().length();
            int reveal = Math.max(1, (int) Math.ceil(len * 0.3)); // reveal 30%
            w.revealRandomLetters(reveal);
        }

        // Lưu bản ghi match_start
        List<String> wordTexts = new ArrayList<>();
        for (WordInstance w : words) wordTexts.add(w.getHint());
        String wordsJson = util.JsonUtil.toJson(wordTexts);
    matchDAO.insertMatchStart(room.getCreatorId(), room.getOpponentId(), categoryName, wordsJson, new java.sql.Timestamp(room.getStartTime()));

        // Gửi message match_start cho cả 2
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_start");
        payload.put("roomId", room.getRoomId());
        payload.put("data", room.toDto());
        String json = JsonUtil.toJson(payload);
        userService.sendToUser(room.getCreatorId(), json);
        userService.sendToUser(room.getOpponentId(), json);

        long delayMs = Math.max(0, room.getEndTime() - System.currentTimeMillis());
        ScheduledFuture<?> prev = scheduledEndTasks.remove(roomId);
        if (prev != null && !prev.isDone()) prev.cancel(false);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                MatchRoom r = rooms.get(roomId);
                if (r != null && "PLAYING".equals(r.getStatus())) {
                    endMatch(roomId);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                scheduledEndTasks.remove(roomId);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
        scheduledEndTasks.put(roomId, future);
    }

    public void startMatch(String roomId, String opponentId) {
        try {
            startMatch(roomId, Integer.parseInt(opponentId));
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Khi người chơi nhập chữ trong 1 từ
     */
    public void handleLetterInput(String roomId, int playerId, int wordIdx, int charIdx, char ch) {
        MatchRoom room = rooms.get(roomId);
        if (room == null || !"PLAYING".equals(room.getStatus())) return;

        WordInstance word = room.getWords().get(wordIdx);
        // Normalize character to uppercase to avoid case-mix
        char upperCh = Character.toUpperCase(ch);
        boolean correct = word.handleInput(charIdx, upperCh);

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

    // Backward-compatible overload for String playerId
    public void handleLetterInput(String roomId, String playerId, int wordIdx, int charIdx, char ch) {
        try {
            handleLetterInput(roomId, Integer.parseInt(playerId), wordIdx, charIdx, ch);
        } catch (NumberFormatException ignored) {}
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

        // Cancel scheduled end task if exists
        ScheduledFuture<?> future = scheduledEndTasks.remove(roomId);
        if (future != null && !future.isDone()) future.cancel(false);

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

    // Cập nhật DB: kết thúc match
        // Lưu ý: để tối giản, chưa lưu match_id trong room; ở production nên lưu vào room
        // Ở đây không có matchId, bỏ qua update DB hoặc cần lưu trong room khi start

        // Gửi kết quả
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_result");
        payload.put("roomId", roomId);
        payload.put("winner", winner);
        // Include human-friendly winner name for clients
        try {
            model.User winnerUser = userService.getUserById(winner);
            String winnerName = (winnerUser != null && winnerUser.getName() != null) ? winnerUser.getName() : ("Player " + winner);
            payload.put("winnerName", winnerName);
        } catch (Exception ex) {
            payload.put("winnerName", "Player " + winner);
        }
        payload.put("p1Score", p1.getScore());
        payload.put("p2Score", p2.getScore());
        String json = JsonUtil.toJson(payload);
        userService.sendToUser(room.getCreatorId(), json);
        userService.sendToUser(room.getOpponentId(), json);
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public MatchRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }
}
