package cpen221.mp1.ratemyprofessor;

import cpen221.mp1.ngrams.NGrams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Task2Tests {

    private static DataAnalyzer da1;
    private static DataAnalyzer da2;
    private static DataAnalyzer da3;

    @BeforeAll
    public static void setUpTests() throws FileNotFoundException {
        da1 = new DataAnalyzer("data/reviews1.txt");
        da2 = new DataAnalyzer("data/reviews2.txt");
        da3 = new DataAnalyzer("data/reviews3.txt");
    }

    @Test
    public void testGoodOne() {
        String query = "good";
        Map<String, Long> expected = Map.of(
                "ML", 0L,
                "WL", 1L,
                "MM", 0L,
                "WM", 0L,
                "MH", 1L,
                "WH", 1L
        );
        assertEquals(expected, da1.getHistogram(query));
    }

    @Test
    public void testHeIsTwo() {
        String query = "he is";
        Map<String, Long> expected = Map.of(
                "ML", 1L,
                "WL", 0L,
                "MM", 3L,
                "WM", 0L,
                "MH", 3L,
                "WH", 0L
        );
        assertEquals(expected, da2.getHistogram(query));
    }

    @Test
    public void testHeIsFemaleTwo() {
        String query = "he is";
        Map<String, Long> expected = Map.of(
                "ML", 0L,
                "WL", 1L,
                "MM", 0L,
                "WM", 3L,
                "MH", 0L,
                "WH", 3L
        );
        assertEquals(expected, da3.getHistogram(query));
    }
    @Test
    public void testReturnReviews() {
        String s1 = "she is not a good teacher";
        String s2 = "good textbook written by him";
        String s3 = "good teacher she really cares";

        NGrams s1NGrams = new NGrams(new String[]{s1 });
        ArrayList<Review> sampleReviewList = new ArrayList<>();
        sampleReviewList.add(new Review(1.0f, 'W', s1));
        sampleReviewList.add(new Review(5.0f, 'M', s2));
        sampleReviewList.add(new Review(4.5f, 'W', s3));

        assertEquals(s1NGrams.getAllNGrams(), da1.returnReviewGram(0).getAllNGrams());
        Assertions.assertArrayEquals(sampleReviewList.toArray(new Review[0]),
                da1.getReviewList().toArray(new Review[0]));
    }

    @Test
    public void testReviewOverrides() {
        String s1 = "she is not a good teacher";
        String s2 = "good textbook written by him";
        Review test1 = new Review(1.0f, 'W', s1);
        Review test4 = new Review(1.5f, 'W', s1);
        Review test5 = new Review(1.0f, 'M', s1);
        Review test6 = new Review(1.0f, 'W', s2);

        Assertions.assertNotEquals(test1, s2);
        Assertions.assertNotEquals(test1, test4);
        Assertions.assertNotEquals(test1, test5);
        Assertions.assertNotEquals(test1, test6);
        assertEquals(test1.hashCode(), 'W');
    }
}
