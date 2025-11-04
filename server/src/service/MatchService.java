package service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
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
    List<WordInstance> canonicalWords = dictionaryService.getRandomWordsByCategory(categoryCode, 5);
    // Keep canonical words for hints/length only
    room.setWords(canonicalWords);
        room.setCategoryCode(categoryCode);
        String categoryName = categoryDAO.getCategoryNameById(categoryDAO.getCategoryIdByCode(categoryCode));
        room.setCategoryName(categoryName);

    // Khởi tạo điểm và per-player copies of words (so each player has their own filled/wasTried state)
    room.setPlayers(new HashMap<Integer, PlayerState>());
    PlayerState ps1 = new PlayerState();
    PlayerState ps2 = new PlayerState();
    room.getPlayers().put(room.getCreatorId(), ps1);
    room.getPlayers().put(room.getOpponentId(), ps2);

        room.setStatus("PLAYING");

        // Reveal initial letters in each canonical word as hints (30% of letters, min 1)
        // We'll compute reveal positions once and apply the same indices to both players' copies
        for (WordInstance canonical : canonicalWords) {
            int len = canonical.getAnswer().length();
            int reveal = Math.max(1, (int) Math.ceil(len * 0.30)); // reveal 30% per user request

            // Choose indices to reveal
            java.util.List<Integer> idxs = new java.util.ArrayList<>();
            for (int i = 0; i < len; i++) idxs.add(i);
            java.util.Collections.shuffle(idxs, new java.util.Random());
            java.util.List<Integer> pick = idxs.subList(0, Math.min(reveal, idxs.size()));

            // Apply to canonical (for hint data) and to per-player copies
            canonical.revealAtIndices(pick);
        }

        // Create per-player deep copies of word instances and apply the same revealed indices
        java.util.List<WordInstance> copies1 = new java.util.ArrayList<>();
        java.util.List<WordInstance> copies2 = new java.util.ArrayList<>();
        for (WordInstance canonical : canonicalWords) {
            // Determine which indices are revealed in canonical
            java.util.List<Integer> revealedIdx = new java.util.ArrayList<>();
            char[] filled = canonical.getFilled();
            for (int i = 0; i < filled.length; i++) {
                if (filled[i] == canonical.getAnswer().charAt(i)) revealedIdx.add(i);
            }

            // Create copies for each player
            WordInstance c1 = new WordInstance(canonical.getAnswer(), canonical.getHint());
            WordInstance c2 = new WordInstance(canonical.getAnswer(), canonical.getHint());
            c1.revealAtIndices(revealedIdx);
            c2.revealAtIndices(revealedIdx);
            copies1.add(c1);
            copies2.add(c2);
        }

        ps1.setPersonalWords(copies1);
        ps2.setPersonalWords(copies2);

        // Lưu bản ghi match_start và lưu matchId trả về vào room để dùng khi kết thúc
        List<String> wordTexts = new ArrayList<>();
        for (WordInstance w : canonicalWords) wordTexts.add(w.getHint());
        String wordsJson = util.JsonUtil.toJson(wordTexts);
        Integer matchId = matchDAO.insertMatchStart(room.getCreatorId(), room.getOpponentId(), categoryName, wordsJson, new java.sql.Timestamp(room.getStartTime()));
        if (matchId != null) {
            room.setMatchId(matchId);
        }

        // Gửi message match_start cho cả 2
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_start");
        payload.put("roomId", room.getRoomId());
    payload.put("data", room.toDto());
        String json = JsonUtil.toJson(payload);
        userService.sendToUser(room.getCreatorId(), json);
        userService.sendToUser(room.getOpponentId(), json);

        // Schedule automatic end when endTime is reached. Calculate delay relative to now.
        long delayMs = Math.max(0, room.getEndTime() - System.currentTimeMillis());
        // Cancel any previously scheduled task (defensive)
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

    // Backward-compatible overload for String opponentId
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

        // Use player's personal copy of the word so each player has independent filled state
        PlayerState ps = room.getPlayers().get(playerId);
        if (ps == null) return;
        java.util.List<WordInstance> pWords = ps.getPersonalWords();
        if (pWords == null || wordIdx < 0 || wordIdx >= pWords.size()) return;

        WordInstance word = pWords.get(wordIdx);
        // If this position is already revealed/fixed (hint or previously correct), ignore attempts to change it
        char[] filled = word.getFilled();
        if (charIdx >= 0 && charIdx < filled.length) {
            if (filled[charIdx] == word.getAnswer().charAt(charIdx)) {
                // fixed cell (revealed or already correct) — do not allow modifications
                return;
            }
        }
        // Normalize character to uppercase to avoid case-mix
        char upperCh = Character.toUpperCase(ch);
        boolean correct = word.handleInput(charIdx, upperCh);

        if (correct && !word.wasTried(charIdx)) {
            ps.addScore(1);
            word.setTried(charIdx, true);
        }

        if (word.isFullyCorrect() && !word.isBonusGiven()) {
            ps.addScore(2);
            ps.addCorrectWord();
            word.setBonusGiven(true);
        }

        // Broadcast the room DTO (which now contains per-player revealed patterns)
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
     * Xử lý tin nhắn chat trong phòng
     */
    public void handleChatMessage(String roomId, int senderId, String message) {
        MatchRoom room = rooms.get(roomId);
        if (room == null || room.getMatchId() == null) return;

        // Lấy tên người gửi
        String senderName = "Người chơi";
        try {
            model.User sender = userService.getUserById(senderId);
            if (sender != null && sender.getName() != null) {
                senderName = sender.getName();
            }
        } catch (Exception ignored) {}

        // Lưu vào database
        matchDAO.appendChatLog(room.getMatchId(), senderName, message, new java.sql.Timestamp(System.currentTimeMillis()));

        // Gửi tin nhắn đến cả 2 người chơi
        JsonObject chatMessage = new JsonObject();
        chatMessage.addProperty("type", "CHAT_MESSAGE");
        chatMessage.addProperty("roomId", roomId);
        chatMessage.addProperty("senderName", senderName);
        chatMessage.addProperty("message", message);
        String json = chatMessage.toString();
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

        // Determine end and winner
        long now = System.currentTimeMillis();
        // compute remaining time relative to scheduled end (could be 0 if timed out)
        long scheduledEnd = room.getEndTime();
        long remainingMs = Math.max(0, scheduledEnd - now);
        Integer winnerTimeRemainingSec = null; // seconds

        PlayerState p1 = room.getPlayers().get(room.getCreatorId());
        PlayerState p2 = room.getPlayers().get(room.getOpponentId());

        int winnerId;
        if (p1.getScore() > p2.getScore()) winnerId = room.getCreatorId();
        else if (p2.getScore() > p1.getScore()) winnerId = room.getOpponentId();
        else winnerId = (p1.getLastScoreAt() < p2.getLastScoreAt()) ? room.getCreatorId() : room.getOpponentId();

        // remaining time only meaningful for the winner
        winnerTimeRemainingSec = (int) (remainingMs / 1000);

        // Set room end time to now and mark finished
        room.setEndTime(now);
        room.setStatus("FINISHED");

    // Update user stats: winner +100 score, both players MatchCount++ and Win/Lose++ accordingly
    Integer loserId = (winnerId == room.getCreatorId()) ? room.getOpponentId() : room.getCreatorId();
    userService.recordMatchResult(winnerId, loserId, 100);

        // Persist end info to DB if we have a matchId
        Integer mid = room.getMatchId();
        if (mid != null) {
            int p1Score = (p1 != null) ? p1.getScore() : 0;
            int p2Score = (p2 != null) ? p2.getScore() : 0;
            matchDAO.updateMatchEnd(mid, winnerId, loserId, p1Score, p2Score, winnerTimeRemainingSec, new java.sql.Timestamp(room.getEndTime()));
        }

        // Gửi kết quả
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "match_result");
        payload.put("roomId", roomId);
        payload.put("winner", winnerId);
        // Include human-friendly winner name for clients
        try {
            model.User winnerUser = userService.getUserById(winnerId);
            String winnerName = (winnerUser != null && winnerUser.getName() != null) ? winnerUser.getName() : ("Player " + winnerId);
            payload.put("winnerName", winnerName);
        } catch (Exception ex) {
            payload.put("winnerName", "Player " + winnerId);
        }
        payload.put("p1Score", (p1 != null) ? p1.getScore() : 0);
        payload.put("p2Score", (p2 != null) ? p2.getScore() : 0);
        payload.put("winnerTimeRemaining", winnerTimeRemainingSec);
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

    /**
     * Gửi tin nhắn đến tất cả người chơi trong phòng
     */
    public void broadcastToRoom(String roomId, String message) {
        MatchRoom room = rooms.get(roomId);
        if (room != null) {
            userService.sendToUser(room.getCreatorId(), message);
            userService.sendToUser(room.getOpponentId(), message);
        }
    }
}
