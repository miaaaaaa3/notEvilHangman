/*  Student information for assignment:
 *
 *  On my honor, Mia Tey, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Mia Tey
 *  email address: mia_tey@aol.com
 *  UTEID: mat5693
 *  Section 5 digit ID: 51175
 *  Grader name: Pranav
 *  Number of slip days used on this assignment: 0
 */

// add imports as necessary

import java.util.Set;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Map;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {

    // instance variables / fields
    private boolean debugOn;
    private ArrayList<String> dictionary;
    private ArrayList<String> activeWords;
    private ArrayList<Character> guesses;
    private ArrayList<PatternFamily> familiesSorted;
    private int numGuessed;
    private int maxGuesses;
    private String currPattern;
    private final char UNKNOWN = '-';
    private int roundCounter;
    private final int MEDIUM_MOD = 4;
    private final int EASY_MOD = 2;
    private int difficulty;
    private final int EASY = 1;
    private final int MED = 2;
    private final int HARD = 3;

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        if (words == null || words.size() <= 0) {
            throw new IllegalArgumentException("Violation of precondition:" +
                " Set of Strings may not be null or empty.");
        }
        this.debugOn = debugOn;
        dictionary = new ArrayList<>();
        for (String word : words) { // copy dictionary
            dictionary.add(word);
        }
        Collections.sort(dictionary);
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        this(words, false);
    }


    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     * with the given length
     */
    public int numWords(int length) {
        int count = 0;
        for (String word : dictionary) {
            if (word.length() == length) {
                count++;
            }
        }
        return count;
    }


    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     * @param wordLen the length of the word to pick this time.
     * numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     * player loses the round. numGuesses >= 1
     * @param diff The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        if (numWords(wordLen) <= 0 || numGuesses < 1) {
            throw new IllegalArgumentException("Violation of precondition:" +
                " wordLen must be greater than 0 and numGuesses at least 1.");
        }
        setUpActiveWords(wordLen);
        newPattern(wordLen);       
        guesses = new ArrayList<>();
        maxGuesses = numGuesses;
        numGuessed = 0;
        roundCounter = 0; // to track difficulty
        if (diff == HangmanDifficulty.EASY) {
            difficulty = EASY;
        } else if (diff == HangmanDifficulty.MEDIUM) {
            difficulty = MED;
        } else {
            difficulty = HARD;
        }
    }

    // Set up the list of active words based on specified string length. 
    private void setUpActiveWords(int wordLen) {
        activeWords = new ArrayList<>();
        for (String word : dictionary) {
            if (word.length() == wordLen) {
                activeWords.add(word); 
            }
        }
    }

    // Make pattern of unguessed letters based on specified string length.
    private void newPattern(int wordLen) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordLen; i++) {
            sb.append(UNKNOWN);
        }
        currPattern = sb.toString();
    }


    /**
     * The number of words still possible (live) based on the guesses so far.
     *  Guesses will eliminate possible words.
     * @return the number of words that are still possibilities based on the
     * original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return activeWords.size();
    }


    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     * @return the number of wrong guesses the user has left
     * in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return maxGuesses - numGuessed;
    }


    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     * @return a String that contains the letters the user
     * has guessed so far during this round.
     */
    public String getGuessesMade() {
        Collections.sort(guesses);
        return guesses.toString();
    }


    /**
     * Check the status of a character.
     * @param guess The characater to check.
     * @return true if guess has been used or guessed this round of Hangman,
     * false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        return guesses.contains(guess);
    }


    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed) characters and the actual character 
     * for "correctly guessed" characters.
     * @return the current pattern.
     */
    public String getPattern() {
        return currPattern;
    }


    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the give guess.
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        if (alreadyGuessed(guess)) {
            throw new IllegalStateException("Violation of precondition:" +
                " Character already guessed.");
        }
        guesses.add(guess);
        roundCounter ++;

        // Map to organize word pattern families and corresponding words
        Map<String, ArrayList<String>> wordFamilies = createFamilies(guess);

        // Map holds word patterns and number of words that match that pattern
        TreeMap<String, Integer> freqMap = new TreeMap<>();
        for (String family : wordFamilies.keySet()) {
            freqMap.put(family, wordFamilies.get(family).size());
        }
        sortDifficulty(freqMap); // Create arrayList to sort families by difficulty
        String prevPat = currPattern; // store to compare to check for wrong guess
        currPattern = difficultyString(); // choose pattern based on difficulty
        updateGuessCount(prevPat);
        activeWords = wordFamilies.get(currPattern);
        return freqMap;
    }

    // Using the words in the active list, create and return a map of word 
    // patterns as keys and arrayLists of names as the values.
    private Map<String, ArrayList<String>> createFamilies(char guess) {
        HashMap<String, ArrayList<String>> wordFamilies = new HashMap<>();
        
        // For each word in active list, build possible patterns and 
        // create arrayLists of words that match that pattern.
        for (String currWord : activeWords) {
            String key = constructPattern(guess, currWord);
            if (!wordFamilies.containsKey(key)) {
                ArrayList<String> newFamily = new ArrayList<>();
                newFamily.add(currWord);
                wordFamilies.put(key, newFamily);
            } else {
                ArrayList<String> family = wordFamilies.get(key);
                family.add(currWord);
            }
        }
        return wordFamilies;
    }

    // Build new family pattern options using user guess and current pattern
    private String constructPattern(char guess, String currWord) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currWord.length(); i++) {
            if (currWord.charAt(i) == guess) {
                sb.append(guess);
            } else {
                sb.append(currPattern.charAt(i));
            }
        }
        return sb.toString();   
    }

    // Create arrayList of PatternFamily objects and sort by difficulty
    private void sortDifficulty(TreeMap<String, Integer> freqMap) {
        familiesSorted = new ArrayList<>();
        for (String family : freqMap.keySet()) {
            familiesSorted.add(new PatternFamily(family, freqMap.get(family)));
        }
        Collections.sort(familiesSorted);
    }

    // Determine which pattern to use based on difficulty level and prev patterns' difficulty
    private String difficultyString() {
        int index = 0;
        if (difficulty == EASY) {
            if ((roundCounter % EASY_MOD == 0) && (familiesSorted.size() > 1)) {
                index = 1;
            }
        } else if (difficulty == MED) {
            if ((roundCounter % MEDIUM_MOD == 0) && (familiesSorted.size() > 1)) {
                index = 1;
            }
        } else index = 0;
        return familiesSorted.get(index).getPattern();
    }

    // Update guess count if letter guessed incorrectly 
    private void updateGuessCount(String prevPattern) {
        if (prevPattern.equals(currPattern)) {
            numGuessed ++;
        }
    }
 
    /**
     * This class creates PatternFamily objects so that they are sortable
     * by difficulty level. It takes in String and int values created from 
     * freqMap uses a CompareTo method to sort patterns.
     */
    private static class PatternFamily implements Comparable<PatternFamily> {
        private String pattern;
        private int matches;
        private int lettersRevealed;
        private final char UNKNOWN = '-';

        // Create new PatternFamily object using string pattern and number 
        // of words with that pattern
        // pre: pattern != null && matches >= 0
        public PatternFamily(String pattern, int matches) {
            if (pattern == null || matches < 0) {
                throw new IllegalArgumentException("Violation of precondition:" +
                " String pattern cannot be null and int matches must be greater than 0.");
            }
            this.pattern = pattern;
            this.matches = matches;
            lettersRevealed = lettersRev(pattern);
        }

        // Get number of letters revealed for this PatternFamily object
        private int lettersRev(String pattern) {
            int count = 0;
            for (int i = 0; i < pattern.length(); i ++) {
                if (pattern.charAt(i) != UNKNOWN) {
                    count++;
                }
            }
            return count;
        }

        // Returns String pattern for this object
        public String getPattern() {
            return pattern;
        }

        // Compares PatternFamily objects on difficulty. 
        // If a neg int is returned, it means this PatternFamily is more 
        // difficult than other. First compares matches. The pattern with
        // the most matches is more difficult. If tie, compare letters revealed. 
        // The less letters revealed, the more difficult.If tie, compare lexicographically.
        public int compareTo(PatternFamily other) {
            int result = other.matches - this.matches;
            if (result == 0) { // Tie on matches. Compare letters revealed. 
                result = this.lettersRevealed - other.lettersRevealed;
            }
            if (result == 0) { // Tie on letters revealed. Compare lexicographically.
                result = pattern.compareTo(other.pattern);
            }
            return result;
        }
    }


    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (numWordsCurrent() <= 0) {
            throw new IllegalStateException("Violation of precondition:" + 
                " numWordsCurrent must be greater than 0");
        }
        if (activeWords.size() == 0) {
            return activeWords.get(0);
        } else {
            // Pick random word from activeWords
            Random r = new Random();
            int index = r.nextInt(activeWords.size());
            return activeWords.get(index);
        }
    }
}
