package me.twomillions.plugin.advancedwish.interfaces;

import de.leonhard.storage.Yaml;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 数据库接口，提供常用的数据库操作方法。
 *
 * @author 2000000
 * @date 2023/3/26
 */
public interface DatabasesInterface {
    /**
     * 根据指定的 YAML 配置，初始化数据库连接。
     *
     * @param yaml 包含数据库连接信息的 YAML 配置
     * @return 数据库连接状态
     */
    Object setup(Yaml yaml);

    /**
     * 根据给定的 UUID、Key 和默认值获取对应的值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 查询的 UUID
     * @param key 查询的 Key
     * @param defaultValue 查询的默认值
     * @param databaseCollectionType 查询的集合
     * @return 对应的值
     */
    Object getOrDefault(String uuid, String key, Object defaultValue, DatabaseCollectionType databaseCollectionType);

    /**
     * 根据给定的 UUID、Key 和默认值获取对应的 List 值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 查询的 UUID
     * @param key 查询的 Key
     * @param defaultValue 查询的默认值
     * @param databaseCollectionType 查询的集合
     * @return 对应的 List 值
     */
    Object getOrDefaultList(String uuid, String key, ConcurrentLinkedQueue<String> defaultValue, DatabaseCollectionType databaseCollectionType);

    /**
     * 更新玩家数据。
     *
     * @param uuid 玩家的 UUID
     * @param key 查询的 Key
     * @param value 数据的值
     * @param databaseCollectionType 数据存储的集合
     */
    boolean update(String uuid, String key, Object value, DatabaseCollectionType databaseCollectionType);

    /**
     * 获取指定集合类型的所有数据。
     *
     * @param databaseCollectionType 查询的集合
     * @return 以 Map 的形式返回所有数据，其中 Map 的 Key 是 UUID，value 是一个包含键值对的 Map
     */
    Map<String, Map<String, Object>> getAllData(DatabaseCollectionType databaseCollectionType);

    /**
     * 将指定集合类型的所有数据插入到数据库中。
     *
     * @param databaseCollectionType 数据存储的集合
     * @param data 要插入的数据，以 Map 的形式传递，其中 Map 的 Key 是 UUID，value 是一个包含键值对的 Map
     */
    default void insertAllData(DatabaseCollectionType databaseCollectionType, Map<String, Map<String, Object>> data) {
        long count = data.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .map(keyData -> new Object[] { entry.getKey(), keyData.getKey(), keyData.getValue() })
                )
                .peek(values -> {
                    update((String) values[0], (String) values[1], values[2], databaseCollectionType);
                    QuickUtils.sendConsoleMessage("&a插入数据: UUID: " + values[0] + ", key: " + values[1] + ", value: " + values[2]);
                })
                .count();

        if (count != 0) {
            QuickUtils.sendConsoleMessage("&a迁移完毕，总迁移数据: " + count + " 个。");
        }
    }
}
