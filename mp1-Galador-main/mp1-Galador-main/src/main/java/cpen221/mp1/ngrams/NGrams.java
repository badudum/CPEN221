package cpen221.mp1.ngrams;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents NGrams.
 *
 * @author W1nst0n03
 */
public class NGrams {

    private final ArrayList<Long> uniqueCount = new ArrayList<>();
    private final List<HashMap<String, Long>> nGramList = new ArrayList<>();

    /**
     * Creates an NGrams object.
     *
     * @param text all the text to analyze and create n-grams from;
     *             is not null and is not empty.
     */
    public NGrams(String[] text) {
        int longestSentence = 0;
        ArrayList<String[]> sentenceList = new ArrayList<>();
        for (String sentence: text) {
            sentenceList.add(this.getWords(sentence));
            if (this.getWords(sentence).length > longestSentence) {
                longestSentence = this.getWords(sentence).length;
            }
        }
        for (int x = 0; x < longestSentence; x++) {
            HashMap<String, Long> nthGram = new HashMap<>();
            nGramList.add(nthGram);
            uniqueCount.add(0L);
        }

        for (int nGram = 0; nGram < longestSentence; nGram++) {
            for (String[] strings : sentenceList) {
                for (int j = 0; j < strings.length - nGram; j++) {
                    StringBuilder gramBuilder = new StringBuilder();
                    for (int getGram = 0; getGram <= nGram; getGram++) {
                        gramBuilder.append(strings[j + getGram]);
                        if (getGram < nGram) {
                            gramBuilder.append(" ");
                        }
                    }
                    String currentNgram = gramBuilder.toString();
                    if (nGramList.get(nGram).containsKey(currentNgram)) {
                        nGramList.get(nGram).replace(currentNgram,
                                nGramList.get(nGram).get(currentNgram) + 1);
                    } else {
                        nGramList.get(nGram).put(currentNgram, 1L);
                        uniqueCount.set(nGram, uniqueCount.get(nGram) + 1L);
                    }
                }
            }
        }
    }

    /**
     * Creates an NGrams object.
     *
     * @param text all the text to analyze and create n-grams from;
     *             is not null and is not empty.
     * @param gramCount Indicator to only make a specific amount of ngrams;
     *                  is not 0, and is not larger than the longest sentence
     */
    public NGrams(String[] text, int gramCount) {
        int longestSentence = 0;
        ArrayList<String[]> sentenceList = new ArrayList<>();
        for (String sentence: text) {
            sentenceList.add(this.getWords(sentence));
            if (this.getWords(sentence).length > longestSentence) {
                longestSentence = this.getWords(sentence).length;
            }
        }
        for (int x = 0; x < longestSentence; x++) {
            HashMap<String, Long> nthGram = new HashMap<>();
            nGramList.add(nthGram);
            uniqueCount.add(0L);
        }

        for (int nGram = 0; nGram < gramCount; nGram++) {
            for (String[] strings : sentenceList) {
                for (int j = 0; j < strings.length - nGram; j++) {
                    StringBuilder gramBuilder = new StringBuilder();
                    for (int getGram = 0; getGram <= nGram; getGram++) {
                        gramBuilder.append(strings[j + getGram]);
                        if (getGram < nGram) {
                            gramBuilder.append(" ");
                        }
                    }
                    String currentNgram = gramBuilder.toString();
                    if (nGramList.get(nGram).containsKey(currentNgram)) {
                        nGramList.get(nGram).replace(currentNgram,
                                nGramList.get(nGram).get(currentNgram) + 1);
                    } else {
                        nGramList.get(nGram).put(currentNgram, 1L);
                        uniqueCount.set(nGram, uniqueCount.get(nGram) + 1L);
                    }
                }
            }
        }
    }

    /**
     * Obtain the total number of unique 1-grams,
     * 2-grams, ..., n-grams.
     * Specifically, if there are m_i i-grams,
     * obtain sum_{i=1}^{n} m_i.
     *
     * @return the total number of 1-grams,
     * 2-grams, ..., n-grams
     * @param n is the upper bounds of n-grams that will be checked for
     */
    public long getTotalNGramCount(int n) {
        long uniqueToN = 0;
        for (int i = 0; i < n && i < uniqueCount.size(); i++) {
            uniqueToN += uniqueCount.get(i);
        }
        return uniqueToN;
    }

    /**
     * Get the n-grams, as a List, with the i-th entry being
     * all the (i+1)-grams and their counts.
     *
     * @return a list of n-grams and their associated counts,
     * with the i-th entry being all the (i+1)-grams and their counts
     */
    public List<Map<String, Long>> getAllNGrams() {
        return new ArrayList<>(nGramList);
    }

    /**
     * Get the αll of the individual words in a string and input them
     * into an array.
     *
     * @param text String to be divided into individual words
     * @return an array strings where each string is an individual word
     *         in the given string
     */
    private String[] getWords(String text) {
        ArrayList<String> words = new ArrayList<>();
        BreakIterator wb = BreakIterator.getWordInstance();
        wb.setText(text);
        int start = wb.first();
        for (int end = wb.next(); end
                != BreakIterator.DONE; start = end, end = wb.next()) {
            String word = text.substring(start, end).toLowerCase();
            word = word.replaceAll("^\\s*\\p{Punct}+\\s*",
                    "").replaceAll("\\s*\\p{Punct}+\\s*$", "");
            if (!word.equals(" ") && !word.equals("")) {
                words.add(word);
            }
        }
        String[] wordsArray = new String[words.size()];
        words.toArray(wordsArray);
        return wordsArray;
    }

}

