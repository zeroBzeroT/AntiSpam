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
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AntiSpam extends JavaPlugin implements Listener, CommandExecutor {
    static final List<String> whisperCommands = Arrays.asList("tell", "w", "msg", "whisper");
    public static List<String> bots = new ArrayList<>();
    final ArrayList<Player> notMoved = new ArrayList<>();
    FileConfiguration config;
    private SpamCheck spamBotCheck;


    @Override
    public void onEnable() {
        spamBotCheck = new SpamCheck(this);

        saveDefaultConfig();
        config = this.getConfig();
        bots = config.getStringList("bots");

        SpamCheck.msgDiffFactor = config.getDouble("msg-diff-factor");
        SpamCheck.maxDuplicates = config.getInt("max-duplicates");
        SpamCheck.maxSentencesSaved = config.getInt("max-sentences-saved");
        SpamCheck.minMessageLength = config.getInt("min-message-length");
        SpamCheck.perPlayerQueueSizeFactor = config.getInt("per-player-queue-size-factor");

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
        Player player = event.getPlayer();

        addNotMoved(player);
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        int playersOnline = getServer().getOnlinePlayers().size();
        Player player = event.getPlayer();
        delNotMoved(player);
        spamBotCheck.setPlayerCount(playersOnline);
    }

    // chat spam check
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (this.notMoved.contains(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("cannot-talk")));
            return;
        }

        if(isBot(player)) {
            return;
        }

        if (spamBotCheck.isRecurringSpam(message)) {
            event.setCancelled(true);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("spam-talk-message")));

            log("onPlayerChatEvent", player.getName() + " message has been discarded.");
        }
    }

    private boolean isBot(Player player) {
        // Bots
        for (String bot : AntiSpam.bots) {
            if (player.getName().toLowerCase().contentEquals(bot.toLowerCase())) {
                return true;
            }
            if(player.getUniqueId().equals(UUID.fromString(bot))) {
                return true;
            }
        }

        return false;
    }

    // whisper spam check
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        String inputText = event.getMessage();

        if (whisperCommands.stream().anyMatch(cmd -> inputText.toLowerCase().startsWith("/" + cmd + " "))) {
            String[] args = inputText.split(" ", 3);

            if (args.length == 3) {
                if (this.notMoved.contains(player)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("cannot-talk")));
                    return;
                }

                if (spamBotCheck.isRecurringSpam(args[2])) {
                    event.setCancelled(true);

                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("spam-whisper-message")));

                    log("onPlayerCommandPreprocess", player.getName() + " whispering has been discarded.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        // check if the player has moved over a block border
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
            return;

        Player player = e.getPlayer();

        if (this.notMoved.contains(player)) {
            delNotMoved(player);
        }
    }

    // TODO: add not moved after killing
    private void addNotMoved(Player player) {
        for (String bot : bots) {
            if (bot.toLowerCase().contentEquals(player.getName().toLowerCase())) {
                log("BOT", player.getName());
                return;
            }
        }

        if (!player.hasPermission("move.bypass")) {
            this.notMoved.add(player);
        }
    }

    private void delNotMoved(Player player) {
        this.notMoved.remove(player);
    }

    public void log(String module, String message) {
        getLogger().info("§a[" + module + "] §e" + message + "§r");
    }
}
