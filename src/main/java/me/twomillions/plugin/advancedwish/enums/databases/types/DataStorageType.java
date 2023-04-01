package me.twomillions.plugin.advancedwish.enums.databases.types;

/**
 * @author 2000000
 * @date 2023/3/23
 */
public enum DataStorageType {

    /**
     * MongoDB - MongoDB 存储
     */
    MongoDB,

    /**
     * MySQL - MySQL 存储
     */
    MySQL,

    /**
     * Json - Json 存储
     */
    Json;

    /**
     * valueOf 忽略大小写。
     *
     * @param name name
     * @return DataStorageType
     */
    public static DataStorageType valueOfIgnoreCase(String name) {
        for (DataStorageType type : DataStorageType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant " + DataStorageType.class.getCanonicalName() + "." + name);
    }
}
