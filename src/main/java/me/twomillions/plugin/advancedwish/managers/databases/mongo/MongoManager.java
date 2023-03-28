package me.twomillions.plugin.advancedwish.managers.databases.mongo;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.Setter;
import me.twomillions.plugin.advancedwish.utils.others.ConstantsUtils;
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.databases.status.DataTransformationStatus;
import me.twomillions.plugin.advancedwish.enums.databases.status.AuthStatus;
import me.twomillions.plugin.advancedwish.enums.databases.types.DatabaseCollectionType;
import me.twomillions.plugin.advancedwish.enums.databases.status.ConnectStatus;
import me.twomillions.plugin.advancedwish.enums.databases.status.CustomUrlStatus;
import me.twomillions.plugin.advancedwish.interfaces.DatabasesInterface;
import me.twomillions.plugin.advancedwish.managers.config.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.texts.QuickUtils;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MongoDB 操作类。
 *
 * @author 2000000
 * @date 2023/1/8 21:23
 */
@Getter
@Setter
public class MongoManager implements DatabasesInterface {
     private volatile MongoClient mongoClient;
     private volatile DBCollection dbCollection;
     private volatile MongoDatabase mongoDatabase;
     private volatile String mongoClientUrlString;
     private volatile MongoClientURI mongoClientUrl;

    private volatile String ip;
    private volatile String port;
    private volatile String username;
    private volatile String password;

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
        // 设置MongoDB登录信息
        setIp(yaml.getString("MONGO.IP"));
        setPassword(yaml.getString("MONGO.PORT"));
        setUsername(yaml.getString("MONGO.AUTH.USER"));
        setPassword(yaml.getString("MONGO.AUTH.PASSWORD"));

        // 检查是否使用自定义的MongoDB连接URL
        setCustomUrlStatus("".equals(yaml.getString("MONGO.CUSTOM-URL")) ? CustomUrlStatus.TurnOff : CustomUrlStatus.TurnOn);

        // 根据是否使用自定义URL设置连接URL
        if (getCustomUrlStatus() == CustomUrlStatus.TurnOn) {
            setMongoClientUrlString(yaml.getString("MONGO.CUSTOM-URL"));
        } else {
            if ("".equals(getUsername()) || "".equals(getPassword())) {
                setAuthStatus(AuthStatus.TurnOff);
                setMongoClientUrlString("mongodb://" + getIp() + ":" + getPort() + "/AdvancedWish");
            } else {
                setAuthStatus(AuthStatus.UsingAuth);
                setMongoClientUrlString("mongodb://" + getUsername() + ":" + getPassword() + "@" + getIp() + ":" + getPort() + "/AdvancedWish");

                QuickUtils.sendConsoleMessage("&aAdvanced Wish 检查到Mongo开启身份验证，已设置身份验证信息!");
            }
        }

        // 检查MongoDB连接状态
        try {
            setMongoClientUrl(new MongoClientURI(getMongoClientUrlString()));
            setMongoClient(new MongoClient(getMongoClientUrl()));
            setMongoDatabase(getMongoClient().getDatabase("AdvancedWish"));

            QuickUtils.sendConsoleMessage("&aAdvanced Wish 已成功建立与MongoDB的连接!");

            setConnectStatus(ConnectStatus.Connected);
        } catch (Exception exception) {
            QuickUtils.sendConsoleMessage("&c您打开了MongoDB数据库选项，但Advanced Wish未能正确连接到MongoDB，请检查MongoDB服务状态，即将关闭服务器!");

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
        MongoCollection<Document> collection = getMongoDatabase().getCollection(databaseCollectionType.toString());

        Document filter = new Document("uuid", uuid);
        Document document = collection.find(filter).first();

        // 如果没有找到，则插入默认值
        if (document == null) {
            document = new Document("uuid", uuid).append(key, defaultValue);
            collection.insertOne(document);
            return defaultValue;
        }

        Object value = document.get(key);

        // 如果找到的值为 null，则更新为默认值
        if (value == null) {
            collection.updateOne(filter, new Document("$set", new Document(key, defaultValue)));
            return defaultValue;
        }

        return value;
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
        MongoCollection<Document> collection = getMongoDatabase().getCollection(databaseCollectionType.toString());

        Document filter = new Document("uuid", uuid);
        Document document = collection.find(filter).first();

        // 如果没有找到，则插入默认值
        if (document == null) {
            document = new Document("uuid", uuid).append(key, defaultValue);
            collection.insertOne(document);
            return defaultValue;
        }

        ConcurrentLinkedQueue<String> value = new ConcurrentLinkedQueue<>(document.getList(key, String.class));

        // 如果找到的值为 null，则更新为默认值
        if (value.size() == 0) {
            collection.updateOne(filter, new Document("$set", new Document(key, defaultValue)));
            return defaultValue;
        }

        return value;
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
        MongoCollection<Document> mongoCollection = getMongoDatabase().getCollection(databaseCollectionType.toString());
        Document filter = new Document("uuid", uuid);
        Document update = new Document("$set", new Document(key, value));
        UpdateOptions options = new UpdateOptions().upsert(true);
        mongoCollection.updateOne(filter, update, options);

        return true;
    }

    /**
     * 将 JSON 转换为 MongoDB 数据。
     *
     * @param yaml 包含转换信息的 YAML 对象
     * @return 返回数据迁移状态
     */
    public DataTransformationStatus playerGuaranteedJsonToMongo(Yaml yaml) {
        // 检查是否开启转换
        if (!yaml.getBoolean("TRANSFORMATION-JSON-TO-MONGO")) {
            return DataTransformationStatus.TurnOff;
        }

        // 检查 MongoDB 是否已连接
        if (getConnectStatus() != ConnectStatus.Connected) {
            QuickUtils.sendConsoleMessage("&c您开启了数据迁移选项，但 Mongo 数据库并没有成功连接，请检查配置文件，服务器即将关闭。");
            return DataTransformationStatus.Failed;
        }

        // 获取文件路径和文件名列表
        String logsPath = Main.getLogsPath();
        String guaranteedPath = Main.getGuaranteedPath();

        ConcurrentLinkedQueue<String> logsFileNames = ConfigManager.getAllFileNames(logsPath);
        ConcurrentLinkedQueue<String> guaranteedFileNames = ConfigManager.getAllFileNames(guaranteedPath);

        // 检查是否有需要转换的文件
        if (logsFileNames.isEmpty() && guaranteedFileNames.isEmpty()) {
            QuickUtils.sendConsoleMessage("&c未发现需要进行迁移的 JSON 数据，结束此次迁移，服务器即将关闭。");
            return DataTransformationStatus.Failed;
        }

        // 迁移 JSON 文件
        int jsonKeySetAmount = 0;

        if (!guaranteedFileNames.isEmpty()) {
            for (String guaranteedFileName : guaranteedFileNames) {
                Json json = ConfigManager.createJson(guaranteedFileName, guaranteedPath, true, false);
                Set<String> jsonKeySet = json.keySet();
                jsonKeySetAmount += jsonKeySet.size();
                for (String key : jsonKeySet) {
                    update(guaranteedFileName.split(ConstantsUtils.JSON_SUFFIX)[0], key, json.get(key), DatabaseCollectionType.PlayerGuaranteed);
                }
            }
        } else {
            QuickUtils.sendConsoleMessage("&c未发现需要进行迁移的玩家许愿 JSON 数据，跳过玩家许愿数据迁移。");
        }

        if (!logsFileNames.isEmpty()) {
            for (String logsFileName : logsFileNames) {
                Json json = ConfigManager.createJson(logsFileName, logsPath, true, false);
                Set<String> jsonKeySet = json.keySet();
                jsonKeySetAmount += jsonKeySet.size();
                for (String key : jsonKeySet) {
                    update(logsFileName.split(ConstantsUtils.JSON_SUFFIX)[0], key, json.get(key), DatabaseCollectionType.PlayerLogs);
                }
            }
        } else {
            QuickUtils.sendConsoleMessage("&c未发现需要进行迁移的玩家许愿日志 JSON 数据，跳过玩家许愿日志数据迁移。");
        }

        QuickUtils.sendConsoleMessage("&a已成功迁移 JSON 数据至 MongoDB 数据库，此次迁移总文件数: &e" + (guaranteedFileNames.size() + logsFileNames.size()) + "&a，迁移数据数: &e" + jsonKeySetAmount + "&a，即将关闭服务器，已迁移的 JSON 不会被删除，请手动关闭迁移选项!");

        return DataTransformationStatus.Completed;
    }
}
