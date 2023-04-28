package me.twomillions.plugin.advancedwish.utils.scripts.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.twomillions.plugin.advancedwish.annotations.JsInteropJavaType;
import me.twomillions.plugin.advancedwish.interfaces.ScriptUtilsInterface;
import me.twomillions.plugin.advancedwish.managers.register.RegisterManager;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;

/**
 * 允许使用 JavaScript 注册 Placeholder API 拓展。
 *
 * @author 2000000
 * @date 2023/4/28
 */
@Getter @Setter
@JsInteropJavaType
@SuppressWarnings("unused")
@Builder(setterPrefix = "set")
public class ScriptPlaceholder implements ScriptUtilsInterface {
    /**
     * ScriptPlaceholder 占位符列表。
     */
    @Getter private static final ConcurrentLinkedQueue<ScriptPlaceholder> scriptPlaceholders = new ConcurrentLinkedQueue<>();

    /**
     * 作者。
     */
    private final String author;

    /**
     * 版本。
     */
    private final String version;

    /**
     * 标识符。
     */
    private final String identifier;

    /**
     * 用于处理占位符执行的函数。
     */
    private final BiFunction<OfflinePlayer, String, String> executor;

    /**
     * PlaceholderExpansion 实例。
     */
    @Builder.Default
    private PlaceholderExpansion placeholderExpansion = null;

    /**
     * 注册 PlaceholderExpansion 实例。
     */
    @Override
    public void register() {
        if (placeholderExpansion == null) {
            toPlaceholderExpansion();
        }

        scriptPlaceholders.add(this);
        placeholderExpansion.register();
    }

    /**
     * 注销 PlaceholderExpansion 实例。
     * 低版本 Placeholder API 没有 unregister 方法，捕获异常以取消注销。
     */
    @Override
    public void unregister() {
        if (placeholderExpansion == null) {
            toPlaceholderExpansion();
        }

        try {
            placeholderExpansion.unregister();
            scriptPlaceholders.remove(this);
        } catch (Throwable throwable) {
            QuickUtils.sendConsoleMessage("&eScriptPlaceholder&c 注销异常，这是最新版吗? 请尝试更新它: &ehttps://www.spigotmc.org/resources/placeholderapi.6245/&c，已取消 &eScriptPlaceholder&c 注销。");
        }
    }

    /**
     * 转换 PlaceholderExpansion 实例。
     *
     * @throws RuntimeException 当服务器没有 Placeholder API 插件时
     */
    private void toPlaceholderExpansion() {
        if (!RegisterManager.isUsingPapi()) {
            throw new RuntimeException("You can not use ScriptPlaceholder, because this server isn't using the Placeholder API plugin!");
        }

        placeholderExpansion = new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return identifier;
            }

            @Override
            public @NotNull String getAuthor() {
                return author;
            }

            @Override
            public @NotNull String getVersion() {
                return version;
            }

            @Override
            public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
               return executor.apply(player, params);
            }
        };
    }
}
