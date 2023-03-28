package me.twomillions.plugin.advancedwish.managers.effect;

import com.github.benmanes.caffeine.cache.Cache;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.managers.WishManager;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.managers.register.RegisterManager;
import me.twomillions.plugin.advancedwish.utils.events.EventUtils;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.random.BossBarRandomUtils;
import me.twomillions.plugin.advancedwish.utils.others.CaffeineUtils;
import me.twomillions.plugin.advancedwish.utils.others.ExpUtils;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.texts.StringEncrypter;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 主要效果发送类。
 *
 * @author 2000000
 * @date 2022/11/24 20:27
 */
@SuppressWarnings("deprecation")
public class EffectSendManager {
    private static final Plugin plugin = Main.getInstance();

    /**
     * 用于储存需要玩家以 OP 身份执行的命令。
     */
    @Getter private volatile static Cache<Player, String> opSentCommand = CaffeineUtils.buildBukkitCache();

    /**
     * 发送效果。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    public static void sendEffect(String fileName, Player player, String path, String pathPrefix) {
        if (!player.isOnline()) return;
        
        // isCancelled
        if (EventUtils.callAsyncEffectSendEvent(fileName, player, path, pathPrefix).isCancelled()) return;

        // Send
        try {
            sendTitle(fileName, player, path, pathPrefix);
            sendParticle(fileName, player, path, pathPrefix);
            sendSounds(fileName, player, path, pathPrefix);
            sendCommands(fileName, player, path, pathPrefix);
            sendMessage(fileName, player, path, pathPrefix);
            sendChat(fileName, player, path, pathPrefix);
            sendAnnouncement(fileName, player, path, pathPrefix);
            sendPotion(fileName, player, path, pathPrefix);
            sendHealthAndHunger(fileName, player, path, pathPrefix);
            sendExp(fileName, player, path, pathPrefix);
            sendActionBar(fileName, player, path, pathPrefix);
            sendBossBar(fileName, player, path, pathPrefix);
            runKether(fileName, player, path, pathPrefix);
        } catch (Exception exception) {
            QuickUtils.sendConsoleMessage("&c效果发送错误! 配置是否完整? 奖池配置文件是否错误? 文件名: &e" + fileName + "&c，路径: &e" + path + "&c，节点: &e" + pathPrefix + "&c，目标玩家: &e" + player.getName());
            return;
        }

        // Logs
        String playerUUIDString = player.getUniqueId().toString();

        if (isRecordEffectSend(fileName, path, pathPrefix)) {
            String logTime = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(System.currentTimeMillis());

            String finalLogString = logTime + ";" + player.getName() + ";" + player.getUniqueId() + ";" + StringEncrypter.encrypt(fileName) + ";" + pathPrefix + ";";

            // call AsyncRecordEffectSendEvent
            if (EventUtils.callAsyncRecordEffectSendEvent(player, fileName, path, pathPrefix).isCancelled()) {
                return;
            }

            LogManager.addPlayerWishLog(playerUUIDString, finalLogString);
        }
    }

    /**
     * 是否开启效果发送日志记录。
     *
     * @param fileName fileName
     * @return boolean
     */
    public static boolean isRecordEffectSend(String fileName, String path, String pathPrefix) {
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        return QuickUtils.handleBoolean(yaml.getOrDefault(pathPrefix + ".RECORD", "false"));
    }

    /**
     * 向目标玩家发送标题。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendTitle(String fileName, Player player, String path, String pathPrefix) {
        // 判断服务器版本，1.7 版本以下不支持标题发送
        if (Main.getServerVersion() <= 107) return;

        // 读取标题 Yaml 文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        yaml.setPathPrefix(pathPrefix == null ? "TITLE" : pathPrefix + ".TITLE");

        // 替换标题的主副文本中的占位符
        String mainTitle = QuickUtils.handleString(yaml.getString("MAIN-TITLE"), player);
        String subTitle = QuickUtils.handleString(yaml.getString("SUB-TITLE"), player);

        // 如果标题文本为空，直接返回
        if ("".equals(mainTitle) && "".equals(subTitle)) return;

        // 替换并获取标题淡入、停留和淡出时间
        int fadeIn = QuickUtils.handleInt(yaml.getString("FADE-IN"), player);
        int fadeOut = QuickUtils.handleInt(yaml.getString("FADE-OUT"), player);
        int stay = QuickUtils.handleInt(yaml.getString("STAY"), player);

        /*
         * 为不同版本的 Minecraft 使用不同的发送标题方法
         * 1.9 版本及以上使用新的方法，而 1.9 版本以下使用旧的方法
         * Spigot API 提供了这两种方法，不需要使用 NMS
         */
        if (Main.getServerVersion() == 109) {
            player.sendTitle(mainTitle, subTitle);
        } else {
            player.sendTitle(mainTitle, subTitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * 发送粒子效果。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendParticle(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 遍历 PARTICLE 列表并发送
        yaml.getStringList("PARTICLE").forEach(particleConfig -> {
            if (particleConfig == null || particleConfig.length() <= 1) return;

            // 解析粒子效果配置
            String[] particleConfigSplit = QuickUtils.handleStrings(particleConfig.toUpperCase(Locale.ROOT).split(";"), player);

            ParticleEffect particleEffect;
            String particleString = particleConfigSplit[0];

            try {
                // 将字符串转换为 ParticleEffect 枚举
                particleEffect = ParticleEffect.valueOf(particleString);
            } catch (Exception exception) {
                // 粒子效果未知，发送警告信息
                ExceptionUtils.sendUnknownWarn("粒子效果", fileName, particleString);
                return;
            }

            // 解析x、y、z、amount参数
            double x = Double.parseDouble(particleConfigSplit[1]);
            double y = Double.parseDouble(particleConfigSplit[2]);
            double z = Double.parseDouble(particleConfigSplit[3]);
            int amount = Integer.parseInt(particleConfigSplit[4]);

            // 是否为 Note 粒子效果，是否对所有玩家生效，是否具有颜色属性
            boolean isNote = particleEffect == ParticleEffect.NOTE;
            boolean allPlayer = !particleConfigSplit[5].equals("PLAYER");
            boolean hasColor = !particleConfigSplit[6].equals("FALSE");

            // 如果是 Note 粒子效果且具有颜色属性，则无法自定义颜色，发送警告信息
            if (isNote && hasColor) {
                QuickUtils.sendConsoleMessage("&c请注意，音符 (Note) 粒子效果并不支持自定义颜色! 已自动切换为随机颜色!");

                if (allPlayer) {
                    new ParticleBuilder(particleEffect, player.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .display();
                } else {
                    new ParticleBuilder(particleEffect, player.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .display(player);
                }
                return;
            }

            // 如果具有颜色属性，则设置颜色
            if (hasColor) {
                if (allPlayer) {
                    new ParticleBuilder(particleEffect, player.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .setColor(Color.getColor(particleConfigSplit[6]))
                            .display();
                } else {
                    new ParticleBuilder(particleEffect, player.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .setColor(Color.getColor(particleConfigSplit[6]))
                            .display(player);
                }
                return;
            }

            if (allPlayer) {
                new ParticleBuilder(particleEffect, player.getLocation())
                        .setOffsetX((float) x)
                        .setOffsetY((float) y)
                        .setOffsetZ((float) z)
                        .setAmount(amount)
                        .display();
            } else {
                new ParticleBuilder(particleEffect, player.getLocation())
                        .setOffsetX((float) x)
                        .setOffsetY((float) y)
                        .setOffsetZ((float) z)
                        .setAmount(amount)
                        .display(player);
            }
        });
    }

    /**
     * 发送音效给指定玩家，支持替换占位符。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendSounds(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        List<String> soundConfigs = yaml.getStringList("SOUNDS");

        // 遍历音效列表并发送每个音效
        soundConfigs.forEach(soundConfig -> {
            if (soundConfig == null || soundConfig.length() <= 1) return;

            // 解析音效配置
            String[] soundConfigSplit = QuickUtils.handleStrings(soundConfig.toUpperCase(Locale.ROOT).split(";"), player);

            Sound sound;
            String soundString = soundConfigSplit[0];

            try {
                sound = Sound.valueOf(soundString);
            } catch (Exception exception) {
                // 如果解析音效配置失败，则打印错误日志并跳过
                ExceptionUtils.sendUnknownWarn("音效", fileName, soundString);
                return;
            }

            int volume = Integer.parseInt(soundConfigSplit[1]);
            int pitch = Integer.parseInt(soundConfigSplit[2]);

            // 播放音效
            player.playSound(player.getLocation(), sound, volume, pitch);
        });
    }

    /**
     * 发送指令。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendCommands(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix == null ? "COMMANDS" : pathPrefix + ".COMMANDS");

        // 执行玩家指令
        yaml.getStringList("PLAYER").forEach(commandConfig -> {
            if (commandConfig == null || commandConfig.length() <= 1) return;

            commandConfig = QuickUtils.handleString(commandConfig, player);

            // 判断指令是否需要 OP 权限
            if (!commandConfig.startsWith("[op]:") || player.isOp()) {
                // 去掉 [op]: 前缀后执行指令
                String finalCommand = commandConfig.replace("[op]:", "");
                Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(finalCommand));
            } else {
                // 处理需要 OP 权限的指令
                commandConfig = commandConfig.replace("[op]:", "");

                // 为目标玩家赋予临时 OP 权限并执行指令
                try {
                    WishManager.setPlayerCacheOpData(player, true);
                    getOpSentCommand().put(player, commandConfig);

                    String finalCommand = commandConfig;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.setOp(true);
                        player.performCommand(finalCommand);
                    });
                } finally {
                    // 执行完指令后移除临时 OP 权限
                    Bukkit.getScheduler().runTask(plugin, () -> player.setOp(false));

                    getOpSentCommand().invalidate(player);

                    WishManager.setPlayerCacheOpData(player, false);
                }
            }
        });

        // 执行控制台指令
        yaml.getStringList("CONSOLE").forEach(commandConfig -> {
            if (commandConfig == null || commandConfig.length() <= 1) return;

            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

            // 替换翻译占位符并执行控制台指令
            String finalCommandConfig = QuickUtils.handleString(commandConfig, player);
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(console, finalCommandConfig));
        });
    }

    /**
     * 向玩家发送消息。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendMessage(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 读取
        List<String> messageConfigs = yaml.getStringList("MESSAGE");

        /*
         * https://www.spigotmc.org/threads/is-sendmessage-thread-safe-like-this.372767/
         *
         * Messaging is one of the few things that can be done async safely.
         * 消息传递是为数不多的可以异步安全完成的事情之一。 By SpigotMc Moderator SteelPhoenix
         */

        // 向目标玩家发送消息
        messageConfigs.stream()
                .filter(messageConfig -> messageConfig != null && messageConfig.length() > 1)
                .map(messageConfig -> QuickUtils.handleString(messageConfig, player))
                .forEach(player::sendMessage);
    }

    /**
     * 使玩家发送信息。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendChat(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 读取
        List<String> messageConfigs = yaml.getStringList("CHAT");

        // 使目标玩家发送信息
        messageConfigs.stream()
                .filter(messageConfig -> messageConfig != null && messageConfig.length() > 1)
                .map(messageConfig -> QuickUtils.handleString(messageConfig, player))
                .forEach(player::chat);
    }

    /**
     * 发送公告。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendAnnouncement(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 遍历公告配置列表
        yaml.getStringList("ANNOUNCEMENT").forEach(announcementConfig -> {
            if (announcementConfig == null || announcementConfig.length() <= 1) return;

            // 广播公告
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(QuickUtils.handleString(announcementConfig, player)));
        });
    }

    /**
     * 给目标玩家发送药水效果。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendPotion(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 遍历配置中的所有药水效果
        yaml.getStringList("EFFECTS").forEach(effectConfig -> {
            if (effectConfig == null || effectConfig.length() <= 1) return;

            // 解析药水效果配置
            String[] effectConfigSplit = QuickUtils.handleStrings(effectConfig.split(";"), player);
            String effectString = effectConfigSplit[0];

            PotionEffectType effectType = PotionEffectType.getByName(effectString);

            int duration = Integer.parseInt(effectConfigSplit[1]);
            int amplifier = Integer.parseInt(effectConfigSplit[2]);

            // 如果药水效果为空，则发送未知警告并返回
            if (effectType == null) { ExceptionUtils.sendUnknownWarn("药水效果", fileName, effectString); return; }

            // 给目标玩家添加药水效果
            Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(new PotionEffect(effectType, duration, amplifier)));
        });
    }

    /**
     * 回复玩家的血量和饱食度。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendHealthAndHunger(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 解析配置
        int hunger = QuickUtils.handleInt(yaml.getString("HUNGER"), player);
        double health = QuickUtils.handleDouble(yaml.getString("HEALTH"), player);

        if (health != 0) {
            double playerHealth = player.getHealth();
            player.setHealth(Math.min(playerHealth + health, player.getMaxHealth()));
        }

        if (hunger != 0) {
            int playerFoodLevel = player.getFoodLevel();
            player.setFoodLevel(Math.min(playerFoodLevel + hunger, 20));
        }
    }

    /**
     * 给玩家增加经验值。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendExp(String fileName, Player player, String path, String pathPrefix) {
        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix);

        // 解析配置
        int exp = QuickUtils.handleInt(yaml.getString("EXP"), player);

        if (exp != 0) ExpUtils.addExp(player, exp);
    }

    /**
     * 发送 Action Bar 消息。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendActionBar(String fileName, Player player, String path, String pathPrefix) {
        // 如果是 1.7 服务器则不发送 Action Bar (因为 1.7 没有)
        if (Main.getServerVersion() <= 107) return;

        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix == null ? "ACTION-BAR" : pathPrefix + ".ACTION-BAR");

        // 解析配置
        String actionBarMessage = QuickUtils.handleString(yaml.getString("MESSAGE"), player);
        int actionBarTime = QuickUtils.handleInt(yaml.getString("TIME"), player);

        if (actionBarMessage.isEmpty() || actionBarTime == 0) return;

        new BukkitRunnable() {
            int time = 0;
            @Override
            public void run() {
                if (time >= actionBarTime) { cancel(); return; }

                time++;

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    /**
     * 发送 Boss Bar 给目标玩家。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void sendBossBar(String fileName, Player player, String path, String pathPrefix) {
        // 判断是否支持 Boss Bar
        if (Main.getServerVersion() <= 108) return;

        // 创建配置文件
        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);
        // 设置路径前缀
        yaml.setPathPrefix(pathPrefix == null ? "BOSS-BAR" : pathPrefix + ".BOSS-BAR");

        // 解析配置
        String bossBarMessage = QuickUtils.handleString(yaml.getString("MESSAGE"), player);
        double bossBarTime = QuickUtils.handleDouble(yaml.getString("TIME"), player);
        String barColorString = QuickUtils.handleString(yaml.getString("COLOR"), player);
        String barStyleString = QuickUtils.handleString(yaml.getString("STYLE"), player);

        // 判断 Boss Bar 的信息是否为空
        if ("".equals(bossBarMessage) || bossBarTime == 0) return;

        // 设置 Boss Bar 的颜色和样式
        BarColor bossBarColor = barColorString.equals("RANDOM") ? BossBarRandomUtils.randomColor() : BarColor.valueOf(barColorString);
        BarStyle bossBarStyle = barStyleString.equals("RANDOM") ? BossBarRandomUtils.randomStyle() : BarStyle.valueOf(barStyleString);

        // 创建 Boss Bar 并添加到目标玩家
        BossBar bossBar = Bukkit.createBossBar(bossBarMessage, bossBarColor, bossBarStyle);
        bossBar.addPlayer(player);

        // 使用异步任务更新 Boss Bar 的进度
        new BukkitRunnable() {
            double timeLeft = bossBarTime;
            @Override
            public void run() {
                timeLeft -= 0.05;
                if (timeLeft <= 0) { bossBar.removeAll(); cancel(); return; }
                bossBar.setProgress(timeLeft / bossBarTime);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    /**
     * 运行 Kether 代码。
     *
     * @param fileName 配置文件名
     * @param player 目标玩家
     * @param path 配置文件路径
     * @param pathPrefix 配置文件路径前缀
     */
    private static void runKether(String fileName, Player player, String path, String pathPrefix) {
        // 检查是否安装了 Vulpecula 插件
        if (!RegisterManager.isUsingVulpecula()) return;

        Yaml yaml = ConfigManager.createYaml(fileName, path, true, false);

        String ketherCode = QuickUtils.handleString(yaml.getString(pathPrefix + ".KETHER"), player);

        if (ketherCode.isEmpty()) return;

        // 解析执行 Kether 代码
        ScriptUtilKt.eval(ketherCode, player, Collections.emptyList(), null, false);
    }
}