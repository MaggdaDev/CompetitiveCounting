package competitivecounting.vaults.trophyvault;

import competitivecounting.vaults.Riddle;

import java.util.ArrayList;
import java.util.List;

public class TrophyRiddleFactory {
    private final static String RIDDLE_TEXT = "Find the number hidden by '???' in the following text:\n\n```{0}```";
    private final static String RIDDLE_SOLUTION_EXPLANATION = "Congratulations, you have proven to possess profound knowledge of {0}!";

    public static Riddle createRiddle(WikiArticelObject wikiArticelObject) {
        List<SectionObject> sections = List.of(wikiArticelObject.getSectionObjects());
        List<Integer> sectionIdxCandidates = new ArrayList<>();
        List<Integer> wordIdxCandidates = new ArrayList<>();

        for (int i = 0; i < sections.size(); i++) {
            SectionObject section = sections.get(i);
            List<String> words = section.getWords();
            for (int j = 0; j < words.size(); j++) {
                String word = words.get(j);
                int idxLeadingLetter = section.getWordStartIndices()[j] - 1;
                char leadingChar = idxLeadingLetter >= 0 ? section.getContent().charAt(idxLeadingLetter) : ' ';
                if (acceptAsNumber(word, leadingChar)) {
                    sectionIdxCandidates.add(i);
                    wordIdxCandidates.add(j);
                }
            }
        }

        if (sectionIdxCandidates.isEmpty()) {
            return null;
        }

        // Select candidate
        int randomIdx = (int) (Math.random() * sectionIdxCandidates.size());
        int sectionIdx = sectionIdxCandidates.get(randomIdx);
        int wordIdx = wordIdxCandidates.get(randomIdx);
        String solutionWord = sections.get(sectionIdx).getWords().get(wordIdx);
        SectionObject sectionObject = sections.get(sectionIdx);
        int sentenceIdx = sectionObject.getWordSentenceIndices()[wordIdx];

        // Replace solution by ???
        String sectionContent = sectionObject.getContent();
        int startIndexOfSolutionWord = sectionObject.getWordStartIndices()[wordIdx];
        int endIndexOfSolutionWord = sectionObject.getWordEndIndices()[wordIdx];
        int solutionLengthMinusThree = 3-solutionWord.length();
        String contentReplaced =  new StringBuilder(sectionContent)
                .replace(startIndexOfSolutionWord, endIndexOfSolutionWord, "???")
                .toString();

        int[] indices = getNeighborIndices(sentenceIdx, sectionObject.getSentences().length);
        int startIndexRiddle = sectionObject.getSentenceStartIndices()[indices[0]];
        int endIndexRiddle = sectionObject.getSentenceEndIndices()[indices[indices.length - 1]];
        String riddleContent = contentReplaced.substring(startIndexRiddle, endIndexRiddle + solutionLengthMinusThree);
        int solution = Integer.parseInt(solutionWord);


        return new Riddle(RIDDLE_TEXT.replace("{0}",riddleContent), solution, RIDDLE_SOLUTION_EXPLANATION.replace("{0}", wikiArticelObject.getTitle()));
    }

    private static boolean acceptAsNumber(String word, char leadingChar) {
        int num;
        if (leadingChar == ',' || leadingChar == '.') {
            return false;
        }
        try {
            num = Integer.parseInt(word);
        } catch (NumberFormatException e) {
            return false;
        }
        if (num <= 60 || (num > 1000 && num < 2050)) {
            return false;
        }
        return true;
    }

    public static int[] getNeighborIndices(int i, int arrayLength) {
        if (arrayLength <= 0) {
            return new int[0];
        }
        int count = Math.min(3, arrayLength);
        int start;
        // Prefer i - 1, i, i + 1
        if (i - 1 >= 0 && i + 1 < arrayLength) {
            start = i - 1;
            // Left border: i, i + 1, i + 2
        } else if (i - 1 < 0) {
            start = i;

            // Right border: i - 2, i - 1, i
        } else {
            start = i - count + 1;
        }
        // Ensure the range stays inside the array
        start = Math.max(0, Math.min(start, arrayLength - count));
        int[] result = new int[count];
        for (int j = 0; j < count; j++) {
            result[j] = start + j;
        }
        return result;
    }
}
