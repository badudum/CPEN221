package cpen221.mp1.ratemyprofessor;

import cpen221.mp1.autocompletion.gui.In;
import cpen221.mp1.datawrapper.DataWrapper;
import cpen221.mp1.ngrams.NGrams;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents DataAnalyzer for Rate My Professor Dataset.
 *
 * @author TangMartin, Badudum, W1nst0n03
 */
public class DataAnalyzer {

    private final ArrayList<Review> reviewList = new ArrayList<>();

    /**
     * Create an object to analyze a RateMyProfessor dataset.
     *
     * @param dataSourceFileName the name of the file that contains the data
     * @throws FileNotFoundException if the file does not exist or cannot be
     *                               found
     */
    public DataAnalyzer(String dataSourceFileName)
            throws FileNotFoundException {
        In input = new In(dataSourceFileName);
        String ignoreHeader = input.readLine();
        for (String line = input.readLine(); line != null; line = input.readLine()) {
            String currentReview = line;
            String[] x = currentReview.split(",", 3);
            float tempScore = Float.parseFloat(x[0]);
            char tempGender = x[1].charAt(0);
            String tempReview = x[2];
            reviewList.add(new Review(tempScore, tempGender, tempReview));
        }
    }

    /**
     * Return an n-grams object of a Review given the index of that review.
     *
     * @param reviewIndex the index of the review to be returned;
     *                    is between 0 and reviewList.length
     * @return an n-grams object of the review at the given index
     */
    public NGrams returnReviewGram(int reviewIndex) {
        return new NGrams(reviewList.get(reviewIndex).getReviewText());
    }

    /**
     * Return the list of Review objects for the DataAnalyzer.
     *
     * @return An ArrayList of the Reviews held by the DataAnalyzer object
     */
    public ArrayList<Review> getReviewList() {
        return new ArrayList<>(reviewList);
    }

    /**
     * Obtain a histogram with the number of occurrences of the
     * query term in the RMP comments, categorized as men-low (ML),
     * women-low (WL), men-medium (MM), women-medium (WM),
     * men-high (MH), and women-high (WH).
     *
     * @param query the search term, which contains between one and three words
     * @return the histogram with the number of occurrences of the
     * query term in the RMP comments, categorized as men-low (ML),
     * women-low (WL), men-medium (MM), women-medium (WM),
     * men-high (MH), and women-high (WH)
     */
    public Map<String, Long> getHistogram(String query) {
        Map<String, Long> histogram = new HashMap<>();
        int queryLength = query.split(" ").length;
        histogram.put("ML", 0L);
        histogram.put("WL", 0L);
        histogram.put("MM", 0L);
        histogram.put("WM", 0L);
        histogram.put("MH", 0L);
        histogram.put("WH", 0L);
        for (Review review: reviewList) {
            if (new NGrams(review.getReviewText()).
                    getAllNGrams().get(queryLength - 1).containsKey(query)) {
                if (review.getGender() == 'M') {
                    if (review.getScore() <= 2) {
                        histogram.replace("ML",
                                histogram.get("ML")
                                        + new NGrams(review.getReviewText()).
                                        getAllNGrams().get(queryLength - 1).
                                        get(query));
                    } else if (review.getScore() <= 3.5) {
                        histogram.replace("MM",
                                histogram.get("MM")
                                        + new NGrams(review.getReviewText()).
                                        getAllNGrams().get(queryLength - 1).
                                        get(query));
                    } else {
                        histogram.replace("MH",
                                histogram.get("MH")
                                        + new NGrams(review.getReviewText()).
                                        getAllNGrams().get(queryLength - 1).
                                        get(query));
                    }
                } else if (review.getGender() == 'W') {
                    if (review.getScore() <= 2) {
                        histogram.replace("WL",
                                histogram.get("WL")
                                        + new NGrams(review.getReviewText()).
                                        getAllNGrams().get(queryLength - 1).
                                        get(query));
                    } else if (review.getScore() <= 3.5) {
                        histogram.replace("WM",
                                histogram.get("WM")
                                        + new NGrams(review.getReviewText()).
                                        getAllNGrams().get(queryLength - 1).
                                        get(query));
                    } else {
                        histogram.replace("WH",
                                histogram.get("WH")
                                        + new NGrams(review.getReviewText()).
                                        getAllNGrams().get(queryLength - 1).
                                        get(query));
                    }
                }
            }
        }
        return histogram;
    }

}
