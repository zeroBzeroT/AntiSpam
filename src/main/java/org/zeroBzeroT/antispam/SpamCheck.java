package org.zeroBzeroT.antispam;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class SpamCheck {
    // number of spam messages that is saved
    static final int maxBadSentencesSaved = 64;

    // minimum message length before it is beeing checked
    static int minMessageLength = 8;

    // factor of the message difference
    static double msgDiffFactor = 1d / 5d;

    // maximum duplicated messages to be saved
    static int maxDuplicates = 2;

    // initial number of saved non spam sentences
    static int maxSentencesSaved = 128;

    // increment for the maximum saved sentences per player
    static int perPlayerQueueSizeFactor = 5;

    // the last [maxSentencesSaved] chat messages for comparison
    final LimitedSizeQueue<String> lastMessages = new LimitedSizeQueue<>(maxSentencesSaved);

    // the last [maxBadSentencesSaved] spam chat messages for comparison
    final LimitedSizeQueue<String> lastSpamMessages = new LimitedSizeQueue<>(maxBadSentencesSaved);

    // sanitizing message from chars that are from unicode ranges that are only used a few times in that message
    private UnicodeRanges unicodeRanges;

    /**
     * constructor without a given unicode range specification
     * the default range specification is used from the resources
     */
    public SpamCheck(Plugin plugin) {
        try {
            unicodeRanges = new UnicodeRanges(plugin);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * constructor with a given unicode range specification
     *
     * @param unicodeRanges unicode range class
     */
    public SpamCheck(UnicodeRanges unicodeRanges) {
        this.unicodeRanges = unicodeRanges;
    }

    /**
     * distance between two sentences
     *
     * @param x first sentence
     * @param y second sentence
     * @return the minimum number of single-character edits
     */
    static int levenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                        dp[i - 1][j] + 1, dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    /**
     * cost of single char substitution
     *
     * @param a first character
     * @param b second character
     * @return 0 or 1
     */
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    /**
     * minimum of all given numbers
     *
     * @param numbers given numbers
     * @return minimum value of the numbers
     */
    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Checks message for spam
     *
     * @param message The message sent by the player
     * @return is the message spam
     */
    public boolean isRecurringSpam(String message) {
        // from [minMessageLength] character length
        if (message.length() < minMessageLength)
            return false;

        // use unicode ranges to sanitize text
        String saniMsg = unicodeRanges.sanitizeText(message, minMessageLength);

        // remove long random numbers
        saniMsg = saniMsg.replaceAll("\\b\\d{9,}\\b", "");

        // remove hashcodes that some use at the start or end of a spam text
        saniMsg = saniMsg.replaceAll("[^a-zA-Z0-9](?=([a-zA-Z]*\\d))\\S{4,}[^a-zA-Z0-9]", "");

        // remove camelcase that some use at the start or end of a spam text - useful?
        saniMsg = saniMsg.replaceAll("[^a-zA-Z0-9](?=([a-z]+[A-Z]+|[A-Z]+[a-z]+){2})\\S{3,}[^a-zA-Z0-9]", "");

        // remove non printable chars and spaces
        saniMsg = saniMsg.replaceAll("[\\p{C} ]", "");

        saniMsg = saniMsg.toLowerCase();

        if (saniMsg.length() <= 1) {
            System.out.println("(saniMsg.length() <= 1 for) '" + message + "'");
            // short message spam
            return true;
        }

        System.out.println("saniMsg '" + saniMsg + "'");

        int cntDuplicates = 0;

        // has the same already been written?
        for (String oldMsg : new LinkedList<>(lastMessages)) { // copy contents to new object to avoid concurrent modification by async chat event handling
            // difference in length of the messages is already greater than the factor
            if (Math.abs(oldMsg.length() - saniMsg.length()) > Math.max(oldMsg.length(), saniMsg.length()) * msgDiffFactor)
                continue;

            // Levenshtein distance - strings are similar
            if (levenshteinDistance(oldMsg, saniMsg) < saniMsg.length() * msgDiffFactor) {
                cntDuplicates++;

                if (cntDuplicates >= maxDuplicates)
                    break;
            }
        }

        // we dont need to add the message if its already in the list (really?
        // drawbacks?)
        //if (cntDuplicates < maxDuplicates) {
        lastMessages.add(saniMsg);
        //}

        if (cntDuplicates >= maxDuplicates) {
            // is Spam
            if (!lastSpamMessages.contains(saniMsg))
                lastSpamMessages.add(saniMsg);

            return true;
        } else {
            // Messages seems to be ok - so check the last spam messages
            for (String oldSpam : new LinkedList<>(lastSpamMessages)) {
                // difference in length of the messages is already greater than the factor
                if (Math.abs(oldSpam.length() - saniMsg.length()) > Math.max(oldSpam.length(), saniMsg.length()) * msgDiffFactor)
                    continue;

                // Levenshtein distance - strings are similar
                if (levenshteinDistance(oldSpam, saniMsg) < saniMsg.length() * msgDiffFactor) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Set the current player count
     *
     * @param count Current number of Players
     */
    public void setPlayerCount(int count) {
        lastMessages.setSize(Math.max(maxSentencesSaved, count * perPlayerQueueSizeFactor));
    }
}
