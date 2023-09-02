package cpen221.mp1.autocompletion;

import cpen221.mp1.ratemyprofessor.DataAnalyzer;
import cpen221.mp1.searchterm.SearchTerm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class Task3Tests {

    private static final String citiesData = "data/cities.txt";
    private static AutoCompletor ac;
    private static AutoCompletor acLine1;

    @BeforeAll
    public static void setupTests() throws FileNotFoundException {
        cpen221.mp1.cities.DataAnalyzer cityAnalyzer = new cpen221.mp1.cities.DataAnalyzer(citiesData);
        ac = new AutoCompletor(cityAnalyzer.getSearchTerms());
        DataAnalyzer reviewAnalyzer = new DataAnalyzer("data/ratemyprofessor_data.txt");
        acLine1 = new AutoCompletor(reviewAnalyzer.returnReviewGram(0).getAllNGrams());
    }

    @Test
    public void testCities() {
        SearchTerm[] st = ac.topKMatches("Saint Petersburg", 3);

        SearchTerm russia = new SearchTerm("Saint Petersburg, Russia", 4039745);
        SearchTerm usa = new SearchTerm("Saint Petersburg, Florida, United States", 244769);
        SearchTerm[] expectedST = new SearchTerm[] {russia, usa };

        Assertions.assertArrayEquals(expectedST, st);
    }
    @Test
    public void testAllMatches() {
        SearchTerm[] st = acLine1.allMatches("jans is great she is a fantastic teacher and her class was both interesting and lively she does expect you to read a lot out of the book and other sources as well she is a very lively teacher and you can tell she loves the");

        SearchTerm x1 = new SearchTerm("jans is great she is a fantastic teacher and her class was both interesting and lively she does expect you to read a lot out of the book and other sources as well she is a very lively teacher and you can tell she loves the", 1);
        SearchTerm x2 = new SearchTerm("jans is great she is a fantastic teacher and her class was both interesting and lively she does expect you to read a lot out of the book and other sources as well she is a very lively teacher and you can tell she loves the subject", 1);
        SearchTerm x3 = new SearchTerm("jans is great she is a fantastic teacher and her class was both interesting and lively she does expect you to read a lot out of the book and other sources as well she is a very lively teacher and you can tell she loves the subject she", 1);
        SearchTerm x4 = new SearchTerm("jans is great she is a fantastic teacher and her class was both interesting and lively she does expect you to read a lot out of the book and other sources as well she is a very lively teacher and you can tell she loves the subject she teaches", 1);
        SearchTerm[] expectedST = new SearchTerm[] {x1, x2, x3, x4, };

        Assertions.assertArrayEquals(expectedST, st);
    }
    @Test
    public void testDefaultTopKMatches() {
        SearchTerm[] st = acLine1.topKMatches("sh");

        SearchTerm she = new SearchTerm("she", 5);
        SearchTerm she_is = new SearchTerm("she is", 2);
        SearchTerm she_is_a = new SearchTerm("she is a", 2);
        SearchTerm she_does = new SearchTerm("she does", 1);
        SearchTerm she_does_expect = new SearchTerm("she does expect", 1);
        SearchTerm x1 = new SearchTerm("she does expect you", 1);
        SearchTerm x2 = new SearchTerm("she does expect you to", 1);
        SearchTerm x3 = new SearchTerm("she does expect you to read", 1);
        SearchTerm x4 = new SearchTerm("she does expect you to read a", 1);
        SearchTerm x5 = new SearchTerm("she does expect you to read a lot", 1);
        SearchTerm[] expectedST = new SearchTerm[] {she, she_is, she_is_a, she_does, she_does_expect, x1, x2, x3, x4, x5 };

        Assertions.assertArrayEquals(expectedST, st);
    }
    @Test
    public void testNumberOfMatches() {
        int AssertedValue1 = 40;
        int AssertedValue2 = 119;
        Assertions.assertEquals(AssertedValue1, acLine1.numberOfMatches("h"));
        Assertions.assertEquals(AssertedValue2, acLine1.numberOfMatches("s"));
    }

}
