package org.zeroBzeroT.antispam;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AntiSpam extends JavaPlugin implements Listener, CommandExecutor {

    @NotNull
    private static final Set<UUID> bots = new HashSet<>();

    @NotNull
    private static final Set<String> whisperCommands = new HashSet<>();

    @NotNull
    private final Set<UUID> notMoved = ConcurrentHashMap.newKeySet();

    FileConfiguration config;

    private SpamCheck spamBotCheck;
    private String messageCannotTalk;
    private String messageSpamTalk;
    private String messageSpamWhisper;
    private boolean notMovedCheckEnabled;

    // accumulated message size
    private long cumulatedMessageSize;

    private boolean isSpam;

    @Override
    public void onEnable() {
        spamBotCheck = new SpamCheck(this);

        saveDefaultConfig();
        config = this.getConfig();

        bots.addAll(config.getStringList("bots").stream().map(UUID::fromString).collect(Collectors.toSet()));
        whisperCommands.addAll(config.getStringList("whisperCommands"));
        notMovedCheckEnabled = config.getBoolean("not-moved-check-enabled");
        messageCannotTalk = getConfig().getString("cannot-talk");
        messageSpamTalk = getConfig().getString("spam-talk-message");
        messageSpamWhisper = getConfig().getString("spam-whisper-message");

        SpamCheck.msgDiffFactor = config.getDouble("msg-diff-factor");
        SpamCheck.maxDuplicates = config.getInt("max-duplicates");
        SpamCheck.maxSentencesSaved = config.getInt("max-sentences-saved");
        SpamCheck.maxSpamSaved = config.getInt("max-spam-saved");
        SpamCheck.minMessageLength = config.getInt("min-message-length");
        SpamCheck.sentencesSavedPerPlayer = config.getInt("sentences-saved-per-player");
        SpamCheck.whitespaceFrequency = config.getDouble("whitespace-frequency");
        SpamCheck.maxMessageUnicodeRanges = config.getInt("maximum-unicode-ranges");
        SpamCheck.cooldownPerCharacter = (long) (60000d / config.getInt("maximum-characters-per-minute"));

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);

        Objects.requireNonNull(getCommand("showspam")).setExecutor(this);
        Objects.requireNonNull(getCommand("movereload")).setExecutor(this);

        cumulatedMessageSize = 0;
        isSpam = false;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            // assume that only 10% of the players ever chat ;)
            int threshold = config.getInt("maximum-characters-per-minute") * Bukkit.getOnlinePlayers().size() / 5;
            isSpam = cumulatedMessageSize > threshold;

            log("isSpam", "Spam: " + isSpam + " Size: " + cumulatedMessageSize + " Threshold: " + threshold);

            cumulatedMessageSize = 0;
        }, 0L, 20L * 60);
    }

    @Override
    public void onDisable() {
        try {
            saveConfig();

            HandlerList.unregisterAll((JavaPlugin) this);
            HandlerList.unregisterAll((Listener) this);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static Component deserializeLegacy(@Nullable final String legacyMessage) {
        assert legacyMessage != null;
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacyMessage);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command cmd,
                             @NotNull final String label, @NotNull final String @NotNull [] args) {
        final String commandName = cmd.getName().toLowerCase();

        if (sender instanceof final Player player && commandName.equals("movereload")) {
            if (player.hasPermission("move.reload")) {
                reloadConfig();
                player.sendMessage(deserializeLegacy(getConfig().getString("reload-message")));
                return true;
            }
            player.sendMessage(deserializeLegacy(getConfig().getString("noPermissions")));
            return true;
        }
        if (sender instanceof ConsoleCommandSender) {
            return switch (commandName) {
                case "showspam" -> logMessages("showspam", spamBotCheck.lastSpamMessages);
                case "showmessages" -> logMessages("showmessages", spamBotCheck.lastMessages);
                default -> false;
            };
        }
        return false;
    }

    private static boolean logMessages(@NotNull final String module, @NotNull final LimitedSizeQueue<String> queuedMessages) {
        log(module, Component.text("Here are the last messages:").color(NamedTextColor.DARK_PURPLE));

        for (final String oldMessage : new LinkedList<>(queuedMessages)) {
            log(module, Component.text(oldMessage).color(NamedTextColor.LIGHT_PURPLE));
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoinEvent(@NotNull final PlayerLoginEvent event) {
        if (notMovedCheckEnabled)
            notMovedAdd(event.getPlayer());
    }

    /**
     * flag player as "not moved" after killing
     *
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(@NotNull final PlayerDeathEvent event) {
        if (notMovedCheckEnabled)
            notMovedAdd(event.getEntity());
    }

    /**
     * clean up plugin player data
     *
     * @param event
     */
    @EventHandler
    public void onPlayerLeaveEvent(@NotNull final PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        if (notMovedCheckEnabled)
            notMoved.remove(uuid);

        spamBotCheck.setPlayerCount(getServer().getOnlinePlayers().size());
        spamBotCheck.onPlayerLeave(uuid);
    }

    /**
     * chat spam check
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(@NotNull final AsyncChatEvent event) {
        final String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        cumulatedMessageSize += message.length();

        if (isSpam(event.getPlayer(), message, false))
            event.setCancelled(true);
    }

    /**
     * whisper spam check
     * TODO: add /r command which only has 1 param
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(@NotNull final PlayerCommandPreprocessEvent event) {
        final String message = event.getMessage();

        if (whisperCommands.stream().noneMatch(cmd -> message.toLowerCase().startsWith("/" + cmd + " "))) return;
        final String[] messagePart = message.split(" ", 3);

        if (messagePart.length != 3) return;
        cumulatedMessageSize += event.getMessage().length();

        if (isSpam(event.getPlayer(), messagePart[2], true))
            event.setCancelled(true);
    }

    /**
     * check if the player has moved over a block border
     *
     * @param event
     */
    @EventHandler
    public void onPlayerMove(@NotNull final PlayerMoveEvent event) {
        if (!notMovedCheckEnabled)
            return;

        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ())
            return;

        notMoved.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Checks if a message of a player is spam
     *
     * @param player       sender
     * @param message      text message
     * @param isWhispering is private message
     * @return true, if the message is spam
     */
    private boolean isSpam(@NotNull final Player player, @NotNull final String message, final boolean isWhispering) {
        // spam attacked? if not -> return
        if (!isSpam)
            return false;

        // Bot Whitelist (not for whispering)
        if (!isWhispering && isBot(player))
            return false;

        if (notMovedCheckEnabled && this.notMoved.contains(player.getUniqueId())) {
            player.sendMessage(deserializeLegacy(messageCannotTalk));
            return true;
        }

        boolean isSpam = false;
        String failedTest = "";

        if (spamBotCheck.isFloodSpam(player.getUniqueId(), message)) {
            // should be always the first check
            // Flood check - [maximum-characters-per-minute] char hits per minute maximum ;)
            // its a per player chat cooldown depending on message length
            // cooldown for the new message is added even on violation
            isSpam = true;
            failedTest = "Flood";
        } else if (spamBotCheck.isNoBlanksSpam(player.getUniqueId(), message)) {
            // checks the frequency of whitespaces
            isSpam = true;
            failedTest = "No Blanks";
        } else if (spamBotCheck.isUnicodeRangeSpam(message)) {
            // [maximum-unicode-ranges] unicode ranges in a single message
            isSpam = true;
            failedTest = "Unicode Ranges";
        } else if (spamBotCheck.isRecurringSpam(message)) {
            // ignore [max-duplicates]nd duplicate message
            // Caps check?
            isSpam = true;
            failedTest = "Recurring";
        }

        if (isSpam) {
            player.sendMessage(deserializeLegacy(isWhispering ? messageSpamWhisper : messageSpamTalk));
            log("Failed " + failedTest, player.getName() + " " + (isWhispering ? "whispering" : "message") + " has been discarded.");
            return true;
        }

        return false;
    }

    /**
     * Checks, if a certain player is in the bot whitelist
     *
     * @param player
     * @return
     */
    private boolean isBot(@NotNull final Player player) {
        return isBot(player.getUniqueId());
    }

    /**
     * Checks, if a certain uuid is in the bot whitelist
     *
     * @param uuid
     * @return
     */
    private boolean isBot(@NotNull final UUID uuid) {
        return bots.contains(uuid);
    }

    /**
     * flag player as "not moved"
     *
     * @param player
     */
    private void notMovedAdd(@NotNull final Player player) {
        if (bots.contains(player.getUniqueId())) {
            log("Bot", player.getName());
            return;
        }

        if (!player.hasPermission("move.bypass"))
            this.notMoved.add(player.getUniqueId());
    }

    /**
     * print a log message
     *
     * @param module
     * @param message
     */
    public static void log(@NotNull final String module, @NotNull final Component message) {
        final Component prefix = Component.text("[" + module + "]").color(NamedTextColor.GREEN);
        final Component fullMessage = prefix.append(message);
        Bukkit.getConsoleSender().sendMessage(fullMessage);
    }

    /**
     * print a log message, but yellow by default
     *
     * @param module
     * @param message
     */
    public static void log(@NotNull final String module, @NotNull final String message) {
        log(module, Component.text(message).color(NamedTextColor.YELLOW));
    }
}
