package controller;

import model.Ranking;
import service.RankingService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

public class RankingController {
    private static RankingController instance;
    private final RankingService rankingService;

    private RankingController() {
        this.rankingService = RankingService.getInstance();
    }

    public static synchronized RankingController getInstance() {
        if (instance == null) {
            instance = new RankingController();
        }
        return instance;
    }

    public void handleGetRankings(ClientHandler client) {
        List<Ranking> rankings = rankingService.getTopPlayers();
        JsonObject response = createRankingResponse(rankings);
        client.sendMessage("RANKING_UPDATE", response);
    }

    public void broadcastRankingUpdate(List<ClientHandler> clients) {
        List<Ranking> rankings = rankingService.getTopPlayers();
        JsonObject response = createRankingResponse(rankings);
        
        for (ClientHandler client : clients) {
            client.sendMessage("RANKING_UPDATE", response);
        }
    }

    private JsonObject createRankingResponse(List<Ranking> rankings) {
        JsonObject response = new JsonObject();
        JsonArray rankingsArray = new JsonArray();

        for (Ranking rank : rankings) {
            JsonObject rankObj = new JsonObject();
            rankObj.addProperty("rank", rank.getRank());
            rankObj.addProperty("username", rank.getUsername());
            rankObj.addProperty("totalMatches", rank.getTotalMatches());
            rankObj.addProperty("wonMatches", rank.getWonMatches());
            rankObj.addProperty("totalScore", rank.getTotalScore());
            rankObj.addProperty("winRate", rank.getWinRate());
            rankObj.addProperty("avgTimeRemaining", rank.getAvgTimeRemaining());
            rankingsArray.add(rankObj);
        }

        response.add("rankings", rankingsArray);
        return response;
    }
}