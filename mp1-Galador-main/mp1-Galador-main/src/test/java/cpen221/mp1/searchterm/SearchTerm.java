package cpen221.mp1.searchterm;

import java.util.Comparator;

public class SearchTerm implements Comparable<SearchTerm> {

    private final String query;
    private final long weight;

    /**
     * Initializes a term with the given query string and weight.
     *
     * @param query the query for the search, is not empty
     * @param weight the weight associated with the query string
     *
     */
    public SearchTerm(String query, long weight) {
        this.query  = query;
        this.weight = weight;
    }

    /**
     * Obtain a comparator for comparing two search terms based on weight.
     *
     * @return a comparator that compares two search terms using their weight
     */
    public static Comparator<SearchTerm> byWeightOrder() {
        return (o1, o2) -> {
            long temp = o1.weight - o2.weight;
            return temp < 0 ? 1 : (temp > 0 ? -1 : 0);
        };
    }

    /**
     * Obtain a comparator for lexicographic ordering.
     *
     * @return a comparator that compares two search terms lexicographically
     */
    public static Comparator<SearchTerm> byPrefixOrder() {
        return (o1, o2) -> {
            String term1 = o1.query.replaceAll("[^A-Za-z ]+", "").toLowerCase();
            String term2 = o2.query.replaceAll("[^A-Za-z ]+", "").toLowerCase();
            int x = term1.compareTo(term2);
            return Integer.compare(x, 0);
        };
    }

    /**
     * Returns a string representation of this SearchTerm.
     *
     * @return a string representation of this term in the following format:
     * the weight, followed by a tab, followed by the query
     */
    public String toString() {
        return String.format("%-10d\t%s", this.weight, this.query);
    }

    /**
     * Returns the query of the term.
     *
     * @return A string of the query.
     */
    public String returnQuery() {
        return query;
    }

    /**
     * Compares the two terms in lexicographic order by query.
     */
    @Override
    public int compareTo(SearchTerm other) {
        return SearchTerm.byPrefixOrder().compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SearchTerm otherST) {
            return (this.query.equals(otherST.query)
                    && this.weight == otherST.weight);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) weight;
    }

}
