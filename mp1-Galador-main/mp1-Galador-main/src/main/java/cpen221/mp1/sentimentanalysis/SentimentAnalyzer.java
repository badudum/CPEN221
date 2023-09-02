package cpen221.mp1.sentimentanalysis;

import cpen221.mp1.ratemyprofessor.DataAnalyzer;
import cpen221.mp1.ratemyprofessor.Review;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Computes Sentiment Analyzer.
 *
 * @author TangMartin, badudum
 */
public class SentimentAnalyzer {

    private Set<String> reviewText;
    private final Map<String, Float> probability;
    private final DataAnalyzer data;
    private final HashMap<Float, Float[]> ratings;

    /**
     * Initializes a sentiment analyzer object with a given filename.
     *
     * @param filename name of the file to be analyzed;
     *                 file exists and is not empty
     */
    public SentimentAnalyzer(String filename) throws FileNotFoundException {
        this.ratings = new HashMap<>();
        float totalRatings = 0.0f;
        try {
            this.data = new DataAnalyzer(filename);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        }

        Map<String, Long> master = new HashMap<>();
        float total = 0f;
        this.probability = new HashMap<>();
        for (Review reviews: this.data.getReviewList()) {
            for (String s : reviews.getReview().
                    getAllNGrams().get(0).keySet()) {
                if (master.containsKey(s)) {
                    master.replace(s, master.get(s) + 1);
                } else {
                    master.put(s, 0L);
                }
                total += 1f;
            }
            if (ratings.containsKey(reviews.getScore())) {
                ratings.get(reviews.getScore())[0] += 1;
            } else {
                ratings.put(reviews.getScore(), new Float[2]);
                ratings.get(reviews.getScore())[0] = 1.0f;
            }
            totalRatings++;

        }

        for (Float key : ratings.keySet()) {
            float p = ratings.get(key)[0] / totalRatings;
            ratings.get(key)[1] = p;
        }

        for (String y: master.keySet()) {
            this.probability.put(y, (master.get(y) / total));
        }

    }

    /**
     * Given the rating, get the probability of the review text occurring at the
     * specified rating.
     *
     * @param rating A floating point value that represents the rating at which
     *               we want the probability of the bag of words occurring
     * @return The probability of the bag of words occurring at the
     *         specified rating, as a floating point value
     */
    public float bagRating(float rating) {
        Map<String, Long> myReviewText = new HashMap<>();
        float occurrences = 1.0f;
        Long totalWords = 0L;

        for (Review reviews : data.getReviewList()) {
            if (reviews.getScore() == rating) {
                for (String key
                        : reviews.getReview().getAllNGrams().get(0).keySet()) {
                    totalWords +=
                            reviews.getReview().getAllNGrams().get(0).get(key);
                    if (this.reviewText.contains(key)) {
                        if (myReviewText.containsKey(key)) {
                            myReviewText.replace(key, myReviewText.get(key)
                                    + reviews.getReview().
                                    getAllNGrams().get(0).get(key));
                        } else {
                            myReviewText.put(key, reviews.getReview().
                                    getAllNGrams().get(0).get(key));
                        }
                    }
                }
            }
        }

        for (String review : myReviewText.keySet()) {
            myReviewText.replace(review, myReviewText.get(review) + 1L);
        }

        for (Long x : myReviewText.values()) {
            occurrences *= (float) x;
        }
        return (float) ((double) occurrences
                /  Math.pow((double) totalWords + 1d, this.reviewText.size()));
    }

    /**
     * Given the text of a review, returns the rating predicted by the simple
     * Bayesian approach based on the data from the given file of reviews.
     *
     * @param text A string representing a review whose score will be predicted;
     *             is not empty
     * @return A floating point value representing the predicted score of the
     *         given review
     */
    public float getPredictedRating(String text) {
        Map<Float, Float> ratingBag = new HashMap<>();
        this.reviewText = Set.of(text.split(" "));
        float pWord = 1f;
        for (String word : this.reviewText) {
            pWord = pWord * this.probability.get(word);
        }
        for (float i = 1.0f; i <= 5; i += 0.5f) {
            float pBagRating = this.bagRating(i);
            float pRating = this.ratings.get(i)[1];
            ratingBag.put((pBagRating * pRating) / pWord, i);
        }
        return ratingBag.get(Collections.max(ratingBag.keySet()));
    }

}

