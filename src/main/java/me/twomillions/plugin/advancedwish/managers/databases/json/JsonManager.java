package me.twomillions.plugin.advancedwish.managers.databases.json;

import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.interfaces.DatabasesInterface;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
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
     * @param uuid 查询的 UUID
     * @param key 查询的 Key
     * @param defaultValue 查询的默认值
     * @param databaseCollectionType 查询的集合
     * @return 对应的值
     */
    @Override
    public Object getOrDefault(String uuid, String key, Object defaultValue, DatabaseCollectionType databaseCollectionType) {
        String path = getPath(databaseCollectionType);
        return ConfigManager.createJson(uuid, path, true, false).getOrDefault(key, defaultValue);
    }

    /**
     * 根据给定的 UUID、Key 和默认值获取对应的 List 值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 查询的 UUID
     * @param key 查询的 Key
     * @param defaultValue 查询的默认值
     * @param databaseCollectionType 查询的集合
     * @return 对应的 List 值
     */
    @Override
    public ConcurrentLinkedQueue<String> getOrDefaultList(String uuid, String key, ConcurrentLinkedQueue<String> defaultValue, DatabaseCollectionType databaseCollectionType) {
        String path = getPath(databaseCollectionType);
        return new ConcurrentLinkedQueue<>(ConfigManager.createJson(uuid, path, true, false).getOrDefault(key, defaultValue));
    }

    /**
     * 更新玩家数据。
     *
     * @param uuid 玩家的 UUID
     * @param key 查询的 Key
     * @param value 数据的值
     * @param databaseCollectionType 查询的集合
     */
    @Override
    public boolean update(String uuid, String key, Object value, DatabaseCollectionType databaseCollectionType) {
        String path = getPath(databaseCollectionType);
        ConfigManager.createJson(uuid, path, true, false).set(key, value);
        return true;
    }

    /**
     * 获取指定集合类型的所有数据。
     *
     * @param databaseCollectionType 查询的集合
     * @return 以 Map 的形式返回所有数据，其中 Map 的 Key 是 UUID，value 是一个包含键值对的 Map
     */
    public Map<String, Map<String, Object>> getAllData(DatabaseCollectionType databaseCollectionType) {
        String path = getPath(databaseCollectionType);

        Map<String, Map<String, Object>> result = new HashMap<>();

        ConfigManager.getAllFileNames(path).forEach(fileName -> {
            Json json = ConfigManager.createJson(fileName, path, true, false);

            Set<String> jsonKeySet = json.keySet();
            Map<String, Object> subMap = new HashMap<>();

            for (String key : jsonKeySet) {
                Object value = json.get(key);
                subMap.put(key, value);
            }

            result.put(fileName.split(ConstantsUtils.JSON_SUFFIX)[0], subMap);
        });


        return result;
    }

    /**
     * 获取数据集合对应的文件路径。
     *
     * @param databaseCollectionType 集合
     * @return 路径
     */
    public String getPath(DatabaseCollectionType databaseCollectionType) {
        switch (databaseCollectionType) {
            case PlayerLogs:
                return Main.getLogsPath();

            case PlayerGuaranteed:
                return Main.getGuaranteedPath();

            default:
                return ExceptionUtils.throwUnknownDatabaseCollectionType();
        }
    }
}
