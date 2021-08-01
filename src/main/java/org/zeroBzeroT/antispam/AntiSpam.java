package org.zeroBzeroT.antispam;

import org.bukkit.ChatColor;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AntiSpam extends JavaPlugin implements Listener, CommandExecutor {
    private static final Set<UUID> bots = new HashSet<>();
    private static final Set<String> whisperCommands = new HashSet<>();
    private final Set<UUID> notMoved = ConcurrentHashMap.newKeySet();

    FileConfiguration config;

    private SpamCheck spamBotCheck;
    private String messageCannotTalk;
    private String messageSpamTalk;
    private String messageSpamWhisper;
    private boolean notMovedCheckEnabled;

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

        this.getCommand("showspam").setExecutor(this);
        this.getCommand("movereload").setExecutor(this);
    }

    @Override
    public void onDisable() {
        try {
            saveConfig();

            HandlerList.unregisterAll((JavaPlugin) this);
            HandlerList.unregisterAll((Listener) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && cmd.getName().equalsIgnoreCase("movereload")) {
            Player player = (Player) sender;

            if (player.hasPermission("move.reload")) {
                reloadConfig();
                player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', getConfig().getString("reload-message")));
                return true;
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("noPermissions")));
            return true;
        } else if (sender instanceof ConsoleCommandSender && cmd.getName().equalsIgnoreCase("showspam")) {
            log("showspam", ChatColor.DARK_PURPLE + "Here comes the spam:");

            for (String oldSpam : new LinkedList<>(spamBotCheck.lastSpamMessages)) {
                log("showspam", ChatColor.LIGHT_PURPLE + oldSpam);
            }

            return true;
        } else if (sender instanceof ConsoleCommandSender && cmd.getName().equalsIgnoreCase("showmessages")) {
            log("showmessages", ChatColor.DARK_PURPLE + "Here comes the last messages:");

            for (String oldMessage : new LinkedList<>(spamBotCheck.lastMessages)) {
                log("showmessages", ChatColor.LIGHT_PURPLE + oldMessage);
            }

            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerLoginEvent event) {
        if (notMovedCheckEnabled) {
            Player player = event.getPlayer();
            notMovedAdd(player);
        }
    }

    /**
     * flag player as "not moved" after killing
     * @param e
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (notMovedCheckEnabled) {
            Player player = e.getEntity();
            notMovedAdd(player);
        }
    }

    /**
     * clean up plugin player data
     * @param event
     */
    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        if (notMovedCheckEnabled) {
            notMoved.remove(event.getPlayer().getUniqueId());
        }

        spamBotCheck.setPlayerCount(getServer().getOnlinePlayers().size());
        spamBotCheck.onPlayerLeave(event.getPlayer().getUniqueId());
    }

    /**
     * chat spam check
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (isSpam(player, message, false)) {
            event.setCancelled(true);
        }
    }

    /**
     * whisper spam check
     * TODO: add /r command which only has 1 param
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (whisperCommands.stream().anyMatch(cmd -> message.toLowerCase().startsWith("/" + cmd + " "))) {
            String[] messagePart = message.split(" ", 3);

            if (messagePart.length == 3) {
                if (isSpam(player, messagePart[2], true)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * check if the player has moved over a block border
     * @param e
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!notMovedCheckEnabled)
            return;

        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
            return;

        notMoved.remove(e.getPlayer().getUniqueId());
    }

    /**
     * Checks if a message of a player is spam
     * @param player       sender
     * @param message      text message
     * @param isWhispering is private message
     * @return true, if the message is spam
     */
    private boolean isSpam(Player player, String message, boolean isWhispering) {
        boolean isSpam = false;

        // Bot Whitelist (not for whispering)
        if (!isWhispering && isBot(player)) {
            return false;
        }

        if (notMovedCheckEnabled && this.notMoved.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCannotTalk));
            return true;
        }

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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', isWhispering ? messageSpamWhisper : messageSpamTalk));

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
    private boolean isBot(Player player) {
        return isBot(player.getUniqueId());
    }

    /**
     * Checks, if a certain uuid is in the bot whitelist
     *
     * @param uuid
     * @return
     */
    private boolean isBot(UUID uuid) {
        return bots.contains(uuid);
    }

    /**
     * flag player as "not moved"
     *
     * @param player
     */
    private void notMovedAdd(Player player) {
        if (bots.contains(player.getUniqueId())) {
            log("Bot", player.getName());
            return;
        }

        if (!player.hasPermission("move.bypass")) {
            this.notMoved.add(player.getUniqueId());
        }
    }

    /**
     * print a log message
     *
     * @param module
     * @param message
     */
    public void log(String module, String message) {
        getLogger().info("§a[" + module + "] §e" + message + "§r");
    }
}
