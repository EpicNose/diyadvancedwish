package me.twomillions.plugin.advancedwish.managers.databases;

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
import me.twomillions.plugin.advancedwish.Main;
import me.twomillions.plugin.advancedwish.enums.mongo.*;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.QuickUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MongoDB 操作类。
 *
 * @author 2000000
 * @date 2023/1/8 21:23
 */
public class MongoManager {
    @Getter @Setter private volatile static MongoClient mongoClient;
    @Getter @Setter private volatile static DBCollection dbCollection;
    @Getter @Setter private volatile static MongoDatabase mongoDatabase;
    @Getter @Setter private volatile static String mongoClientUrlString;
    @Getter @Setter private volatile static MongoClientURI mongoClientUrl;

    @Getter @Setter private volatile static MongoAuthState mongoAuthState;
    @Getter @Setter private volatile static MongoConnectState mongoConnectState;
    @Getter @Setter private volatile static MongoCustomUrlState mongoCustomUrlState;

    /**
     * 设置 MongoDB 数据库连接。
     *
     * @param yaml 配置文件
     * @return MongoDB 连接状态
     */
    public static MongoConnectState setupMongo(Yaml yaml) {
        // 检查是否启用MongoDB
        if (!yaml.getBoolean("MONGO.ENABLE")) {
            setMongoConnectState(MongoConnectState.TurnOff);
            return getMongoConnectState();
        }

        // 设置MongoDB登录信息
        String mongoIP = yaml.getString("MONGO.IP");
        String mongoPort = yaml.getString("MONGO.PORT");
        String mongoUser = yaml.getString("MONGO.AUTH.USER");
        String mongoPassword = yaml.getString("MONGO.AUTH.PASSWORD");

        // 检查是否使用自定义的MongoDB连接URL
        setMongoCustomUrlState("".equals(yaml.getString("MONGO.CUSTOM-URL")) ? MongoCustomUrlState.TurnOff : MongoCustomUrlState.TurnOn);

        // 根据是否使用自定义URL设置连接URL
        if (getMongoCustomUrlState() == MongoCustomUrlState.TurnOn) {
            setMongoClientUrlString(yaml.getString("MONGO.CUSTOM-URL"));
        } else {
            if ("".equals(mongoUser) || "".equals(mongoPassword)) {
                setMongoAuthState(MongoAuthState.TurnOff);
                setMongoClientUrlString("mongodb://" + mongoIP + ":" + mongoPort + "/AdvancedWish");
            } else {
                setMongoAuthState(MongoAuthState.UsingAuth);
                setMongoClientUrlString("mongodb://" + mongoUser + ":" + mongoPassword + "@" + mongoIP + ":" + mongoPort + "/AdvancedWish");

                QuickUtils.sendConsoleMessage("&aAdvanced Wish 检查到Mongo开启身份验证，已设置身份验证信息!");
            }
        }

        // 检查MongoDB连接状态
        try {
            setMongoClientUrl(new MongoClientURI(getMongoClientUrlString()));
            setMongoClient(new MongoClient(getMongoClientUrl()));
            setMongoDatabase(getMongoClient().getDatabase("AdvancedWish"));

            QuickUtils.sendConsoleMessage("&aAdvanced Wish 已成功建立与MongoDB的连接!");

            setMongoConnectState(MongoConnectState.Connected);
        } catch (Exception exception) {
            QuickUtils.sendConsoleMessage("&c您打开了MongoDB数据库选项，但Advanced Wish未能正确连接到MongoDB，请检查MongoDB服务状态，即将关闭服务器!");

            setMongoConnectState(MongoConnectState.CannotConnect);

            Bukkit.shutdown();
        }

        return getMongoConnectState();
    }

    /**
     * 从 Mongo 数据库中获取指定玩家的数据，如果找不到则插入一个新的值。
     *
     * @param player 玩家实例
     * @param foundKey 要查询的键
     * @param value 找不到键值时使用的默认值
     * @param mongoCollections 数据库集合枚举
     * @return 找到的键值，或默认值
     */
    public static Object getOrDefault(Player player, String foundKey, Object value, MongoCollections mongoCollections) {
        String playerUUID = player.getUniqueId().toString();

        MongoCollection<Document> playerCollection = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = playerCollection.find(new Document("uuid", playerUUID)).first();

        if (playerDocument == null) {
            playerDocument = new Document("uuid", playerUUID);
            playerDocument.append(foundKey, value);
            playerCollection.insertOne(playerDocument);
            return value;
        }

        Object foundValue = playerDocument.get(foundKey);

        if (foundValue == null) {
            playerCollection.updateOne(playerDocument, new Document("$set", new Document(foundKey, value)));
            return value;
        }

        return foundValue;
    }

    /**
     * 根据给定的 uuid、key 和默认值获取对应的值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 查询的 uuid
     * @param key 查询的 key
     * @param defaultValue 查询的默认值
     * @param mongoCollections 查询的 MongoDB 集合
     * @return 对应的值
     */
    public static Object getOrDefault(String uuid, String key, Object defaultValue, MongoCollections mongoCollections) {
        MongoCollection<Document> collection = getMongoDatabase().getCollection(mongoCollections.toString());

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
     * 根据给定的 Player、key 和默认值获取对应的 List 值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param player 查询的 Player
     * @param key 查询的 key
     * @param defaultValue 查询的默认值
     * @param mongoCollections 查询的 MongoDB 集合
     * @return 对应的 List 值
     */
    public static ConcurrentLinkedQueue<String> getOrDefaultList(Player player, String key, ConcurrentLinkedQueue<String> defaultValue, MongoCollections mongoCollections) {
        String uuid = player.getUniqueId().toString();
        MongoCollection<Document> collection = getMongoDatabase().getCollection(mongoCollections.toString());

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
     * 根据给定的 UUID、key 和默认值获取对应的 List 值，若未找到则插入默认值并返回。若找到的值为 null，则更新为默认值并返回。
     *
     * @param uuid 查询的 UUID
     * @param key 查询的 key
     * @param defaultValue 查询的默认值
     * @param mongoCollections 查询的 MongoDB 集合
     * @return 对应的 List 值
     */
    public static ConcurrentLinkedQueue<String> getOrDefaultList(String uuid, String key, ConcurrentLinkedQueue<String> defaultValue, MongoCollections mongoCollections) {
        MongoCollection<Document> collection = getMongoDatabase().getCollection(mongoCollections.toString());

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
     * @param player 玩家
     * @param key 数据的键
     * @param value 数据的值
     * @param collection 数据存储的集合
     */
    public static void update(Player player, String key, Object value, MongoCollections collection) {
        MongoCollection<Document> mongoCollection = getMongoDatabase().getCollection(collection.toString());
        Document filter = new Document("uuid", player.getUniqueId().toString());
        Document update = new Document("$set", new Document(key, value));
        UpdateOptions options = new UpdateOptions().upsert(true);
        mongoCollection.updateOne(filter, update, options);
    }

    /**
     * 更新玩家数据。
     *
     * @param uuid 玩家的 UUID
     * @param key 数据的键
     * @param value 数据的值
     * @param collection 数据存储的集合
     */
    public static void update(String uuid, String key, Object value, MongoCollections collection) {
        MongoCollection<Document> mongoCollection = getMongoDatabase().getCollection(collection.toString());
        Document filter = new Document("uuid", uuid);
        Document update = new Document("$set", new Document(key, value));
        UpdateOptions options = new UpdateOptions().upsert(true);
        mongoCollection.updateOne(filter, update, options);
    }

    /**
     * 添加玩家许愿日志。
     *
     * @param player 玩家
     * @param logString 许愿日志
     */
    public static void addPlayerWishLog(Player player, String logString) {
        ConcurrentLinkedQueue<String> logs = getOrDefaultList(player, "logs", new ConcurrentLinkedQueue<>(), MongoCollections.PlayerLogs);
        logs.add(logString);
        update(player, "logs", logs, MongoCollections.PlayerLogs);
    }

    /**
     * 添加玩家许愿日志。
     *
     * @param uuid 玩家 UUID 字符串
     * @param logString 许愿日志
     */
    public static void addPlayerWishLog(String uuid, String logString) {
        ConcurrentLinkedQueue<String> logs = getOrDefaultList(uuid, "logs", new ConcurrentLinkedQueue<>(), MongoCollections.PlayerLogs);
        logs.add(logString);
        update(uuid, "logs", logs, MongoCollections.PlayerLogs);
    }

    /**
     * 获取玩家许愿日志。
     *
     * @param player 玩家
     * @param findMin 要查询的日志的最小编号
     * @param findMax 要查询的日志的最大编号
     * @return 返回查询出来的日志列表
     */
    public static ConcurrentLinkedQueue<String> getPlayerWishLog(Player player, int findMin, int findMax) {
        ConcurrentLinkedQueue<String> logs = getOrDefaultList(player, "logs", new ConcurrentLinkedQueue<>(), MongoCollections.PlayerLogs);

        return getLogsInRange(logs, findMin, findMax);
    }

    /**
     * 获取指定玩家的许愿日志。
     *
     * @param uuid 玩家的 UUID
     * @param findMin 要查询的日志的最小编号
     * @param findMax 要查询的日志的最大编号
     * @return 返回查询出来的日志列表
     */
    public static ConcurrentLinkedQueue<String> getPlayerWishLog(String uuid, int findMin, int findMax) {
        ConcurrentLinkedQueue<String> logs = getOrDefaultList(uuid, "logs", new ConcurrentLinkedQueue<>(), MongoCollections.PlayerLogs);

        return getLogsInRange(logs, findMin, findMax);
    }

    /**
     * 获取玩家所有日志条目数。
     *
     * @param player 玩家
     * @return 返回日志条目数
     */
    public static int getWishLogsSize(Player player) {
        return getOrDefaultList(player, "logs", new ConcurrentLinkedQueue<>(), MongoCollections.PlayerLogs).size();
    }

    /**
     * 获取指定玩家的所有日志条目数。
     *
     * @param uuid 玩家的 UUID
     * @return 返回日志条目数
     */
    public static int getWishLogsSize(String uuid) {
        return getOrDefaultList(uuid, "logs", new ConcurrentLinkedQueue<>(), MongoCollections.PlayerLogs).size();
    }

    /**
     * 获取给定列表的指定范围内的子列表。
     *
     * @param logs 给定的日志列表
     * @param min 子列表的最小索引（从1开始）
     * @param max 子列表的最大索引
     * @return 给定列表的指定范围内的子列表
     */
    public static ConcurrentLinkedQueue<String> getLogsInRange(ConcurrentLinkedQueue<String> logs, int min, int max) {
        ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<>();

        // 使用 Iterator 来遍历队列实现线程安全
        Iterator<String> iterator = logs.iterator();

        int i = 1;
        while (iterator.hasNext() && i <= max) {
            if (i >= min) {
                result.add(iterator.next());
            }

            i++;
        }

        return result;
    }


    /**
     * 将 JSON 转换为 MongoDB 数据。
     *
     * @param yaml 包含转换信息的 YAML 对象
     * @return 返回数据迁移状态
     */
    public static JsonTransformationMongoState playerGuaranteedJsonToMongo(Yaml yaml) {
        // 检查是否开启转换
        if (!yaml.getBoolean("TRANSFORMATION-JSON-TO-MONGO")) {
            return JsonTransformationMongoState.TurnOff;
        }

        // 检查 MongoDB 是否已连接
        if (MongoManager.getMongoConnectState() != MongoConnectState.Connected) {
            QuickUtils.sendConsoleMessage("&c您开启了数据迁移选项，但 Mongo 数据库并没有成功连接，请检查配置文件，服务器即将关闭。");
            return JsonTransformationMongoState.Failed;
        }

        // 获取文件路径和文件名列表
        String logsPath = Main.getLogsPath();
        String guaranteedPath = Main.getGuaranteedPath();

        ConcurrentLinkedQueue<String> logsFileNames = ConfigManager.getAllFileNames(logsPath);
        ConcurrentLinkedQueue<String> guaranteedFileNames = ConfigManager.getAllFileNames(guaranteedPath);

        // 检查是否有需要转换的文件
        if (logsFileNames.isEmpty() && guaranteedFileNames.isEmpty()) {
            QuickUtils.sendConsoleMessage("&c未发现需要进行迁移的 JSON 数据，结束此次迁移，服务器即将关闭。");
            return JsonTransformationMongoState.Failed;
        }

        // 迁移 JSON 文件
        int jsonKeySetAmount = 0;

        if (!guaranteedFileNames.isEmpty()) {
            for (String guaranteedFileName : guaranteedFileNames) {
                Json json = ConfigManager.createJson(guaranteedFileName, guaranteedPath, true, false);
                Set<String> jsonKeySet = json.keySet();
                jsonKeySetAmount += jsonKeySet.size();
                for (String key : jsonKeySet) {
                    update(guaranteedFileName.split(".json")[0], key, json.get(key), MongoCollections.PlayerGuaranteed);
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
                    update(logsFileName.split(".json")[0], key, json.get(key), MongoCollections.PlayerLogs);
                }
            }
        } else {
            QuickUtils.sendConsoleMessage("&c未发现需要进行迁移的玩家许愿日志 JSON 数据，跳过玩家许愿日志数据迁移。");
        }

        QuickUtils.sendConsoleMessage("&a已成功迁移 JSON 数据至 MongoDB 数据库，此次迁移总文件数: &e" + (guaranteedFileNames.size() + logsFileNames.size()) + "&a，迁移数据数: &e" + jsonKeySetAmount + "&a，即将关闭服务器，已迁移的 JSON 不会被删除，请手动关闭迁移选项!");

        return JsonTransformationMongoState.Completed;
    }
}
