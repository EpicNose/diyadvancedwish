package me.twomillions.plugin.advancedwish.managers.databases.json;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.interfaces.DatabasesInterface;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 该类实现 {@link DatabasesInterface}，处理 Json 操作。
 *
 * @author 2000000
 * @date 2023/4/1
 */
public class JsonManager implements DatabasesInterface {
    /**
     * 根据指定的 YAML 配置，初始化。
     *
     * @param yaml YAML
     * @return 无论如何都为 true
     */
    @Override
    public Object setup(Yaml yaml) {
        return true;
    }

    /**
     * 根据给定的 UUID、Key 和默认值获取对应的值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 标识符
     * @param key 查询的 Key
     * @param defaultValue 查询的默认值
     * @param databaseCollection 查询的集合
     * @return 对应的值
     */
    @Override
    public Object getOrDefault(String uuid, String key, Object defaultValue, String databaseCollection) {
        String path = getPath(databaseCollection);
        return ConfigManager.createJson(uuid, path, true, false).getOrDefault(key, defaultValue);
    }

    /**
     * 根据给定的 UUID、Key 和默认值获取对应的 List 值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 标识符
     * @param key 查询的 Key
     * @param defaultValue 查询的默认值
     * @param databaseCollection 查询的集合
     * @return 对应的 List 值
     */
    @Override
    public ConcurrentLinkedQueue<String> getOrDefaultList(String uuid, String key, ConcurrentLinkedQueue<String> defaultValue, String databaseCollection) {
        String path = getPath(databaseCollection);
        return new ConcurrentLinkedQueue<>(ConfigManager.createJson(uuid, path, true, false).getOrDefault(key, defaultValue));
    }

    /**
     * 根据给定的 UUID、Key 更新数据值，若未找到则插入数据值并返回。
     *
     * @param uuid 标识符
     * @param key 查询的 Key
     * @param value 数据值
     * @param databaseCollection 数据存储的集合
     * @return 是否成功更新
     */
    @Override
    public boolean update(String uuid, String key, Object value, String databaseCollection) {
        String path = getPath(databaseCollection);
        ConfigManager.createJson(uuid, path, true, false).set(key, value);
        return true;
    }

    /**
     * 获取指定集合类型的所有数据。
     *
     * @param databaseCollection 查询的集合
     * @return 以 Map 的形式返回所有数据，其中 Map 的 Key 是 UUID，value 是一个包含键值对的 Map
     */
    public Map<String, Map<String, Object>> getAllData(String databaseCollection) {
        String path = getPath(databaseCollection);

        Map<String, Map<String, Object>> result = new HashMap<>();

        ConfigManager.getAllFileNames(path).forEach(fileName -> {
            Json json = ConfigManager.createJson(fileName, path, true, false);

            Set<String> jsonKeySet = json.keySet();
            Map<String, Object> subMap = new HashMap<>();

            for (String key : jsonKeySet) {
                Object value = json.get(key);
                subMap.put(key, value);
            }

            result.put(fileName.split(ConstantsUtils.JSON_FILE_EXTENSION)[0], subMap);
        });


        return result;
    }

    /**
     * 获取数据集合对应的文件路径。
     *
     * @param databaseCollection 集合
     * @return 路径
     */
    public String getPath(String databaseCollection) {
        switch (databaseCollection) {
            case ConstantsUtils.PLAYER_LOGS_COLLECTION_NAME:
                return Main.getLogsPath();

            case ConstantsUtils.PLAYER_GUARANTEED_COLLECTION_NAME:
                return Main.getGuaranteedPath();

            default:
                return ExceptionUtils.throwUnknowndatabaseCollection();
        }
    }
}
