package model;

public class WordInstance {
    private String answer;
    private String hint;
    private char[] filled;
    private boolean[] wasTried;
    private boolean bonusGiven;

    public WordInstance(String answer, String hint) {
        this.answer = answer.toUpperCase();
        this.hint = hint;
        this.filled = new char[answer.length()];
        this.wasTried = new boolean[answer.length()];
        for (int i = 0; i < answer.length(); i++) filled[i] = '_';
    }

    public boolean handleInput(int idx, char ch) {
        char target = Character.toUpperCase(answer.charAt(idx));
        char input = Character.toUpperCase(ch);
        if (target == input) {
            filled[idx] = answer.charAt(idx); // keep original casing as answer (upper)
            return true;
        }
        wasTried[idx] = true;
        return false;
    }

    public boolean isFullyCorrect() {
        for (int i = 0; i < answer.length(); i++)
            if (filled[i] != answer.charAt(i)) return false;
        return true;
    }

    public String getAnswer() { return answer; }
    public String getHint() { return hint; }
    public char[] getFilled() { return filled; }
    public boolean[] getWasTried() { return wasTried; }
    public boolean isBonusGiven() { return bonusGiven; }

    public boolean wasTried(int idx) { return wasTried[idx]; }
    public void setTried(int idx, boolean value) { wasTried[idx] = value; }
    public void setBonusGiven(boolean bonusGiven) { this.bonusGiven = bonusGiven; }

    /**
     * Reveal a number of random letters in this word as initial hints.
     * Revealed letters are marked as tried so they won't award points when
     * a player types them.
     */
    public void revealRandomLetters(int count) {
        if (count <= 0) return;
        java.util.List<Integer> idxs = new java.util.ArrayList<>();
        for (int i = 0; i < answer.length(); i++) {
            if (filled[i] != answer.charAt(i)) idxs.add(i);
        }
        java.util.Collections.shuffle(idxs, new java.util.Random());
        int n = Math.min(count, idxs.size());
        for (int k = 0; k < n; k++) {
            int idx = idxs.get(k);
            filled[idx] = answer.charAt(idx);
            wasTried[idx] = true; // mark as already revealed
        }
    }

    /**
     * Reveal letters at the specified indices. This is used to apply the same
     * reveal pattern to multiple per-player copies so both players see the
     * same hint letters but keep independent filled/wasTried state.
     */
    public void revealAtIndices(java.util.List<Integer> indices) {
        if (indices == null || indices.isEmpty()) return;
        for (Integer idx : indices) {
            if (idx >= 0 && idx < filled.length) {
                filled[idx] = answer.charAt(idx);
                wasTried[idx] = true;
            }
        }
    }
}


