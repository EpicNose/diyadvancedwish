package me.twomillions.plugin.advancedwish.managers.databases;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.databases.types.DataStorageType;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.interfaces.DatabasesInterface;
import me.twomillions.plugin.advancedwish.managers.databases.mongo.MongoManager;
import me.twomillions.plugin.advancedwish.managers.databases.mysql.MySQLManager;
import me.twomillions.plugin.advancedwish.utils.exceptions.ExceptionUtils;

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
    @Setter @Getter private static MongoManager mongoManager = new MongoManager();

    /**
     * DatabasesManager.getMySQLManager().
     */
    @Setter @Getter private static MySQLManager mySQLManager = new MySQLManager();

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

            default:
                return ExceptionUtils.throwUnknownDataStoreType();
        }
    }
}
