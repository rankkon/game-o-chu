package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import service.WordInstance;

/**
 * Simple dictionary loader that reads words from project-level dictionary.txt
 * Each line: WORD|HINT (hint optional). If missing hint, use empty string.
 */
public class DictionaryService {
    private final List<WordInstance> words;

    public DictionaryService() {
        this.words = loadWords();
    }

    private List<WordInstance> loadWords() {
        List<WordInstance> list = new ArrayList<>();
        try {
            File dict = new File("dictionary.txt");
            if (!dict.exists()) return list;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dict), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\|", 2);
                    String answer = parts[0].trim();
                    String hint = parts.length > 1 ? parts[1].trim() : "";
                    if (!answer.isEmpty()) {
                        list.add(new WordInstance(answer, hint));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    public List<WordInstance> getRandomWords(int n) {
        if (words.isEmpty()) return new ArrayList<>();
        List<WordInstance> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled);
        List<WordInstance> picked = new ArrayList<>();
        for (int i = 0; i < Math.min(n, shuffled.size()); i++) {
            WordInstance w = shuffled.get(i);
            picked.add(new WordInstance(w.getAnswer(), w.getHint()));
        }
        return picked;
    }
}


