/**
 * 初始化 Script / 开启插件 / 指令插件 时触发 scriptSetup 函数。
 *
 * 卸载时触发 onDisable 函数。
 */
function scriptSetup() {
    registerListener();
}

/**
 * 使用 ScriptListener 注册监听器。
 */
function registerListener() {
    var asyncPlayerWishEventScriptListener = ScriptListener
        .builder()
        /**
         * 设置监听事件。
         */
        .setEventClass(Packages.me.twomillions.plugin.advancedwish.api.AsyncPlayerWishEvent)
        /**
         * 设置监听权重。
         */
        .setEventPriority(Packages.org.bukkit.event.EventPriority.NORMAL)
        /**
         * 设置是否传递已取消的事件。
         */
        .setIgnoreCancelled(false)
        /**
         * 当监听器监听到事件时要执行的代码。
         * 此处代码执行线程与触发事件的线程有关，而非强制同步或异步。
         * 比如此处监听 AsyncPlayerWishEvent，它是异步线程执行的。
         */
        .setExecutor(
            function(event) {
                QuickUtils.sendConsoleMessage("&a这里是 &e脚本监听器&a! 已触发 &eAsyncPlayerWishEvent&a! 这是同步的吗: &e" + Bukkit.isPrimaryThread() + "&a，您可以在 &e" + Main.getScriptPath() + " &a文件夹下看到脚本文件!");
            }
        )
        .build();

    /**
     * 注册。
     */
    asyncPlayerWishEventScriptListener.register();

    QuickUtils.sendConsoleMessage("&a这里是 &e脚本监听器&a! 已注册 &eAsyncPlayerWishEvent&a 事件监听!" + "&a，您可以在 &e" + Main.getScriptPath()  + " &a文件夹下看到脚本文件!");
}