package competitivecounting.vaults.trophyvault;
import java.util.List;

public class SectionObject {

    private final String title;
    private final String content;

    private final List<String> words;

    private final int[] wordStartIndices;
    private final int[] wordEndIndices;

    private final int[] sentenceStartIndices;
    private final int[] sentenceEndIndices;

    private final String[] sentences;

    /*
     * wordSentenceIndices[i] contains the index
     * of the sentence containing words.get(i).
     */
    private final int[] wordSentenceIndices;

    public SectionObject(
            String title,
            String content,
            List<String> words,
            int[] wordStartIndices,
            int[] wordEndIndices,
            int[] sentenceStartIndices,
            int[] sentenceEndIndices,
            String[] sentences,
            int[] wordSentenceIndices
    ) {
        this.title = title;
        this.content = content;

        this.words = List.copyOf(words);

        this.wordStartIndices =
                wordStartIndices.clone();

        this.wordEndIndices =
                wordEndIndices.clone();

        this.sentences =
                sentences.clone();

        this.wordSentenceIndices =
                wordSentenceIndices.clone();
        this.sentenceStartIndices =
                sentenceStartIndices.clone();
        this.sentenceEndIndices =
                sentenceEndIndices.clone();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<String> getWords() {
        return words;
    }

    public int[] getWordStartIndices() {
        return wordStartIndices.clone();
    }

    public int[] getWordEndIndices() {
        return wordEndIndices.clone();
    }

    public String[] getSentences() {
        return sentences.clone();
    }

    public int[] getWordSentenceIndices() {
        return wordSentenceIndices.clone();
    }

    public int[] getSentenceStartIndices() {
        return sentenceStartIndices;
    }

    public int[] getSentenceEndIndices() {
        return sentenceEndIndices;
    }
}