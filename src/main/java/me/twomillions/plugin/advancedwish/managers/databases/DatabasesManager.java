package me.twomillions.plugin.advancedwish.managers.databases;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.databases.types.DataStorageType;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.interfaces.DatabasesInterface;
import me.twomillions.plugin.advancedwish.managers.databases.json.JsonManager;
import me.twomillions.plugin.advancedwish.managers.databases.mongo.MongoManager;
import me.twomillions.plugin.advancedwish.managers.databases.mysql.MySQLManager;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 2000000
 * @date 2023/3/23
 */
public class DatabasesManager implements DatabasesInterface {
    /**
     * DataStorageType.
     */
    @Setter @Getter private static DataStorageType dataStorageType;

    /**
     * DatabasesManager.
     */
    @Getter @Setter private static DatabasesManager databasesManager = new DatabasesManager();

    /**
     * DatabasesManager.getMongoManager().
     */
    @Getter @Setter private static MongoManager mongoManager = new MongoManager();

    /**
     * DatabasesManager.getMySQLManager().
     */
    @Getter @Setter private static MySQLManager mySQLManager = new MySQLManager();

    /**
     * DatabasesManager.getJsonManager().
     */
    @Getter @Setter private static JsonManager jsonManager = new JsonManager();

    /**
     * 根据指定的 YAML 配置，初始化数据库连接。
     *
     * @param yaml 包含数据库连接信息的 YAML 配置
     * @return 数据库连接状态
     */
    @Override
    public Object setup(Yaml yaml) {
        switch (getDataStorageType()) {
            case MongoDB:
                return getMongoManager().setup(yaml);

            case MySQL:
                return getMySQLManager().setup(yaml);

            case Json:
                return getJsonManager().setup(yaml);

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
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
        switch (getDataStorageType()) {
            case MongoDB:
                return getMongoManager().getOrDefault(uuid, key, defaultValue, databaseCollectionType);

            case MySQL:
                return getMySQLManager().getOrDefault(uuid, key, defaultValue, databaseCollectionType);

            case Json:
                return getJsonManager().getOrDefault(uuid, key, defaultValue, databaseCollectionType);

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
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
        switch (getDataStorageType()) {
            case MongoDB:
                return getMongoManager().getOrDefaultList(uuid, key, defaultValue, databaseCollectionType);

            case MySQL:
                return getMySQLManager().getOrDefaultList(uuid, key, defaultValue, databaseCollectionType);

            case Json:
                return getJsonManager().getOrDefaultList(uuid, key, defaultValue, databaseCollectionType);

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
    }

    /**
     * 更新玩家数据。
     *
     * @param uuid 玩家的 UUID
     * @param key 查询的 Key
     * @param value 数据的值
     * @param databaseCollectionType 数据存储的集合
     */
    @Override
    public boolean update(String uuid, String key, Object value, DatabaseCollectionType databaseCollectionType) {
        switch (getDataStorageType()) {
            case MongoDB:
                return getMongoManager().update(uuid, key, value, databaseCollectionType);

            case MySQL:
                return getMySQLManager().update(uuid, key, value, databaseCollectionType);

            case Json:
                return getJsonManager().update(uuid, key, value, databaseCollectionType);

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
    }

    /**
     * 获取指定集合类型的所有数据。
     *
     * @param databaseCollectionType 查询的集合
     * @return 以 Map 的形式返回所有数据，其中 Map 的 Key 是 UUID，value 是一个包含键值对的 Map
     */
    @Override
    public Map<String, Map<String, Object>> getAllData(DatabaseCollectionType databaseCollectionType) {
        switch (getDataStorageType()) {
            case MongoDB:
                return getMongoManager().getAllData(databaseCollectionType);

            case MySQL:
                return getMySQLManager().getAllData(databaseCollectionType);

            case Json:
                return getJsonManager().getAllData(databaseCollectionType);

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
    }

    /**
     * 数据迁移。
     *
     * @param yaml 包含数据库连接信息的 YAML 配置
     * @param type 原存储类型
     * @param type1 新存储类型
     * @return 是否成功迁移
     */
    public static boolean dataMigration(Yaml yaml, DataStorageType type, DataStorageType type1) {
        try {
            Map<String, Map<String, Object>> playerLogs = Collections.emptyMap();
            Map<String, Map<String, Object>> playerGuaranteed= Collections.emptyMap();

            switch (type) {
                case MongoDB:
                    getMongoManager().setup(yaml);
                    playerLogs = getMongoManager().getAllData(DatabaseCollectionType.PlayerLogs);
                    playerGuaranteed = getMongoManager().getAllData(DatabaseCollectionType.PlayerGuaranteed);
                    break;

                case MySQL:
                    getMySQLManager().setup(yaml);
                    playerLogs = getMySQLManager().getAllData(DatabaseCollectionType.PlayerLogs);
                    playerGuaranteed = getMySQLManager().getAllData(DatabaseCollectionType.PlayerGuaranteed);
                    System.out.println(playerGuaranteed);
                    break;

                case Json:
                    getJsonManager().setup(yaml);
                    playerLogs = getJsonManager().getAllData(DatabaseCollectionType.PlayerLogs);
                    playerGuaranteed = getJsonManager().getAllData(DatabaseCollectionType.PlayerGuaranteed);
                    break;

                default:
                    ExceptionUtils.throwUnknownDataStoreType();
                    break;
            }

            if (playerLogs.isEmpty() && playerGuaranteed.isEmpty()) {
                return false;
            }

            switch (type1) {
                case MongoDB:
                    getMongoManager().setup(yaml);
                    getMongoManager().insertAllData(DatabaseCollectionType.PlayerLogs, playerLogs);
                    getMongoManager().insertAllData(DatabaseCollectionType.PlayerGuaranteed, playerGuaranteed);
                    break;

                case MySQL:
                    getMySQLManager().setup(yaml);
                    getMySQLManager().insertAllData(DatabaseCollectionType.PlayerLogs, playerLogs);
                    getMySQLManager().insertAllData(DatabaseCollectionType.PlayerGuaranteed, playerGuaranteed);
                    break;

                case Json:
                    getJsonManager().setup(yaml);
                    getJsonManager().insertAllData(DatabaseCollectionType.PlayerLogs, playerLogs);
                    getJsonManager().insertAllData(DatabaseCollectionType.PlayerGuaranteed, playerGuaranteed);
                    break;

                default:
                    ExceptionUtils.throwUnknownDataStoreType();
                    break;
            }

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
