package competitivecounting.vaults.trophyvault;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SectionObjectFactory {
    private static final char TEMP_PERIOD = '\uE000';
    private static final Pattern CAPITAL_FOLLOWING_ABBREVIATION =
            Pattern.compile(
                    "\\b(?:"
                            + "St"
                            + "|Dr"
                            + "|Prof"
                            + "|Mr"
                            + "|Mrs"
                            + "|Ms"
                            + "|Sr"
                            + "|Jr"
                            + "|Gen"
                            + "|Rep"
                            + "|Sen"
                            + "|Gov"
                            + "|Pres"
                            + "|Rev"
                            + "|Hon"
                            + "|Col"
                            + "|Maj"
                            + "|Capt"
                            + "|Lt"
                            + "|Sgt"
                            + "|Mt"
                            + "|Ft"
                            + "|No"
                            + "|Fig"
                            + "|Eq"
                            + "|Vol"
                            + "|Ch"
                            + "|[A-ZÄÖÜ]"   // Single-letter abbreviations (e.g., "A.", "B.", "C.")
                            + ")\\.(?=\\s+[A-ZÄÖÜ])"
            );

    private SectionObjectFactory() {
    }

    private static String protectAbbreviationPeriods(String text) {
        Matcher matcher =
                CAPITAL_FOLLOWING_ABBREVIATION.matcher(text);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String match = matcher.group();

            String replacement =
                    match.substring(0, match.length() - 1)
                            + TEMP_PERIOD;

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }


    public static SectionObject create(
            String title,
            String content
    ) {
        List<String> words = new ArrayList<>();
        List<Integer> startIndices = new ArrayList<>();
        List<Integer> endIndices = new ArrayList<>();
        List<Integer> sentenceStartIndices = new ArrayList<>();
        List<Integer> sentenceEndIndices = new ArrayList<>();
        List<String> sentences = new ArrayList<>();
        List<Integer> wordSentenceIndices =
                new ArrayList<>();

        /*
         * First find all sentences.
         */
        BreakIterator sentenceIterator =
                BreakIterator.getSentenceInstance(
                        Locale.ENGLISH
                );

        sentenceIterator.setText(protectAbbreviationPeriods(content));

        int sentenceStart =
                sentenceIterator.first();

        int sentenceEnd =
                sentenceIterator.next();

        int sentenceIndex = 0;

        while (sentenceEnd
                != BreakIterator.DONE) {

            String sentence =
                    content.substring(
                            sentenceStart,
                            sentenceEnd
                    );

            /*
             * Ignore sentences consisting only
             * of whitespace.
             */
            if (!sentence.trim().isEmpty()) {

                /*
                 * Store the sentence including
                 * its punctuation.
                 */
                sentences.add(sentence.trim());
                sentenceStartIndices.add(sentenceStart);
                sentenceEndIndices.add(sentenceEnd);

                /*
                 * Find all words inside
                 * this sentence.
                 */
                int index = sentenceStart;

                while (index < sentenceEnd) {

                    /*
                     * Skip punctuation and
                     * whitespace.
                     */
                    while (index < sentenceEnd
                            && !Character.isLetterOrDigit(
                            content.charAt(index))) {

                        index++;
                    }

                    if (index >= sentenceEnd) {
                        break;
                    }

                    int wordStart = index;

                    /*
                     * Read the complete word.
                     */
                    while (index < sentenceEnd
                            && Character.isLetterOrDigit(
                            content.charAt(index))) {

                        index++;
                    }

                    int wordEnd = index;

                    words.add(
                            content.substring(
                                    wordStart,
                                    wordEnd
                            )
                    );
                    startIndices.add(wordStart);
                    endIndices.add(wordEnd);

                    /*
                     * Map this word to the
                     * current sentence.
                     */
                    wordSentenceIndices.add(
                            sentenceIndex
                    );
                }
                sentenceIndex++;
            }
            sentenceStart = sentenceEnd;
            sentenceEnd =
                    sentenceIterator.next();
        }
        int[] starts = new int[startIndices.size()];
        int[] ends = new int[endIndices.size()];
        int[] sentenceIndices = new int[wordSentenceIndices.size()];
        for (int i = 0; i < starts.length; i++) {
            starts[i] = startIndices.get(i);
            ends[i] = endIndices.get(i);
            sentenceIndices[i] = wordSentenceIndices.get(i);
        }

        return new SectionObject(
                title,
                content,
                words,
                starts,
                ends,
                sentenceStartIndices.stream()
                        .mapToInt(Integer::intValue)
                        .toArray(),
                sentenceEndIndices.stream()
                        .mapToInt(Integer::intValue)
                        .toArray(),
                sentences.toArray(new String[0]),
                sentenceIndices
        );
    }
}