package service;

import java.util.List;

import dao.DictionaryDAO;
import model.WordInstance;

/**
 * DictionaryService: lấy từ ngẫu nhiên theo chủ đề từ DB (bảng dictionary/category)
 */
public class DictionaryService {
    private final DictionaryDAO dictionaryDAO;

    public DictionaryService() {
        this.dictionaryDAO = new DictionaryDAO();
    }

    public List<WordInstance> getRandomWordsByCategory(String categoryCode, int n) {
        return dictionaryDAO.getRandomWordsByCategoryCode(categoryCode, n);
    }
}


