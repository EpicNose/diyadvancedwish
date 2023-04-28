package me.twomillions.plugin.advancedwish.utils.scripts.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.annotations.JsInteropJavaType;
import me.twomillions.plugin.advancedwish.enums.scripts.ScriptSchedulerType;
import me.twomillions.plugin.advancedwish.interfaces.ScriptUtilsInterface;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 2000000
 * @date 2023/4/28
 */
@Getter
@Setter
@JsInteropJavaType
@SuppressWarnings("unused")
@Builder(setterPrefix = "set")
public class ScriptScheduler implements ScriptUtilsInterface {
    private static final JavaPlugin plugin = Main.getInstance();

    /**
     * ScriptSchedulers 列表。
     */
    @Getter private static final ConcurrentLinkedQueue<ScriptScheduler> scriptSchedulers = new ConcurrentLinkedQueue<>();

    /**
     * 运行任务前等待的 ticks (20 ticks = 1s)。
     */
    private final long delay;

    /**
     * 重复运行任务的间隔。
     */
    private final long period;

    /**
     * 任务代码。
     */
    private final Runnable runnable;

    /**
     * 任务类型。
     */
    private final ScriptSchedulerType scriptSchedulerType;

    /**
     * BukkitTask 对象。
     */
    @Builder.Default
    private BukkitTask bukkitTask = null;

    /**
     * 创建 BukkitTask 实例，并开启任务。
     */
    @Override
    public void register() {
        if (bukkitTask != null) {
            unregister();
        }

        switch (scriptSchedulerType) {
            case runTask:
                bukkitTask = Bukkit.getScheduler().runTask(plugin, runnable);
                break;

            case runTaskAsync:
                bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
                break;

            case runTaskTimer:
                bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
                break;

            case runTaskTimerAsync:
                bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
                break;

            case runTaskLater:
                bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
                break;

            case runTaskLaterAsync:
                bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
                break;

            default:
                throw new RuntimeException("Unknown ScriptSchedulerType: " + scriptSchedulerType + "!");
        }

        scriptSchedulers.add(this);
    }

    /**
     * 结束任务。
     */
    @Override
    public void unregister() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
    }
}
