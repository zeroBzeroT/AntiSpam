package org.zeroBzeroT.antispam;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("unchecked")
public class UnicodeRanges {

    @NotNull
    public static final List<Object> undefinedRange = Arrays.asList(0, 0, "undefined");

    final List<List<Object>> unicodeRanges;

    public UnicodeRanges(@NotNull final Plugin plugin) throws IOException, InvalidConfigurationException {
        plugin.saveResource("unicode_ranges.yml", false);
        YamlConfiguration config = new YamlConfiguration();

        config.load(plugin.getDataFolder() + "/unicode_ranges.yml");

        unicodeRanges = (List<List<Object>>) config.getList("ranges");
    }

    public UnicodeRanges() throws IOException, InvalidConfigurationException {
        final YamlConfiguration config = new YamlConfiguration();

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("unicode_ranges.yml")) {
            final InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
            final BufferedReader reader = new BufferedReader(streamReader);

            config.load(reader);
        }

        unicodeRanges = (List<List<Object>>) config.getList("ranges");

        assert unicodeRanges != null;
        unicodeRanges.add(undefinedRange);
    }

    @NotNull
    public String sanitizeText(@NotNull final String text, final int minimalPurgeLength) {
        final Map<List<Object>, Integer> count = countUnicodeRanges(text);

        // get the range with the most chars
        Map.Entry<List<Object>, Integer> maxRange = null;

        for (Map.Entry<List<Object>, Integer> entry : count.entrySet()) {
            if (maxRange == null || entry.getValue().compareTo(maxRange.getValue()) > 0) {
                maxRange = entry;
            }
        }

        if (maxRange == null)
            return "";

        // only remove the chars from other ranges if the text is long enough afterwards
        if (maxRange.getValue() <= minimalPurgeLength) {
            // return the old text if its not possible to remove chars
            return text;
        }

        final StringBuilder newText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            int unicode = text.codePointAt(i);

            if ((int) maxRange.getKey().get(0) <= unicode && unicode <= (int) maxRange.getKey().get(1)) {
                newText.append(text.charAt(i));
            }
        }

        // return the text without chars from other ranges
        return newText.toString();
    }

    /**
     * count the chars for each unicode range
      * @param text
     * @return Map of unicode range and corresponding char count
     */
    @NotNull
    public Map<List<Object>, Integer> countUnicodeRanges(@NotNull final String text) {
        final Map<List<Object>, Integer> count = new HashMap<>();

        boolean found = false;

        for (int i = 0; i < text.length(); i++) {
            for (final List<Object> range : unicodeRanges) {
                final int unicode = text.codePointAt(i);

                if ((int) range.get(0) <= unicode && unicode <= (int) range.get(1)) {
                    if (count.containsKey(range)) {
                        count.put(range, count.get(range) + 1);
                    } else {
                        count.put(range, 1);
                    }

                    found = true;
                    break;
                }
            }

            if (!found) {
                // All chars from undefined ranges are NOT discarded anymore ;) - With <3 0bOp
                if (count.containsKey(undefinedRange)) {
                    count.put(undefinedRange, count.get(undefinedRange) + 1);
                } else {
                    count.put(undefinedRange, 1);
                }
            }
        }

        return count;
    }
}
