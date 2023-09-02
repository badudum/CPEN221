package cpen221.mp1.autocompletion;

import cpen221.mp1.searchterm.SearchTerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AutoCompletor {

    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private List<SearchTerm> searchTerms = new ArrayList<>();

    /**
     * Creates an AutoCompleter object to autocomplete words based on
     * a provided dataset.
     *
     * @param searchTerms an array of SearchTerm to be used
     *                    as AutoCompletor data;
     *                    is not null and not empty
     */
    public AutoCompletor(SearchTerm[] searchTerms) {
        this.searchTerms = Arrays.asList(searchTerms);
        this.searchTerms.sort(SearchTerm.byPrefixOrder());
        this.searchTerms.sort(SearchTerm.byWeightOrder());
    }

    /**
     * Creates an AutoCompleter object to autocomplete words based on a
     * provided dataset.
     *
     * @param searchTerms a List of Maps of n-grams in the exact format created
     *                    by an NGrams object;
     *                    The frequency corresponds to the weight each n-gram's
     *                    SearchTerm;
     *                    is not null and not empty;
     */
    public AutoCompletor(List<Map<String, Long>> searchTerms) {
        for (Map<String, Long> gramMap: searchTerms) {
            String[] keySet = gramMap.keySet().toArray(new String[0]);
            for (String nGram: keySet) {
                this.searchTerms.add(new SearchTerm(nGram, gramMap.get(nGram)));
            }
        }
        this.searchTerms.sort(SearchTerm.byPrefixOrder());
        this.searchTerms.sort(SearchTerm.byWeightOrder());
    }

    /**
     * Get all the matches for a given prefix.
     *
     * @param prefix string to be searched for
     * @return an SearchTerm array of all prefix matches
     */
    public SearchTerm[] allMatches(String prefix) {
        List<SearchTerm> returnTerms = new ArrayList<>();
        for (SearchTerm currentTerm: this.searchTerms) {
            if (currentTerm.returnQuery().startsWith(prefix)) {
                returnTerms.add(currentTerm);
            }
        }
        return returnTerms.toArray(new SearchTerm[0]);
    }

    /**
     * Get the top-K matches for a give prefix up to a given integer limit, K.
     *
     * @param prefix string to be searched for
     * @param limit integer K, limit to how many of the top SearchTerms to
     *              return
     * @return an SearchTerm array of the top prefix matches up to a given limit
     */
    public SearchTerm[] topKMatches(String prefix, int limit) {
        List<SearchTerm> returnTerms = new ArrayList<>();
        int currentIndex = 0;
        for (SearchTerm searchTerm : this.searchTerms) {
            if (searchTerm.returnQuery().length() >= prefix.length()) {
                if (searchTerm.returnQuery().startsWith(prefix)) {
                    returnTerms.add(searchTerm);
                    currentIndex++;
                }
                if (currentIndex == limit) {
                    break;
                }
            }
        }
        return returnTerms.toArray(new SearchTerm[0]);
    }

    /**
     * Get the top matches for a give prefix up to the DEFAULT_SEARCH_LIMIT.
     *
     * @param prefix string to be searched for
     * @return an SearchTerm array of the top prefix matches up to the
     *         DEFAULT_SEARCH_LIMIT
     */
    public SearchTerm[] topKMatches(String prefix) {
        return topKMatches(prefix, DEFAULT_SEARCH_LIMIT);
    }

    /**
     * Get the total number of matches to a prefix.
     *
     * @param prefix string to be searched for
     * @return an integer representing the total number of matches within the
     *         object's dataset
     */
    public int numberOfMatches(String prefix) {
        return this.allMatches(prefix).length;
    }

}
