package org.zeroBzeroT.antispam;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpamCheck {
    // minimum "percentage" of whitespace
    public static double whitespaceFrequency = 0.0625;

    // cooldown time [ms] that should be added for each typed character
    public static long cooldownPerCharacter = 200;

    // number of spam messages that is saved
    static int maxSpamSaved = 64;

    // maximum number of unicode ranges in a single message
    static int maxMessageUnicodeRanges = 4;

    // minimum message length before it is beeing checked
    static int minMessageLength = 8;

    // factor of the message difference
    static double msgDiffFactor = 1d / 5d;

    // maximum duplicated messages to be saved
    static int maxDuplicates = 2;

    // initial number of saved non spam sentences
    static int maxSentencesSaved = 128;

    // increment for the maximum saved sentences per player
    static int sentencesSavedPerPlayer = 5;

    // the last [maxSentencesSaved] chat messages for comparison
    @NotNull
    final LimitedSizeQueue<String> lastMessages = new LimitedSizeQueue<>(maxSentencesSaved);

    // the last [maxBadSentencesSaved] spam chat messages for comparison
    @NotNull
    final LimitedSizeQueue<String> lastSpamMessages = new LimitedSizeQueue<>(maxSpamSaved);

    // time at which the player is allowed to chat again
    @NotNull
    private final ConcurrentHashMap<UUID, Long> momentNextChatAllowed = new ConcurrentHashMap<>();

    // time at which the player is allowed to chat again
    @NotNull
    private final ConcurrentHashMap<UUID, Integer> shortMessageCount = new ConcurrentHashMap<>();

    // sanitizing message from chars that are from unicode ranges that are only used a few times in that message
    private UnicodeRanges unicodeRanges;

    /**
     * constructor without a given unicode range specification
     * the default range specification is used from the resources
     */
    public SpamCheck(@NotNull final Plugin plugin) {
        try {
            unicodeRanges = new UnicodeRanges(plugin);
        } catch (final IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * constructor with a given unicode range specification
     *
     * @param unicodeRanges unicode range class
     */
    public SpamCheck(@NotNull final UnicodeRanges unicodeRanges) {
        this.unicodeRanges = unicodeRanges;
    }

    /**
     * distance between two sentences
     *
     * @param x first sentence
     * @param y second sentence
     * @return the minimum number of single-character edits
     */
    static int levenshteinDistance(@NotNull final String x, @NotNull final String y) {
        final int[][] dp = new int[x.length() + 1][y.length() + 1];

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
    public static int costOfSubstitution(final char a, final char b) {
        return a == b ? 0 : 1;
    }

    /**
     * minimum of all given numbers
     *
     * @param numbers given numbers
     * @return minimum value of the numbers
     */
    public static int min(final int @NotNull ... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    /**
     * Checks message for spam
     *
     * @param message The message sent by the player
     * @return is the message spam
     */
    public boolean isRecurringSpam(@NotNull final String message) {
        // from [minMessageLength] character length
        if (message.length() < minMessageLength)
            return false;

        // remove non printable chars
        String saniMsg = message.replaceAll("[\\p{C}]", "");

        // use unicode ranges to sanitize text
        saniMsg = unicodeRanges.sanitizeText(saniMsg, minMessageLength);

        // remove long random numbers
        saniMsg = saniMsg.replaceAll("\\b\\d{9,}\\b", "");

        // remove hashcodes that some spammers use at the start or end of a spam text
        saniMsg = saniMsg.replaceAll("[^a-zA-Z0-9]*(?=([a-zA-Z]*\\d))\\S{4,}[^a-zA-Z0-9]*", "");

        // remove camelcase that some use at the start or end of a spam text
        //saniMsg = saniMsg.replaceAll("[^a-zA-Z0-9]*(?=([a-z]+[A-Z]+|[A-Z]+[a-z]+){2})\\S{3,}[^a-zA-Z0-9]*", "");
        // TODO: camelcase removal at the beginning and end of sentences

        // remove spaces
        saniMsg = saniMsg.replace(" ", "");

        // lowercase for better comparism
        saniMsg = saniMsg.toLowerCase();

        if (saniMsg.length() == 0) {
            // sanitized message is to short to display -> consider it spam
            return true;
        }

        // =======================================
        // Check message against old chat messages
        // =======================================

        int cntDuplicates = 0;

        // has the same already been written?
        for (final String oldMsg : new LinkedList<>(lastMessages)) { // copy contents to new object to avoid concurrent modification by async chat event handling
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

        lastMessages.add(saniMsg);

        // Spam found - Add message to the last spam messages
        if (cntDuplicates >= maxDuplicates) {
            // is Spam
            if (!lastSpamMessages.contains(saniMsg))
                lastSpamMessages.add(saniMsg);

            return true;
        }

        // =======================================
        // Check message against old spam messages
        // =======================================

        // TODO duplicate code?
        // Messages seems to be ok - so check the last spam messages
        for (final String oldSpam : new LinkedList<>(lastSpamMessages)) {
            // difference in length of the messages is already greater than the factor
            if (Math.abs(oldSpam.length() - saniMsg.length()) > Math.max(oldSpam.length(), saniMsg.length()) * msgDiffFactor)
                continue;

            // Levenshtein distance - strings are similar
            if (levenshteinDistance(oldSpam, saniMsg) < saniMsg.length() * msgDiffFactor) {
                return true;
            }
        }

        return false;
    }

    /**
     * Test if the message has "enough" whitespaces
     *
     * @param message
     * @return
     */
    public boolean isNoBlanksSpam(@NotNull final UUID uuid, @NotNull final String message) {
        // assume that the end of the text corresponds to one whitespace (+1)
        final float whitespaceCount = (message.length() - message.replaceAll(" ", "").length());

        if (whitespaceCount < 2) {
            // sentence has 2 or less whitespaces - temporary
            // TODO: maybe add a bucket cooldown thing here instead of that hardcoded crap
            final int count = shortMessageCount.getOrDefault(uuid, 0);

            if (count >= 3)
                return true;

            shortMessageCount.put(uuid, count + 1);
        } else {
            shortMessageCount.remove(uuid);
        }

        // Percentage of whitespace needed in long messages
        return ((whitespaceCount + 1f) / message.length()) < whitespaceFrequency;
    }

    /**
     * Checks if the player is allowed to send a message or still on cooldown
     *
     * @param uuid    player uuid
     * @param message
     * @return
     */
    public boolean isFloodSpam(@NotNull final UUID uuid, @NotNull final String message) {
        final long timeNow = System.currentTimeMillis();
        final Long timeAllowed = momentNextChatAllowed.get(uuid);

        momentNextChatAllowed.put(uuid, timeNow + getTypingTime(message));

        return !(timeAllowed == null || timeNow > timeAllowed);
    }

    /**
     * Check if the characters in a string are in more than the maximum amount of unicode ranges
     *
     * @param s
     * @return
     */
    public boolean isUnicodeRangeSpam(@NotNull final String s) {
        final int rangeCount = unicodeRanges.countUnicodeRanges(s).size();

        return rangeCount > maxMessageUnicodeRanges;
    }

    /**
     * Determines the typing time [ms] for the given message. The minimum time is 1 second.
     *
     * @param message
     * @return typing time in ms (min: 1000)
     */
    private long getTypingTime(@NotNull final String message) {
        return Math.max(1000, cooldownPerCharacter * message.length());
    }

    /**
     * Set the current player count
     *
     * @param count Current number of Players
     */
    public void setPlayerCount(final int count) {
        lastMessages.setSize(Math.max(maxSentencesSaved, count * sentencesSavedPerPlayer));
    }

    /**
     * Removes a player from
     *
     * @param uuid
     */
    public void onPlayerLeave(@NotNull final UUID uuid) {
        momentNextChatAllowed.remove(uuid);
        shortMessageCount.remove(uuid);
    }
}
