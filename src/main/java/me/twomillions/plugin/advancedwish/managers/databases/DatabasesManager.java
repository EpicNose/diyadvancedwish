package me.twomillions.plugin.advancedwish.managers.databases;

import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.databases.DataStorageType;

/**
 * @author 2000000
 * @date 2023/3/23
 */
public class DatabasesManager {
    /**
     * DataStorageType.
     */
    @Setter @Getter private static DataStorageType dataStorageType;
}
