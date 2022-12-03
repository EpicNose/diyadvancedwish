package me.twomillions.plugin.advancedwish.manager;

import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.utils.BossBarRandomUtils;
import me.twomillions.plugin.advancedwish.utils.CC;
import me.twomillions.plugin.advancedwish.utils.ExpUtils;
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
import org.fusesource.jansi.Ansi;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.*;
import java.util.Locale;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.manager
 * className:      EffectSendManager
 * date:    2022/11/24 20:27
 */
public class EffectSendManager {
    private static final Plugin plugin = main.getInstance();

    // 效果发送
    public static void sendEffect(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        sendTitle(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendParticle(fileName, targetPlayer, path, pathPrefix);
        sendSounds(fileName, targetPlayer, path, pathPrefix);
        sendCommands(fileName, targetPlayer, path, pathPrefix);
        sendMessage(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendAnnouncement(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendPotion(fileName, targetPlayer, path, pathPrefix);
        sendHealthAndHunger(fileName, targetPlayer, path, pathPrefix);
        sendExp(fileName, targetPlayer, path, pathPrefix);
        sendActionBar(fileName, targetPlayer, replacePlayer, path, pathPrefix);
        sendBossBar(fileName, targetPlayer, replacePlayer, path, pathPrefix);
    }

    // 发送标题
    private static void sendTitle(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix == null ? "TITLE" : pathPrefix + ".TITLE");

        String mainTitle = CC.replaceAndTranslate(yaml.getString("MAIN-TITLE"), targetPlayer, replacePlayer);
        String subTitle = CC.replaceAndTranslate(yaml.getString("SUB-TITLE"), targetPlayer, replacePlayer);

        if (mainTitle.equals("") && subTitle.equals("")) return;

        int fadeIn = yaml.getInt("FADE-IN");
        int fadeOut = yaml.getInt("FADE-OUT");
        int stay = yaml.getInt("STAY");

        targetPlayer.sendTitle(mainTitle, subTitle, fadeIn, stay, fadeOut);
    }

    // 发送粒子效果
    private static void sendParticle(String fileName, Player targetPlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("PARTICLE").forEach(s -> {
            if (s == null || s.length() <= 1) return;

            String[] string = s.toUpperCase(Locale.ROOT).split(";");

            ParticleEffect particle = ParticleEffect.valueOf(string[0]);

            double x = Double.parseDouble(string[1]);
            double y = Double.parseDouble(string[2]);
            double z = Double.parseDouble(string[3]);
            int amount = Integer.parseInt(string[4]);

            boolean isNote = particle == ParticleEffect.NOTE;
            boolean allPlayer = !string[5].equals("PLAYER");
            boolean hasColor = !string[6].equals("FALSE");

            if (isNote && hasColor) {
                Bukkit.getLogger().warning(Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString() + "[Advanced Exp Booster] " + Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString() + "请注意，音符 (Note) 粒子效果并不支持自定义颜色! 已自动切换为随机颜色!");

                if (allPlayer)
                    new ParticleBuilder(particle, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .display();
                else
                    new ParticleBuilder(particle, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .display(targetPlayer);
                return;
            }

            if (hasColor) {
                if (allPlayer)
                    new ParticleBuilder(particle, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .setColor(Color.getColor(string[6]))
                            .display();
                else
                    new ParticleBuilder(particle, targetPlayer.getLocation())
                            .setOffsetX((float) x)
                            .setOffsetY((float) y)
                            .setOffsetZ((float) z)
                            .setAmount(amount)
                            .setColor(Color.getColor(string[6]))
                            .display(targetPlayer);
                return;
            }

            if (allPlayer)
                new ParticleBuilder(particle, targetPlayer.getLocation())
                        .setOffsetX((float) x)
                        .setOffsetY((float) y)
                        .setOffsetZ((float) z)
                        .setAmount(amount)
                        .display();
            else
                new ParticleBuilder(particle, targetPlayer.getLocation())
                        .setOffsetX((float) x)
                        .setOffsetY((float) y)
                        .setOffsetZ((float) z)
                        .setAmount(amount)
                        .display(targetPlayer);
        });
    }

    // 发送音效
    private static void sendSounds(String fileName, Player targetPlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("SOUNDS").forEach(s -> {
            if (s == null || s.length() <= 1) return;

            String[] string = s.toUpperCase(Locale.ROOT).split(";");

            Sound sound = Sound.valueOf(string[0]);
            int volume = Integer.parseInt(string[1]);
            int pitch = Integer.parseInt(string[2]);

            targetPlayer.playSound(targetPlayer.getLocation(), sound, volume, pitch);
        });
    }

    // 发送指令
    private static void sendCommands(String fileName, Player targetPlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix == null ? "COMMANDS" : pathPrefix + ".COMMANDS");

        yaml.getStringList("PLAYER").forEach(c -> {
            if (c == null || c.length() <= 1) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                String cmd = c.replaceAll("<player>", targetPlayer.getName());
                targetPlayer.performCommand(cmd);
            });
        });

        yaml.getStringList("CONSOLE").forEach(c -> {
            if (c == null || c.length() <= 1) return;
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

            Bukkit.getScheduler().runTask(plugin, () -> {
                String cmd = c.replaceAll("<player>", targetPlayer.getName());
                Bukkit.dispatchCommand(console, cmd);
            });
        });
    }

    // 发送消息
    private static void sendMessage(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("MESSAGE").forEach(m -> {
            if (m == null || m.length() <= 1) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                String msg = CC.replaceAndTranslate(m, targetPlayer, replacePlayer);
                targetPlayer.sendMessage(CC.translate(msg));
            });
        });
    }

    // 发送公告
    private static void sendAnnouncement(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("ANNOUNCEMENT").forEach(m -> {
            if (m == null || m.length() <= 1) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                String msg = CC.replaceAndTranslate(m, targetPlayer, replacePlayer);
                Bukkit.broadcastMessage(CC.translate(msg));
            });
        });
    }

    // 发送药水效果
    private static void sendPotion(String fileName, Player targetPlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        yaml.getStringList("EFFECTS").forEach(m -> {
            if (m == null || m.length() <= 1) return;

            String[] e = m.split(";");

            PotionEffectType effectType = PotionEffectType.getByName(e[0]);
            int duration = Integer.parseInt(e[1]);
            int amplifier = Integer.parseInt(e[2]);

            Bukkit.getScheduler().runTask(plugin, () -> targetPlayer.addPotionEffect(new PotionEffect(effectType, duration, amplifier)));
        });
    }

    // 回复血量以及饱食度
    private static void sendHealthAndHunger(String fileName, Player targetPlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        if (yaml.getDouble("HEALTH") != 0) {
            double playerHealth = targetPlayer.getHealth();
            double addedHealth = yaml.getDouble("HEALTH");

            targetPlayer.setHealth(Math.min(playerHealth + addedHealth, targetPlayer.getMaxHealth()));
        }

        if (yaml.getDouble("HUNGER") != 0) {
            int playerFoodLevel = targetPlayer.getFoodLevel();
            int addedFoodLevel = yaml.getInt("HUNGER");

            targetPlayer.setFoodLevel(Math.min(playerFoodLevel + addedFoodLevel, 20));
        }
    }

    // 给予EXP
    private static void sendExp(String fileName, Player targetPlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;

        Yaml yaml = new Yaml(fileName, path);
        yaml.setPathPrefix(pathPrefix);

        if (yaml.getDouble("EXP") == 0) return;

        ExpUtils.changeExp(targetPlayer, yaml.getInt("EXP"));
    }

    // 发送 Action Bar
    private static void sendActionBar(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;
        Yaml yaml = new Yaml(fileName, path);

        yaml.setPathPrefix(pathPrefix == null ? "ACTION-BAR" : pathPrefix + ".ACTION-BAR");

        int actionBarTime = yaml.getInt("TIME");
        String actionBarMessage = yaml.getString("MESSAGE");

        if (actionBarMessage.equals("") || actionBarTime == 0) return;

        // 由于 Action Bar 并没有具体的淡出淡入显示时间参数
        // 所以只能通过 Runnable 发送

        new BukkitRunnable() {
            int time = 0;
            @Override
            public void run() {
                if (time >= actionBarTime) {
                    cancel();
                    return;
                }

                time ++;

                TextComponent textComponent = new TextComponent(CC.replaceAndTranslate(yaml.getString("MESSAGE"), targetPlayer, replacePlayer));
                targetPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    // 发送 Boss Bar
    private static void sendBossBar(String fileName, Player targetPlayer, Player replacePlayer, String path, String pathPrefix) {
        path = path == null ? plugin.getDataFolder().toString() : plugin.getDataFolder() + "/" + path;
        Yaml yaml = new Yaml(fileName, path);

        yaml.setPathPrefix(pathPrefix == null ? "BOSS-BAR" : pathPrefix + ".BOSS-BAR");

        double bossBarTime = yaml.getDouble("TIME");
        String bossBarMessage = yaml.getString("MESSAGE");

        if (bossBarMessage.equals("") || bossBarTime == 0) return;

        String barColorString = yaml.getString("COLOR");
        String barStyleString = yaml.getString("STYLE");

        BarColor bossBarColor = barColorString.equals("RANDOM") ? BossBarRandomUtils.randomColor() : BarColor.valueOf(barColorString);
        BarStyle bossBarStyle = barStyleString.equals("RANDOM") ? BossBarRandomUtils.randomStyle() : BarStyle.valueOf(barStyleString);

        BossBar bossBar = Bukkit.createBossBar(CC.replaceAndTranslate(bossBarMessage, targetPlayer, replacePlayer), bossBarColor, bossBarStyle);

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
}
