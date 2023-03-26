package me.twomillions.plugin.advancedwish.utils.scripts;

import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import me.twomillions.plugin.advancedwish.utils.random.RandomUtils;
import org.bukkit.entity.Player;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;

/**
 * JavaScript 工具类。
 *
 * @author 2000000
 * @date 2023/3/2
 */
public class ScriptUtils {
    /**
     * 分析 JavaScript 表达式，返回结果字符串。
     *
     * @param string JavaScript 表达式
     * @param player 表达式目标玩家
     * @param params 传递给 JavaScript 的可选参数
     * @return 结果的字符串表示。如果计算失败，则返回原始输入字符串
     */
    public static String eval(String string, Player player, Object... params) {
        string = QuickUtils.toPapi(QuickUtils.replaceTranslate(string, player), player);

        ScriptEngineManager manager = new ScriptEngineManager();
        javax.script.ScriptEngine engine = manager.getEngineByName("JavaScript");

        engine.put("RandomUtils", new RandomUtils<>());
        engine.put("method", new MethodFunctions(player));

        File pluginFolder = Main.getInstance().getDataFolder().getParentFile();
        String pluginFolderPath = pluginFolder.getAbsolutePath();

        engine.put("_player_", player);
        engine.put("_pluginPath_", pluginFolderPath);

        for (int i = 0; i < params.length; i += 2) {
            engine.put(params[i].toString(), params[i + 1]);
        }

        Object result;

        try {
            result = engine.eval(string);
        } catch (ScriptException scriptException) {
            return string;
        }

        return result == null ? "" : QuickUtils.toPlainString(result.toString());
    }
}
