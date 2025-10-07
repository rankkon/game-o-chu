package service;

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
        if (Character.toUpperCase(answer.charAt(idx)) == Character.toUpperCase(ch)) {
            filled[idx] = answer.charAt(idx);
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
}
