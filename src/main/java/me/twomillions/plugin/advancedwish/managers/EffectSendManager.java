package me.twomillions.plugin.advancedwish.managers;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.managers.databases.MongoManager;
import me.twomillions.plugin.advancedwish.utils.BossBarRandomUtils;
import me.twomillions.plugin.advancedwish.utils.ExpUtils;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import top.lanscarlos.vulpecula.utils.ScriptUtilKt;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 2000000
 * @date 2022/11/24 20:27
 */
public class EffectSendManager {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 用于储存需要玩家以 OP 身份执行的命令
     */
    @Getter private volatile static Map<Player, String> opSentCommand = new ConcurrentHashMap<>();

    /**
     * 发送效果
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    public static void sendEffect(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        if (!targetPlayer.isOnline()) return;
        
        // isCancelled
        if (QuickUtils.callAsyncEffectSendEvent(fileName, targetPlayer, replacePlayer, path, pathPrefix).isCancelled()) return;

        String targetPlayerUUIDString = targetPlayer.getUniqueId().toString();

        // Logs
        if (isRecordEffectSend(fileName, path, pathPrefix)) {
            String logTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(System.currentTimeMillis());

            String finalLogString = logTime + ";" + targetPlayer.getName() + ";" + targetPlayer.getUniqueId() + ";" + QuickUtils.stringToUnicode(fileName) + ";" + pathPrefix + ";";

            if (MongoManager.getMongoConnectState() == MongoConnectState.Connected) MongoManager.addPlayerWishLog(targetPlayerUUIDString, finalLogString);
            else ConfigManager.addPlayerWishLog(targetPlayerUUIDString, finalLogString);
        }

        // Send
        sendTitle(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendParticle(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendSounds(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendCommands(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendMessage(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendAnnouncement(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendPotion(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendHealthAndHunger(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendExp(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendActionBar(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendBossBar(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendKether(fileName, targetPlayer, replacePlayer, path, pathPrefix);
    }

    /**
     * 是否开启效果发送日志记录
     *
     * @param fileName fileName
     * @return boolean
     */
    public static boolean isRecordEffectSend(String fileName, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        return Boolean.parseBoolean(QuickUtils.replaceTranslateToPapi(yaml.getOrDefault(pathPrefix + ".RECORD", "false")));
    }

    /**
     * 发送标题
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendTitle(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        // 如果是 1.7 服务器则不发送 Title (因为 1.7 没有)
        if (Main.getServerVersion() <= 107) return;

        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix == null ? "TITLE" : pathPrefix + ".TITLE");

        String mainTitle = QuickUtils.replaceTranslateToPapi(yaml.getString("MAIN-TITLE"), targetPlayer, replacePlayer);
        String subTitle = QuickUtils.replaceTranslateToPapi(yaml.getString("SUB-TITLE"), targetPlayer, replacePlayer);

        if ("".equals(mainTitle) && "".equals(subTitle)) return;

        int fadeIn = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("FADE-IN"), targetPlayer, replacePlayer));
        int fadeOut = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("FADE-OUT"), targetPlayer, replacePlayer));
        int stay = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("STAY"), targetPlayer, replacePlayer));

        /*
         * 在 1.9 中由于此方法无法定义 fadeIn stay fadeOut 所以使用不同的方法
         * 没有使用 NMS, Spigot API 提供了一种发送标题的方法 旨在跨不同的 Minecraft 版本工作
         */
        if (Main.getServerVersion() == 109) targetPlayer.sendTitle(mainTitle, subTitle);
        else targetPlayer.sendTitle(mainTitle, subTitle, fadeIn, stay, fadeOut);
    }

    /**
     * 发送粒子效果
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendParticle(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("PARTICLE").forEach(particleConfig -> {
            if (particleConfig == null || particleConfig.length() <= 1) return;

            particleConfig = QuickUtils.replaceTranslateToPapi(particleConfig, targetPlayer, replacePlayer);

            String[] particleConfigSplit = particleConfig.toUpperCase(Locale.ROOT).split(";");

            ParticleEffect particleEffect;
            String particleString = particleConfigSplit[0];

            try { particleEffect = ParticleEffect.valueOf(particleString); }
            catch (Exception exception) { QuickUtils.sendUnknownWarn("粒子效果", fileName, particleString); return; }

            double x = Double.parseDouble(QuickUtils.count(particleConfigSplit[1]).toString());
            double y = Double.parseDouble(QuickUtils.count(particleConfigSplit[2]).toString());
            double z = Double.parseDouble(QuickUtils.count(particleConfigSplit[3]).toString());
            int amount = Integer.parseInt(QuickUtils.count(particleConfigSplit[4]).toString());

            boolean isNote = particleEffect == ParticleEffect.NOTE;
            boolean allPlayer = !particleConfigSplit[5].equals("PLAYER");
            boolean hasColor = !particleConfigSplit[6].equals("FALSE");

            if (isNote && hasColor) {
                QuickUtils.sendConsoleMessage("&c请注意，音符 (Note) 粒子效果并不支持自定义颜色! 已自动切换为随机颜色!");

                if (allPlayer)
                    new ParticleBuilder(particleEffect, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .display();
                else
                    new ParticleBuilder(particleEffect, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .display(targetPlayer);
                return;
            }

            if (hasColor) {
                if (allPlayer)
                    new ParticleBuilder(particleEffect, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .setColor(Color.getColor(particleConfigSplit[6]))
                            .display();
                else
                    new ParticleBuilder(particleEffect, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .setColor(Color.getColor(particleConfigSplit[6]))
                            .display(targetPlayer);
                return;
            }

            if (allPlayer)
                new ParticleBuilder(particleEffect, targetPlayer.getLocation())
                        .setOffsetX((float) x)
                        .setOffsetY((float) y)
                        .setOffsetZ((float) z)
                        .setAmount(amount)
                        .display();
            else
                new ParticleBuilder(particleEffect, targetPlayer.getLocation())
                        .setOffsetX((float) x)
                        .setOffsetY((float) y)
                        .setOffsetZ((float) z)
                        .setAmount(amount)
                        .display(targetPlayer);
        });
    }

    /**
     * 发送音效
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendSounds(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("SOUNDS").forEach(soundsConfig -> {
            if (soundsConfig == null || soundsConfig.length() <= 1) return;

            soundsConfig = QuickUtils.replaceTranslateToPapi(soundsConfig, targetPlayer, replacePlayer);

            String[] soundsConfigSplit = soundsConfig.toUpperCase(Locale.ROOT).split(";");

            Sound sound;
            String soundString = soundsConfigSplit[0];

            try { sound = Sound.valueOf(soundString); }
            catch (Exception exception) { QuickUtils.sendUnknownWarn("音效", fileName, soundString); return; }

            int volume = Integer.parseInt(QuickUtils.count(soundsConfigSplit[1]).toString());
            int pitch = Integer.parseInt(QuickUtils.count(soundsConfigSplit[2]).toString());

            targetPlayer.playSound(targetPlayer.getLocation(), sound, volume, pitch);
        });
    }

    /**
     * 发送指令
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendCommands(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix == null ? "COMMANDS" : pathPrefix + ".COMMANDS");

        yaml.getStringList("PLAYER").forEach(commandConfig -> {
            if (commandConfig == null || commandConfig.length() <= 1) return;

            // OP 执行
            commandConfig = QuickUtils.replaceTranslateToPapi(commandConfig, targetPlayer, replacePlayer).toLowerCase(Locale.ROOT);

            if (!commandConfig.startsWith("[op]:") || targetPlayer.isOp()) {
                String finalCommand = commandConfig.replace("[op]:", "");
                Bukkit.getScheduler().runTask(plugin, () -> targetPlayer.performCommand(finalCommand));
            } else {
                commandConfig = commandConfig.replace("[op]:", "");

                // 这绝不是万无一失的，但是可以保证比较安全的
                try {
                    WishManager.setPlayerCacheOpData(targetPlayer, true);
                    getOpSentCommand().put(targetPlayer, commandConfig);

                    String finalCommand = commandConfig;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        targetPlayer.setOp(true);
                        targetPlayer.performCommand(finalCommand);
                    });
                } finally {
                    Bukkit.getScheduler().runTask(plugin, () -> targetPlayer.setOp(false));

                    getOpSentCommand().remove(targetPlayer);
                    WishManager.setPlayerCacheOpData(targetPlayer, null);
                }
            }
        });

        yaml.getStringList("CONSOLE").forEach(commandConfig -> {
            if (commandConfig == null || commandConfig.length() <= 1) return;

            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            commandConfig = QuickUtils.replaceTranslateToPapi(commandConfig, targetPlayer, replacePlayer);

            String finalCommandConfig = commandConfig;
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(console, finalCommandConfig));
        });
    }

    /**
     * 发送消息
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendMessage(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("MESSAGE").forEach(messageConfig -> {
            if (messageConfig == null || messageConfig.length() <= 1) return;

            /*
             * https://www.spigotmc.org/threads/is-sendmessage-thread-safe-like-this.372767/
             *
             * Messaging is one of the few things that can be done async safely.
             * 消息传递是为数不多的可以异步安全完成的事情之一。 By SpigotMc Moderator SteelPhoenix
             */
            targetPlayer.sendMessage(QuickUtils.replaceTranslateToPapi(messageConfig, targetPlayer, replacePlayer));
        });
    }

    /**
     * 发送公告
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendAnnouncement(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("ANNOUNCEMENT").forEach(announcementConfig -> {
            if (announcementConfig == null || announcementConfig.length() <= 1) return;

            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(QuickUtils.replaceTranslateToPapi(announcementConfig, targetPlayer, replacePlayer)));
        });
    }

    /**
     * 发送药水效果
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendPotion(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("EFFECTS").forEach(effectsConfig -> {
            if (effectsConfig == null || effectsConfig.length() <= 1) return;

            effectsConfig = QuickUtils.replaceTranslateToPapi(effectsConfig, targetPlayer, replacePlayer);

            String[] effectsConfigSplit = effectsConfig.split(";");

            String effectString = effectsConfigSplit[0];
            PotionEffectType effectType;

            try { effectType = PotionEffectType.getByName(effectString); }
            catch (Exception exception) { QuickUtils.sendUnknownWarn("药水效果", fileName, effectString); return; }

            int duration = Integer.parseInt(QuickUtils.count(effectsConfigSplit[1]).toString());
            int amplifier = Integer.parseInt(QuickUtils.count(effectsConfigSplit[2]).toString());

            if (effectType == null) {
                QuickUtils.sendUnknownWarn("药水效果", fileName, effectString);
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> targetPlayer.addPotionEffect(new PotionEffect(effectType, duration, amplifier)));
        });
    }

    /**
     * 回复血量以及饱食度
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendHealthAndHunger(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        int hunger = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("HUNGER"), targetPlayer, replacePlayer));
        double health = Double.parseDouble(QuickUtils.replaceTranslateToPapiCount(yaml.getString("HEALTH"), targetPlayer, replacePlayer));

        if (health != 0) {
            double playerHealth = targetPlayer.getHealth();
            targetPlayer.setHealth(Math.min(playerHealth + health, targetPlayer.getMaxHealth()));
        }

        if (hunger != 0) {
            int playerFoodLevel = targetPlayer.getFoodLevel();
            targetPlayer.setFoodLevel(Math.min(playerFoodLevel + hunger, 20));
        }
    }

    /**
     * 给予 EXP
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendExp(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix);

        int exp = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("EXP"), targetPlayer, replacePlayer));

        if (exp != 0) ExpUtils.addExp(targetPlayer, exp);
    }

    /**
     * 发送 Action Bar
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendActionBar(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        // 如果是 1.7 服务器则不发送 Action Bar (因为 1.7 没有)
        if (Main.getServerVersion() <= 107) return;

        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix == null ? "ACTION-BAR" : pathPrefix + ".ACTION-BAR");

        String actionBarMessage = QuickUtils.replaceTranslateToPapi(yaml.getString("MESSAGE"), targetPlayer, replacePlayer);
        int actionBarTime = Integer.parseInt(QuickUtils.replaceTranslateToPapiCount(yaml.getString("TIME"), targetPlayer, replacePlayer));

        if ("".equals(actionBarMessage) || actionBarTime == 0) return;

        // 由于 Action Bar 并没有具体地淡出淡入显示时间参数，所以只能通过 Runnable 发送

        new BukkitRunnable() {
            int time = 0;
            @Override
            public void run() {
                if (time >= actionBarTime) {
                    cancel();
                    return;
                }

                time ++;

                TextComponent textComponent = new TextComponent(QuickUtils.replaceTranslateToPapi(yaml.getString("MESSAGE"), targetPlayer, replacePlayer));
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    /**
     * 发送 Boss Bar
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendBossBar(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        // Boss Bar 支持 1.7 / 1.8 会使用到 NMS 所以我选择直接放弃对于 1.7 / 1.8 的 Boss Bar 支持
        if (Main.getServerVersion() <= 108) return;

        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix == null ? "BOSS-BAR" : pathPrefix + ".BOSS-BAR");

        String bossBarMessage = QuickUtils.replaceTranslateToPapi(yaml.getString("MESSAGE"), targetPlayer, replacePlayer);
        double bossBarTime = Double.parseDouble(QuickUtils.replaceTranslateToPapiCount(yaml.getString("TIME"), targetPlayer, replacePlayer));

        if ("".equals(bossBarMessage) || bossBarTime == 0) return;

        String barColorString = QuickUtils.replaceTranslateToPapi(yaml.getString("COLOR"), targetPlayer, replacePlayer);
        String barStyleString = QuickUtils.replaceTranslateToPapi(yaml.getString("STYLE"), targetPlayer, replacePlayer);

        BarColor bossBarColor = barColorString.equals("RANDOM") ? BossBarRandomUtils.randomColor() : BarColor.valueOf(barColorString);
        BarStyle bossBarStyle = barStyleString.equals("RANDOM") ? BossBarRandomUtils.randomStyle() : BarStyle.valueOf(barStyleString);

        BossBar bossBar = Bukkit.createBossBar(QuickUtils.replaceTranslateToPapi(bossBarMessage, targetPlayer, replacePlayer), bossBarColor, bossBarStyle);

        bossBar.addPlayer(targetPlayer);

        // 秒数 使用 Runnable
        new BukkitRunnable() {
            double timeLeft = bossBarTime;
            @Override
            public void run() {
                timeLeft = timeLeft - 0.05;

                if (timeLeft <= 0.0) {
                    bossBar.removeAll();
                    cancel();
                    return;
                }

                bossBar.setProgress(timeLeft / bossBarTime);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    /**
     * 运行 Kether
     *
     * @param fileName fileName
     * @param targetPlayer targetPlayer
     * @param replacePlayer replacePlayer
     * @param path path
     * @param pathPrefix pathPrefix
     */
    private static void sendKether(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        if (!RegisterManager.isUsingVulpecula()) return;

        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);

        String kether = QuickUtils.replaceTranslateToPapi(yaml.getString(pathPrefix + ".KETHER"), targetPlayer, replacePlayer);

        if ("".equals(kether)) return;

        ScriptUtilKt.eval(kether, targetPlayer, new ArrayList<>(), null, false);
    }
}
