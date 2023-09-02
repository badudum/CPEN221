package cpen221.mp1.cities;

import cpen221.mp1.autocompletion.gui.AutoCompletorGUI;
import cpen221.mp1.autocompletion.gui.In;
import cpen221.mp1.searchterm.SearchTerm;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DataAnalyzer {

    private static final String CITIES_DATA = "data/cities.txt";
    private SearchTerm[] searchTerms;

    public DataAnalyzer(String filename) {
        In input = new In(filename);
        List<SearchTerm> stList = new ArrayList<>();
        for (String line = input.readLine();
             line != null; line = input.readLine()) {
            String[] lineComponents = line.split("\t", 2);
            SearchTerm st = new SearchTerm(lineComponents[1],
                    Integer.parseInt(lineComponents[0].trim()));
            stList.add(st);
        }

        int numTerms = stList.size();
        searchTerms = new SearchTerm[numTerms];
        searchTerms = stList.toArray(searchTerms);

    }

    public SearchTerm[] getSearchTerms() {
        return searchTerms;
    }

    public static void main(String[] args) {
        DataAnalyzer da = new DataAnalyzer(CITIES_DATA);
        SearchTerm[] searchTerms = da.searchTerms;
        final int k = 10;
        SwingUtilities.invokeLater(
                () -> new AutoCompletorGUI(searchTerms, k).setVisible(true)
        );
    }

}
