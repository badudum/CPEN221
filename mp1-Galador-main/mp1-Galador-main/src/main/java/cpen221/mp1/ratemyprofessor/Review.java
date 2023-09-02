package cpen221.mp1.ratemyprofessor;

import cpen221.mp1.ngrams.NGrams;

import java.util.Arrays;

/**
 * Represents a review.
 *
 * @author W1nst0n03
 */
public class Review {

    private final float score;
    private final char gender;
    private final NGrams review;
    private final String[] reviewText;

    /**
     * Initializes a review with the given score, gender, and review string.
     *
     * @param score a floating point value representing the score of the review
     * @param gender a char representing the gender of the subject of the
     *               review;
     *               'M' for man and 'W' for woman
     * @param review a string of the review text
     */
    public Review(float score, char gender, String review) {
        this.score = score;
        this.gender = gender;
        this.reviewText = review.split("\\.");
        this.review = new NGrams(reviewText, 1);
    }

    /**
     * Returns the n-grams object of the review.
     *
     * @return the n-grams object of the review
     */
    public NGrams getReview() {
        return this.review;
    }

    /**
     * Returns original review text string.
     *
     * @return the review text string
     */
    public String[] getReviewText() {
        return  this.reviewText;
    }

    /**
     * Returns the score of the review.
     *
     * @return a float of the score
     */
    public float getScore() {
        return this.score;
    }

    /**
     * Returns the gender of the review.
     *
     * @return a character representing the gender of the review;
     * 'M' for man and 'W' for woman
     */
    public char getGender()  {
        return gender;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Review otherR) {
            return this.score == otherR.score
                    && this.gender == otherR.gender
                    && Arrays.equals(this.reviewText, otherR.reviewText);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) score * (int) gender;
    }

}
