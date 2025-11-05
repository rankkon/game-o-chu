package service;

import java.util.List;

import dao.RankingDAO;
import model.Ranking;

public class RankingService {
    private static RankingService instance;
    private final RankingDAO rankingDAO;
    private static final int DEFAULT_RANKING_LIMIT = 10;

    private RankingService() {
        this.rankingDAO = RankingDAO.getInstance();
    }

    public static synchronized RankingService getInstance() {
        if (instance == null) {
            instance = new RankingService();
        }
        return instance;
    }

    public List<Ranking> getTopPlayers() {
        return getTopPlayers(DEFAULT_RANKING_LIMIT);
    }

    public List<Ranking> getTopPlayers(int limit) {
        // Giới hạn số lượng từ 1 đến 100
        int safeLimit = Math.min(Math.max(1, limit), 100);
        return rankingDAO.getTopPlayers(safeLimit);
    }
}