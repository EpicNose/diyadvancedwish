package me.twomillions.plugin.advancedwish.utils.scripts.other;

import lombok.Builder;
import lombok.Getter;
import me.twomillions.plugin.advancedwish.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author 2000000
 * @date 2023/4/28
 */
@SuppressWarnings("unused")
@Builder(setterPrefix = "set")
public class ScriptListener {
    /**
     * ScriptListener 监听器列表。
     */
    @Getter private static final List<ScriptListener> scriptListeners = new ArrayList<>();

    /**
     * 监听事件类。
     */
    private final Class<? extends Event> eventClass;

    /**
     * 事件优先级。
     */
    private final EventPriority eventPriority;

    /**
     * 当监听器监听到事件时要执行的代码。
     */
    private final Consumer<Event> executor;

    /**
     * 是否传递已取消的事件。
     */
    private final boolean ignoreCancelled;

    /**
     * 实例。
     */
    private final Listener listener = new Listener() { };

    /**
     * 注册事件监听。
     */
    public void register() {
        scriptListeners.add(this);
        Bukkit.getPluginManager().registerEvent(eventClass, listener, eventPriority, (listener, event) -> executor.accept(event), Main.getInstance());
    }

    /**
     * 注销事件监听。
     */
    public void unregister() {
        scriptListeners.remove(this);
        HandlerList.unregisterAll(listener);
    }
}
