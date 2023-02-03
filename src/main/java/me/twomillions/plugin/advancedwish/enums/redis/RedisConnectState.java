package me.twomillions.plugin.advancedwish.enums.redis;

/**
 * @author 2000000
 * @date 2023/02/02
 */
public enum RedisConnectState {

    /**
     * TurnOff - 没有开启 Redis
     */
    TurnOff,

    /**
     * Connected - 已连接 Redis
     */
    Connected,

    /**
     * CannotConnect - 无法连接 Redis
     */
    CannotConnect
}
