import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Word counter project: A program that counts word occurrences in a user given
 * input file and outputs an HTML file with a table of the words and counts
 * listed in alphabetical order.
 *
 * @author Zheyuan Gao
 */
public final class WordCounter {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private WordCounter() {
        // no code needed here
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    public static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char c = text.charAt(position);
        boolean isSeparator = separators.contains(c);

        StringBuilder subString = new StringBuilder();
        int endIndex = position;
        boolean end = false;
        /*
         * if the character is not a separator, then return the substring start
         * from position to the appearance of the next separator
         */
        if (!isSeparator) {
            while (!end && endIndex < text.length()) {

                if (!separators.contains(text.charAt(endIndex))) {
                    subString.append(text.charAt(endIndex));
                } else {
                    end = true;
                }
                endIndex++;

            }

        } else {
            /*
             * if the character is a separator, then return the substring start
             * at position and end at the first appearance of the character that
             * not in the separator set.
             */
            while (!end && endIndex < text.length()) {

                if (separators.contains(text.charAt(endIndex))) {
                    subString.append(text.charAt(endIndex));
                } else {
                    end = true;
                }
                endIndex++;
            }

        }
        return subString.toString();
    }

    /**
     * Override the comparator to compare the alphabet of two strings.
     *
     * @ensure return negative number if str2's first character is in front of
     *         the str1's first character in alphabet order. 0 if the are same
     *         character. positive number if str1's first character is in front
     *         of str2's first character.
     */
    public static class StringLT implements Comparator<String> {

        @Override
        public final int compare(String str1, String str2) {
            return str1.compareToIgnoreCase(str2);
        }

    }

    /**
     * Read the input file and store all the words and the time of appearance in
     * a map. After that, store all the nonredundant words in a queue and sort
     * them.
     *
     * @param input
     *            Simple Reader to read the user input file
     * @param wordCount
     *            Map to store the words and the number of appearance in the
     *            give file
     * @param word
     *            Queue to store the nonredundant words
     * @param separator
     *            set that contains all the separator characters
     * @require [Input is open. The user file exists]
     * @ensure [Words in queue word will list in alphabetical order. The map
     *         will contains the words in the file and the corresponding times
     *         it appears]
     */
    public static void readFileConvertToData(SimpleReader input,
            Map<String, Integer> wordCount, Queue<String> word,
            Set<Character> separator) {
        while (!input.atEOS()) {
            String nextLine = input.nextLine();
            /*
             * Introduce the integer value startPosition to keep track the
             * process of recording the words in the sentence to the map
             */
            int startPosition = 0;
            /*
             * We process can only proceed when the start position is in the
             * scope of the given sentence
             */
            while (startPosition < nextLine.length()) {
                String token = nextWordOrSeparator(nextLine, startPosition,
                        separator);
                /*
                 * If the token is a word
                 */
                if (!separator.contains(token.charAt(0))) {
                    /*
                     * if the map does not have this word yet, add it to the map
                     * and the queue.
                     */
                    if (!wordCount.hasKey(token)) {
                        wordCount.add(token, 1);
                        word.enqueue(token);
                    } else {
                        /*
                         * if the word has already exist, add one to its
                         * corresponding value in map
                         */
                        wordCount.replaceValue(token,
                                wordCount.value(token) + 1);
                    }
                }
                /*
                 * Update the start position and contain to check next token
                 */
                startPosition += token.length();
            }
        }
        /*
         * After all the words are in the queue, sort them with alphabetical
         * order
         */
        Comparator<String> compare = new StringLT();
        word.sort(compare);
    }

    /**
     * Generate the corresponding HTML file to the user choice location.
     *
     * @param output
     *            The simple writer to output content to the user choice
     *            location
     * @param file
     *            The location of the user given text
     * @param wordCount
     *            The map to store the word and corresponding numbers of
     *            appearance in the content
     * @param words
     *            The queue that contains all the words
     * @require [The words in queue is nonredundant and in alphabetical order.
     *          The output is open and the folder user choose exists]
     * @ensure [Output the file to the user choice folder. The file contains the
     *         word count table in HTML format]
     */
    public static void outputHTML(SimpleWriter output, String file,
            Map<String, Integer> wordCount, Queue<String> words) {
        /*
         * output the header of the file
         */
        output.println("<html>");
        output.println(" <head>");
        output.println("  <title>Words Counted in " + file + "</title>");
        output.println(" </head>");
        output.println(" <body>");
        output.println("  <h2>Words Counted in " + file + "</h2>");
        output.println("  <hr />");
        /*
         * output the body of the file
         */
        output.println("  <table border=\"1\">");
        output.println("   <tr>");
        output.println("    <th>Words</th>");
        output.println("    <th>Counts</th>");
        output.println("   </tr>");
        /*
         * Start to print out the words and there corresponding number
         */
        for (int i = 0; i < words.length(); i++) {
            String word = words.dequeue();
            words.enqueue(word);
            output.println("   <tr>");
            output.println("    <td>" + word + "</td>");
            output.println("    <td>" + wordCount.value(word) + "</td>");
            output.println("   </tr>");
        }
        /*
         * output the footer of the file
         */
        output.println("  </table>");
        output.println(" </body>");
        output.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        /*
         * Generate a set of separators
         */
        Set<Character> separatorSet = new Set1L<>();
        separatorSet.add(',');
        separatorSet.add(' ');
        separatorSet.add('.');
        separatorSet.add('-');
        separatorSet.add('?');
        separatorSet.add('!');
        /*
         * Open input and output streams
         */
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Ask user for file location and output location
         */
        out.println();
        out.print("Please input the name of the text file: ");
        String file = in.nextLine();
        out.print("Please enter the folder you want to store the html files: ");
        String folder = in.nextLine();
        SimpleReader input = new SimpleReader1L(file);
        SimpleWriter output = new SimpleWriter1L(folder);
        /*
         * Store the text data in the map and queue
         */
        Map<String, Integer> wordCount = new Map1L<>();
        Queue<String> words = new Queue1L<>();
        readFileConvertToData(input, wordCount, words, separatorSet);
        /*
         * Generate the HTML file to user choose location
         */
        outputHTML(output, file, wordCount, words);
        /*
         * Close the input and output streams
         */
        in.close();
        out.close();
        input.close();
        output.close();
    }

}
