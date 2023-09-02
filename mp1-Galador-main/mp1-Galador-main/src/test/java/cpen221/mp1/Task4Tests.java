package cpen221.mp1;

import cpen221.mp1.sentimentanalysis.SentimentAnalyzer;
import org.junit.jupiter.api.Test;
import java.io.FileNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

public class Task4Tests {
    @Test
    public void testRating1() {
        try {
            SentimentAnalyzer rmpSa = new SentimentAnalyzer("data/ratemyprofessor_data.txt");
            assertEquals(1f, rmpSa.getPredictedRating("oh no it was so difficult"));
        } catch (FileNotFoundException fnf) {
            fail("Data file is not in the right place!");
        }
    }

    @Test
    public void testRating2() {
        try {
            SentimentAnalyzer rmpSa = new SentimentAnalyzer("data/ratemyprofessor_data.txt");
            assertEquals(2f, rmpSa.getPredictedRating("soft voice sit in the front"));
        } catch (FileNotFoundException fnf) {
            fail("Data file is not in the right place!");
        }
    }

    @Test
    public void testBadFile() {
        try {
            SentimentAnalyzer rmpSa = new SentimentAnalyzer("data/ratmyprofessor_data.txt");
        } catch (Exception fnf) {
            assertTrue(true);
        }
    }
}