package competitivecounting.vaults;

public class Riddle {
    private final String question;
    private final int answer;
    private final String solutionExplanation;

    public Riddle(String question, int answer, String solutionExplanation) {
        this.question = question;
        this.answer = answer;
        this.solutionExplanation = solutionExplanation;
    }

    public String getQuestion() {
        return question;
    }

    public int getAnswer() {
        return answer;
    }

    public String getSolutionExplanation() {
        return solutionExplanation;
    }
}
