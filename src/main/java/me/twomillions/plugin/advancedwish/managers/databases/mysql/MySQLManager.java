package me.twomillions.plugin.advancedwish.managers.databases.mysql;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.enums.databases.status.AuthStatus;
import me.twomillions.plugin.advancedwish.enums.databases.status.ConnectStatus;
import me.twomillions.plugin.advancedwish.enums.databases.status.CustomUrlStatus;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.interfaces.DatabasesInterface;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 2000000
 * @date 2023/3/26
 */
@Getter
@Setter
public class MySQLManager implements DatabasesInterface {
    private volatile String jdbcUrlString;
    private volatile BasicDataSource dataSource = new BasicDataSource();

    private volatile String ip;
    private volatile String port;
    private volatile String username;
    private volatile String password;
    private volatile String driver;

    private volatile AuthStatus authStatus;
    private volatile CustomUrlStatus customUrlStatus;
    private volatile ConnectStatus connectStatus = ConnectStatus.TurnOff;

    /**
     * 根据指定的 YAML 配置，初始化数据库连接。
     *
     * @param yaml 包含数据库连接信息的 YAML 配置
     * @return 数据库连接状态
     */
    @Override
    public ConnectStatus setup(Yaml yaml) {
        // 设置MySQL登录信息
        setIp(yaml.getString("MYSQL.IP"));
        setPort(yaml.getString("MYSQL.PORT"));
        setUsername(yaml.getString("MYSQL.AUTH.USER"));
        setPassword(yaml.getString("MYSQL.AUTH.PASSWORD"));
        setDriver(yaml.getString("MYSQL.DRIVER"));

        // 检查是否使用自定义的MySQL连接URL
        setCustomUrlStatus("".equals(yaml.getString("MYSQL.CUSTOM-URL")) ? CustomUrlStatus.TurnOff : CustomUrlStatus.TurnOn);

        // 根据是否使用自定义URL设置连接URL
        if (getCustomUrlStatus() == CustomUrlStatus.TurnOn) {
            setJdbcUrlString(yaml.getString("MYSQL.CUSTOM-URL"));
        } else {
            setJdbcUrlString("jdbc:mysql://" + getIp() + ":" + getPort() + "/advancedwish");
        }

        // 检查MySQL连接状态
        try {
            getDataSource().setDriverClassName(getDriver());
            getDataSource().setUrl(getJdbcUrlString());
            getDataSource().setUsername(getUsername());
            getDataSource().setPassword(getPassword());
            getDataSource().setInitialSize(5);

            QuickUtils.sendConsoleMessage("&aAdvanced Wish 已成功建立与 MySQL 的连接!");

            setConnectStatus(ConnectStatus.Connected);
        } catch (Exception exception) {
            exception.printStackTrace();

            QuickUtils.sendConsoleMessage("&c您打开了 MySQL 数据库选项，但 Advanced Wish 未能正确连接到 MySQL，请检查 MySQL 服务状态，即将关闭服务器!");

            setConnectStatus(ConnectStatus.CannotConnect);

            Bukkit.shutdown();
        }

        return getConnectStatus();
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
        try (Connection connection = getDataSource().getConnection()) {
            checkCollectionType(key, databaseCollectionType);
            checkColumn(key, databaseCollectionType);

            String query = "SELECT COALESCE((SELECT `" + key + "` FROM `" + databaseCollectionType + "` WHERE uuid = ?), ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, uuid);
            preparedStatement.setObject(2, defaultValue);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            Object value = resultSet.getObject(1);

            if (value == null) {
                value = defaultValue;
                update(uuid, key, defaultValue, databaseCollectionType);
            }

            return value;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return defaultValue;
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
        try (Connection connection = getDataSource().getConnection()) {
            checkCollectionType(key, databaseCollectionType);
            checkColumn(key, databaseCollectionType);

            String query = "SELECT COALESCE((SELECT `" + key + "` FROM `" + databaseCollectionType + "` WHERE uuid = ?), ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, String.join(",", defaultValue));

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String value = resultSet.getString(1);

            if (value == null || value.isEmpty()) {
                update(uuid, key, defaultValue, databaseCollectionType);
                return defaultValue;
            }

            return new ConcurrentLinkedQueue<>(Arrays.asList(value.split(",")));
        } catch (SQLException exception) {
            exception.printStackTrace();
            return defaultValue;
        }
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
        try (Connection connection = getDataSource().getConnection()) {
            checkCollectionType(key, databaseCollectionType);
            checkColumn(key, databaseCollectionType);

            String query = "INSERT INTO `" + databaseCollectionType + "` (uuid, `" + key + "`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `" + key + "`=?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, uuid);
            preparedStatement.setObject(2, value);
            preparedStatement.setObject(3, value);
            preparedStatement.executeUpdate();

            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * 执行 SQL 语句。
     *
     * @param sql 要执行的 SQL 语句。
     */
    public boolean executeStatement(String sql) {
        try (Connection connection = getDataSource().getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查指定集合类型的表是否存在，如果不存在，则创建该表。
     *
     * @param key 查询的 Key
     * @param databaseCollectionType 查询的集合
     */
    private void checkCollectionType(String key, DatabaseCollectionType databaseCollectionType) {
        try (Connection connection = getDataSource().getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet tables = databaseMetaData.getTables(null, null, databaseCollectionType.toString(), null);

            if (!tables.next()) {
                String createTableQuery = "CREATE TABLE `" + databaseCollectionType + "` (`uuid` VARCHAR(36) NOT NULL, `" + key + "` TEXT DEFAULT NULL, PRIMARY KEY (`uuid`))";
                PreparedStatement createTableStatement = connection.prepareStatement(createTableQuery);
                createTableStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 检查指定集合类型的表是否包含指定的列，如果不包含，则为该表添加该列。
     *
     * @param key 查询的 Key
     * @param databaseCollectionType 查询的集合
     */
    private void checkColumn(String key, DatabaseCollectionType databaseCollectionType) {
        try (Connection connection = getDataSource().getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet columns = databaseMetaData.getColumns(null, null, String.valueOf(databaseCollectionType), key);

            if (!columns.next()) {
                String alterTableQuery = "ALTER TABLE `" + databaseCollectionType + "` ADD `" + key + "` TEXT DEFAULT NULL";
                PreparedStatement alterTableStatement = connection.prepareStatement(alterTableQuery);
                alterTableStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
