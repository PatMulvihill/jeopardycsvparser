
/**
 *
 * @author Patrick Mulvihill
 */
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class CSVParseTest {

    public static void main(String[] args) {
        String CSVString = "6240,2011-11-04,Jeopardy!,\"RHYME-BOT\",\"$1000\",\"The yellow-bellied type of this critter is sunbathing <a href=\"\"http://www.j-archive.com/media/2011-11-04_J_12.jpg\"\" target=\"\"_blank\"\">here</a>\",\"a marmot\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"MAPMAKER, MAPMAKER\",\"$450\",\"Get out the blue ink for this sea immediately north of Poland\",\"the Baltic\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"MTV VIDEO OF THE YEAR ARTISTS\",\"$400\",\"1988: \"\"Need You Tonight/Meditate\"\"\",\"INXS\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"BALLET\",\"$450\",\"Maria Tallchief played the sugarplum fairy in a 1954 New York City ballet performance of this classic\",\"The Nutcracker\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"CALCULATING THE MEASUREMENTS\",\"$400\",\"16 short tons minus 13 short tons leaves you this many pounds\",\"6,000 (3 tons, 2,000 per)\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"CANADIAN HISTORY\",\"$300\",\"In 1949 Canada joined the United States & 10 other nations to form this military alliance\",\"NATO\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"GIMME AN \"\"H\"\"\",\"$400\",\"Proverbially, \"\"he who\"\" does this \"\"is lost\"\"\",\"hesitates\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"MAPMAKER, MAPMAKER\",\"$2000\",\"However you draw the border between Nicaragua & this 2-named country, somebody gets mad, huh?\",\"Costa Rica\"\n"
                + "6240,2011-11-04,Double Jeopardy!,\"MTV VIDEO OF THE YEAR ARTISTS\",\"$870\",\"2009: \"\"Single Ladies (Put A Ring On It)\"\"\",\"Beyonce\"\n";
        // Convert the CSVString (of type Text) to a string
//        String CSVStr = value.toString();
        // Split the string at each comma, creating an ArrayList with the different attributes in each index.
        // Sometimes the questions will be split into multiple elements because they contain commas, but for the
        // way we will be parsing the CSV's, it doesn't matter.
        List<String> items = Arrays.asList(CSVString.split("\\s*,\\s*"));
        // Loop through all the elements in the CSV
        // Start i at 10 to ensure that you do not parse a point value that doesn't have a year not contained in the data set.
        // We can end the loop at items.size() w/o truncating the last 10 items because if we have a point value, we know
        // that the corresponding year is in the items before it, not after it.
        // We will miss some data points because of this, but it shouldn't matter too much because of the magnitude of our data set.
        for (int i = 10; i < items.size(); i++) {
            // If the second character in an item is a '$', as in "$1600", we know that this item contains a point value
            // Make sure that the item has more than 1 index to ensure avoidnace of index out of bound error when trying to access the potential "$"
            if (items.get(i).length() > 1 && items.get(i).toCharArray()[1] == '$') {
                // We want to save this item as a point value character array so that we can access it's indices
                char[] pointValueChars = items.get(i).toCharArray();
                // The variable lengthSubCount keeps track of how many fewer characters the string containing just digits will have in
                // comparison to the full item string.  For instance, "$1600" (the full item string) has 3 fewer characters than 1600 (the integer
                // we will ultimately want to pass to the mapper. A double quote ("), dollar sign ($), and a comma(,) present in the full item string
                // all increment this variable.
                int lengthSubCount = 0;
                for (int j = 0; j < pointValueChars.length; j++) {
                    if (pointValueChars[j] == '$' || pointValueChars[j] == ',' || pointValueChars[j] == '\"') {
                        lengthSubCount++;
                    }
                }
                // Create a new array of the size needed to contain only the digits contained in the full item string
                // We will eventually be parsing this string to an integer, so we cannot have empty indices. The size must be
                // exactly as large as the number of digits.  ("$1600" -> 1600; we need 4 digits from the original 7).
                char[] newPointValueCharArray = new char[pointValueChars.length - lengthSubCount];
                // indexSkippedCount keeps track of how many of the elements have been skipped from being added to the array that we will convert to an int.
                // Increment this when a $, ", or , is encountered in the original full text string we are pulling the digits from.
                // Use indexSkippedCount to ensure that even if the 1 in "$1600" is stored at character index 2, we can store at as the first digit (index 0)
                // by subtracting indexSkippedCount from j (the iterator in the for loop).
                int indexSkippedCount = 0;
                for (int j = 0; j < pointValueChars.length; j++) {
                    if (pointValueChars[j] == '$' || pointValueChars[j] == ',' || pointValueChars[j] == '\"') {
                        indexSkippedCount++;
                    } else {
                        newPointValueCharArray[j - indexSkippedCount] = pointValueChars[j];
                    }
                }
                // Convert the point value array that contains only the digits from the item to an integer.
                String wagerStr = new String(newPointValueCharArray);
                int wager = Integer.valueOf(wagerStr);
                // We are only interested in Daily Double wagers.  Daily Doubles are the only point values in the data set that aren't necessarily the scores
                // in the conditional (the numbers on the baord from Jeopardy and Double Jeopardy rounds).
                if (wager != 200 && wager != 400 && wager != 600 && wager != 800 && wager != 1000 && wager != 1200 && wager != 1600 && wager != 2000) {
                    // If it is determined that the point value for a question is in fact a Daily Double, the air date must be found.
                    boolean airDateFound = false;
                    int count = 1;
                    // We initialize airDateStr to something that indicates that the regular expression is failing.
                    String airDateStr = "SOMETHING WENT WRONG. The regular expression for finding dates has failed you.";
                    // Go through the items that occurred before the point value item.  If the item matches the data format in the regular expression conditional, then
                    // that item is a data and can be saved as such.  This loop cannot cause an index out of bounds error because the parent for loop starts at 10 instead of 0.
                    // Once the airDate has been found, exit the loop and extract the year.
                    while (airDateFound != true) {
                        airDateStr = items.get(i - count);
                        if (airDateStr.matches("^\\d{4}\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$")) {
                            airDateFound = true;
                        } else {
                            count++;
                        }
                    }
                    // Extract the year from the airDate (the year will always be the first 4 characters.  We can keep the year as a string because it is going to be passed as a key.
                    char[] airDateChars = airDateStr.toCharArray();
                    String yearStr = "" + airDateChars[0] + airDateChars[1] + airDateChars[2] + airDateChars[3];
                    System.out.println(yearStr + ", " + wager);

                    // output the follow key-value pair: <year, wager>
                    // output.collect(new Text(year), new IntWritable(wager));
                }
            }
        }
    }
}
