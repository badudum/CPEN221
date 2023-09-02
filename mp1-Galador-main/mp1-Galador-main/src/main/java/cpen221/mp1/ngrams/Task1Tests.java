package cpen221.mp1.ngrams;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Task1Tests {

    @Test
    public void simpleTestCount() {
        String text1 = "the blue cow jumped over the blue cow moon!";
        String text2 = "The Blue Period of Picasso is the period between 1900 and 1904, when he painted essentially monochromatic paintings in shades of blue and blue-green, only occasionally warmed by other colors.";

        long expectedCount = 130;

        NGrams ng = new NGrams(new String[]{text1, text2});

        assertEquals(expectedCount, ng.getTotalNGramCount(4));
    }

    @Test
    public void simpleTestGetNGrams() {
        String text1 = "great class";
        String text2 = "good textbook written by him";

        List<Map<String, Long>> expectedNGrams = List.of(
                Map.of("great", 1L, "class", 1L, "good", 1L, "textbook", 1L, "written", 1L, "by", 1L, "him", 1L),
                Map.of("great class", 1L, "good textbook", 1L, "textbook written", 1L, "written by", 1L, "by him", 1L),
                Map.of("good textbook written", 1L, "textbook written by", 1L, "written by him", 1L),
                Map.of("good textbook written by", 1L, "textbook written by him", 1L),
                Map.of("good textbook written by him", 1L)
        );

        NGrams ng = new NGrams(new String[]{text1, text2});
        assertEquals(expectedNGrams, ng.getAllNGrams());
    }

    @Test
    public void manyIndividualWordsAndBlankString() {
        String text1 = "great";
        String text2 = "class";
        String text3 = "good";
        String text4 = "textbook";
        String text5 = "written";
        String text6 = "by";
        String text7 = "him";
        String text8 = "";
        String text9 = "supercalifragilisticexpialidocious";
        String text10 = "Thereover";
        List<Map<String, Long>> expectedNGrams = List.of(
                Map.of("great", 1L, "supercalifragilisticexpialidocious", 1L, "class", 1L, "good", 1L,
                        "textbook", 1L, "written", 1L, "by", 1L, "him", 1L, "thereover", 1L)
        );
        NGrams ng = new NGrams(new String[]{text1, text2, text3, text4, text5, text6, text7, text8, text9, text10});
        assertEquals(expectedNGrams, ng.getAllNGrams());
    }

    @Test
    public void getTotalNGramOutOfBounds() {
        String text1 = "the blue cow jumped over the blue cow moon!";
        String text2 = "The Blue Period of Picasso is the period between 1900 and 1904, when he painted essentially monochromatic paintings in shades of blue and blue-green, only occasionally warmed by other colors.";

        long expectedCount = 496;

        NGrams ng = new NGrams(new String[]{text1, text2});

        assertEquals(expectedCount, ng.getTotalNGramCount(999));
        assertEquals(expectedCount, ng.getTotalNGramCount(30));
    }

}
