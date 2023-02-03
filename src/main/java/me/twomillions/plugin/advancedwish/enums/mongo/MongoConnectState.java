package me.twomillions.plugin.advancedwish.enums.mongo;

/**
 * @author 2000000
 * @date 2023/02/02
 */
public enum MongoConnectState {

    /**
     * TurnOff - 没有开启 Mongo
     */
    TurnOff,

    /**
     * Connected - 已连接 Mongo
     */
    Connected,

    /**
     * CannotConnect - 无法连接 Mongo
     */
    CannotConnect
}
