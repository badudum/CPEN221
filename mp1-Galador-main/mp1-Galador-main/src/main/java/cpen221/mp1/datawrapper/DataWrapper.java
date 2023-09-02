package cpen221.mp1.datawrapper;

import cpen221.mp1.autocompletion.gui.In;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class DataWrapper {

    private final Scanner dataReader;

    /**
     * Constructor for DataWrapper, opens a file and creates a scanner
     * to go through the data.
     * The scanner is stored in the class variable dataReader
     *
     * @param fileName the location of the file to be read
     */
    public DataWrapper(String fileName) throws FileNotFoundException {
        File dataFile;
        dataFile = new File(fileName);
        dataReader = new Scanner(dataFile);
    }

    /**
     * Returns the next line of the file and moves the pointer to the next line.
     *
     * @return a string that is the next line of the data or if there is no next
     *         line returns null
     */
    public String nextLine() {
        if (dataReader.hasNextLine()) {
            return dataReader.nextLine();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the file has a next line and false otherwise.
     *
     * @return a boolean dependent on the existence of a next line
     */
    public boolean hasNextLine() {
        return dataReader.hasNextLine();
    }

    /**
     * Resets the file scanner to start at the beginning of the file.
     */
    public void resetScanner() {
        dataReader.reset();
    }

}
