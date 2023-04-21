package me.twomillions.plugin.advancedwish.abstracts;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author 2000000
 * @date 2023/4/21
 */
@Getter @Setter
public abstract class AsyncEventAbstract extends Event implements Cancellable {
    private boolean cancelled = false;
    private HandlerList handlers = new HandlerList();

    /**
     * 构造器，异步事件。
     */
    public AsyncEventAbstract() {
        super(true);
    }
}
