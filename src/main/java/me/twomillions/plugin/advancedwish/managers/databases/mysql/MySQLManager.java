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
import java.util.*;
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

            QuickUtils.sendConsoleMessage("&a已成功建立与 &eMySQL&a 的连接!");

            setConnectStatus(ConnectStatus.Connected);
        } catch (Exception exception) {
            exception.printStackTrace();

            QuickUtils.sendConsoleMessage("&c您打开了 &eMySQL&c 数据库选项，但未能正确连接到 &eMySQL&c，请检查 &eMySQL&c 服务状态，即将关闭服务器!");

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

            return QuickUtils.stringToList(value);
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

            if (value instanceof Collections) {
                value = QuickUtils.listToString((Collection<?>) value);
            }

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
     * 获取指定集合类型的所有数据。
     *
     * @param databaseCollectionType 查询的集合
     * @return 以 Map 的形式返回所有数据，其中 Map 的 Key 是 UUID，value 是一个包含键值对的 Map
     */
    @Override
    public Map<String, Map<String, Object>> getAllData(DatabaseCollectionType databaseCollectionType) {
        try (Connection connection = getDataSource().getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet columns = databaseMetaData.getColumns(null, null, databaseCollectionType.toString(), null);

            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                columnNames.add(columnName);
            }

            if (columnNames.isEmpty()) {
                return new HashMap<>();
            }

            String query = "SELECT uuid, `" + String.join("`, `", columnNames) + "` FROM `" + databaseCollectionType + "`";

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            List<String> dataColumnNames = new ArrayList<>();

            for (int i = 2; i <= columnCount; i++) {
                dataColumnNames.add(metadata.getColumnName(i));
            }

            Map<String, Map<String, Object>> data = new HashMap<>();

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                Map<String, Object> rowData = new HashMap<>();

                for (String columnName : dataColumnNames) {
                    Object value = resultSet.getObject(columnName);
                    rowData.put(columnName, value);
                }

                data.put(uuid, rowData);
            }

            Map<String, Map<String, Object>> correctedData = new HashMap<>();
            for (String columnName : dataColumnNames) {
                if (!columnName.equals("uuid")) {
                    Map<String, Object> columnData = new HashMap<>();

                    for (String uuid : data.keySet()) {
                        columnData.put(uuid, data.get(uuid).get(columnName));
                    }

                    correctedData.put(columnName, columnData);
                }
            }

            return correctedData;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return new HashMap<>();
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
     * 检查是否存在指定集合。
     * 如果不存在，则创建新集合，其中包含一个主键 uuid 和一个指定的列。
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
     * 检查是否存在指定的列。如果不存在，则在表格中添加新列。
     *
     * @param key 查询的 Key
     * @param databaseCollectionType 查询的集合
     */
    private void checkColumn(String key, DatabaseCollectionType databaseCollectionType) {
        try (Connection connection = getDataSource().getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet columns = databaseMetaData.getColumns(null, null, databaseCollectionType.toString(), key);

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
