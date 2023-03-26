package me.twomillions.plugin.advancedwish.managers.databases;

import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.databases.types.DataStorageType;
import me.twomillions.plugin.advancedwish.managers.databases.mongo.MongoManager;
import me.twomillions.plugin.advancedwish.managers.databases.mysql.MySQLManager;

/**
 * @author 2000000
 * @date 2023/3/23
 */
public class DatabasesManager {
    /**
     * DataStorageType.
     */
    @Setter @Getter private static DataStorageType dataStorageType;

    /**
     * DatabasesManager.getMongoManager().
     */
    @Setter @Getter private static MongoManager mongoManager = new MongoManager();

    /**
     * DatabasesManager.getMySQLManager().
     */
    @Setter @Getter private static MySQLManager mySQLManager = new MySQLManager();
}
