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
import me.twomillions.plugin.advancedwish.enums.mongo.JsonTransformationMongoState;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoAuthState;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoCollections;
import me.twomillions.plugin.advancedwish.enums.mongo.MongoConnectState;
import me.twomillions.plugin.advancedwish.main;
import me.twomillions.plugin.advancedwish.managers.ConfigManager;
import me.twomillions.plugin.advancedwish.utils.CC;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * author:     2000000
 * project:    AdvancedWish
 * package:        me.twomillions.plugin.advancedwish.databases.manager
 * className:      MongoManager
 * date:    2023/1/8 21:23
 */
public class MongoManager {
    @Getter @Setter private volatile static MongoClient mongoClient;
    @Getter @Setter private volatile static DBCollection dbCollection;
    @Getter @Setter private volatile static MongoDatabase mongoDatabase;
    @Getter @Setter private volatile static String mongoClientUrlString;
    @Getter @Setter private volatile static MongoClientURI mongoClientUrl;

    @Getter @Setter private volatile static MongoAuthState mongoAuthState;
    @Getter @Setter private volatile static MongoConnectState mongoConnectState;

    // 设置 Mongo
    public static MongoConnectState setupMongo(Yaml yaml) {
        // Mongo 开启检查
        if (!yaml.getBoolean("USE-MONGO")) {
            setMongoConnectState(MongoConnectState.TurnOff);
            return getMongoConnectState();
        }

        // Mongo 登陆设置
        String mongoIP = yaml.getString("MONGO.IP");
        String mongoPort = yaml.getString("MONGO.PORT");
        String mongoUser = yaml.getString("MONGO.AUTH.USER");
        String mongoPassword = yaml.getString("MONGO.AUTH.PASSWORD");

        if (mongoUser.equals("") || mongoPassword.equals("")) {
            setMongoAuthState(MongoAuthState.TurnOff);
            setMongoClientUrlString("mongodb://" + mongoIP + ":" + mongoPort);
        } else {
            setMongoAuthState(MongoAuthState.UsingAuth);
            setMongoClientUrlString("mongodb://" + mongoUser + ":" + mongoPassword + "@" + mongoIP + ":" + mongoPort);

            CC.sendConsoleMessage("&aAdvanced Wish 检查到 Mongo 开启身份验证，已设置身份验证信息!");
        }

        // Mongo 连接状态检查
        try {
            setMongoClientUrl(new MongoClientURI(getMongoClientUrlString()));
            setMongoClient(new MongoClient(getMongoClientUrl()));
            setMongoDatabase(getMongoClient().getDatabase("AdvancedWish"));

            CC.sendConsoleMessage("&aAdvanced Wish 已成功建立与 Mongo 的连接!");

            setMongoConnectState(MongoConnectState.Connected);
        } catch (Exception exception) {
            CC.sendConsoleMessage("&c您打开了 Mongo 数据库选项，但是 Advanced Wish 未与 Mongo 数据库正确连接，请检查 Mongo 服务状态，即将关闭服务器!");

            setMongoConnectState(MongoConnectState.CannotConnect);

            Bukkit.shutdown();
        }

        return getMongoConnectState();
    }

    // 查询数据 - 若为空则设置并返回为 value - Object
    public static Object getOrDefault(Player player, String foundKey, Object value, MongoCollections mongoCollections) {
        String playerUUID = player.getUniqueId().toString();

        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = new Document("uuid", playerUUID);
        Document foundDocument = playerGuaranteed.find(playerDocument).first();

        // 如果没有找到则插入并返回 value
        if (foundDocument == null) { playerDocument.append(foundKey, value); playerGuaranteed.insertOne(playerDocument); return value.toString(); }

        // 如果获取的值为 null，则更新并且返回 value
        return foundDocument.getOrDefault(foundKey, value);
    }

    // 查询数据 - 若为空则设置并返回为 value - Object - 多态 UUID
    public static Object getOrDefault(String uuid, String foundKey, Object value, MongoCollections mongoCollections) {
        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = new Document("uuid", uuid);
        Document foundDocument = playerGuaranteed.find(playerDocument).first();

        // 如果没有找到则插入并返回 value
        if (foundDocument == null) { playerDocument.append(foundKey, value); playerGuaranteed.insertOne(playerDocument); return value.toString(); }

        // 如果获取的值为 null，则更新并且返回 value
        return foundDocument.getOrDefault(foundKey, value);
    }

    // 查询数据 - 若为空则设置并返回为 value - List
    public static List<String> getOrDefaultList(Player player, String foundKey, List<String> value, MongoCollections mongoCollections) {
        String playerUUID = player.getUniqueId().toString();

        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = new Document("uuid", playerUUID);
        Document foundDocument = playerGuaranteed.find(playerDocument).first();

        // 如果没有找到则插入并返回 value
        if (foundDocument == null) { playerDocument.append(foundKey, value); playerGuaranteed.insertOne(playerDocument); return value; }

        // 如果获取的值为 null，则更新并且返回 value
        return foundDocument.getList(foundKey, String.class, value);
    }

    // 查询数据 - 若为空则设置并返回为 value - List - 多态 UUID
    public static List<String> getOrDefaultList(String uuid, String foundKey, List<String> value, MongoCollections mongoCollections) {
        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = new Document("uuid", uuid);
        Document foundDocument = playerGuaranteed.find(playerDocument).first();

        // 如果没有找到则插入并返回 value
        if (foundDocument == null) { playerDocument.append(foundKey, value); playerGuaranteed.insertOne(playerDocument); return value; }

        // 如果获取的值为 null，则更新并且返回 value
        return foundDocument.getList(foundKey, String.class, value);
    }

    // 更新数据
    public static void update(Player player, String foundKey, Object value, MongoCollections mongoCollections) {
        String playerUUID = player.getUniqueId().toString();

        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = new Document("uuid", playerUUID);

        Document keyDocument = new Document(foundKey, value);
        Document updateDocument = new Document("$set", keyDocument);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);

        playerGuaranteed.updateOne(playerDocument, updateDocument, updateOptions);
    }

    // 更新数据 - 多态 UUID
    public static void update(String uuid, String foundKey, Object value, MongoCollections mongoCollections) {
        MongoCollection<Document> playerGuaranteed = getMongoDatabase().getCollection(mongoCollections.toString());

        Document playerDocument = new Document("uuid", uuid);

        Document keyDocument = new Document(foundKey, value);
        Document updateDocument = new Document("$set", keyDocument);
        UpdateOptions updateOptions = new UpdateOptions().upsert(true);

        playerGuaranteed.updateOne(playerDocument, updateDocument, updateOptions);
    }

    // 添加玩家许愿日志
    public static void addPlayerWishLog(Player player, String logString) {
        List<String> logs = getOrDefaultList(player, "logs", new ArrayList<>(), MongoCollections.PlayerLogs); logs.add(logString);
        update(player, "logs", logs, MongoCollections.PlayerLogs);
    }

    // 添加玩家许愿日志 - 多态 UUID
    public static void addPlayerWishLog(String uuid, String logString) {
        List<String> logs = getOrDefaultList(uuid, "logs", new ArrayList<>(), MongoCollections.PlayerLogs); logs.add(logString);
        update(uuid, "logs", logs, MongoCollections.PlayerLogs);
    }

    // 获取玩家许愿日志
    public static List<String> getPlayerWishLog(Player player, int findMin, int findMax) {
        List<String> returnLogs = new ArrayList<>();
        List<String> getLogs = getOrDefaultList(player, "logs", new ArrayList<>(), MongoCollections.PlayerLogs);

        // 从 0 开始，所以 +1
        int query = 1;

        for (String log : getLogs) {
            if (query > findMax) break;
            if (query < findMin) { query ++; continue; }

            returnLogs.add(log); query ++;
        }

        return returnLogs;
    }

    // 获取玩家许愿日志 - 多态 UUID
    public static List<String> getPlayerWishLog(String uuid, int findMin, int findMax) {
        List<String> returnLogs = new ArrayList<>();
        List<String> getLogs = getOrDefaultList(uuid, "logs", new ArrayList<>(), MongoCollections.PlayerLogs);

        // 从 0 开始，所以 +1
        int query = 1;

        for (String log : getLogs) {
            if (query > findMax) break;
            if (query < findMin) { query ++; continue; }

            returnLogs.add(log); query ++;
        }

        return returnLogs;
    }

    // 获取玩家所有日志条目数
    public static int getWishLogsSize(Player player) {
        return getOrDefaultList(player, "logs", new ArrayList<>(), MongoCollections.PlayerLogs).size();
    }

    // 获取玩家所有日志条目数
    public static int getWishLogsSize(String uuid) {
        return getOrDefaultList(uuid, "logs", new ArrayList<>(), MongoCollections.PlayerLogs).size();
    }

    // Json 转换为 Mongo 数据
    public static JsonTransformationMongoState playerGuaranteedJsonToMongo(Yaml yaml) {
        if (!yaml.getBoolean("TRANSFORMATION-JSON-TO-MONGO")) return JsonTransformationMongoState.TurnOff;
        if (MongoManager.getMongoConnectState() != MongoConnectState.Connected) { CC.sendConsoleMessage("&c您开启了数据迁移选项，但 Mongo 数据库并没有成功连接，请检查配置文件，服务器即将关闭。"); return JsonTransformationMongoState.Failed; }

        String logsPath = main.getLogsPath();
        String guaranteedPath = main.getGuaranteedPath();

        List<String> logsFileNames = ConfigManager.getAllFileName(logsPath);
        List<String> guaranteedFileNames = ConfigManager.getAllFileName(guaranteedPath);

        boolean notTransformationLogs = logsFileNames.size() <= 0;
        boolean notTransformationGuaranteed = guaranteedFileNames.size() <= 0;

        // 若 Json 文件个数为 0 则返回失败
        if (notTransformationLogs && notTransformationGuaranteed) { CC.sendConsoleMessage("&c未发现需要进行迁移的 Json 数据，结束此次迁移，服务器即将关闭。"); return JsonTransformationMongoState.Failed; }

        // 获取所有 key，遍历进行更新
        int jsonKeySetAmount = 0;

        if (!notTransformationGuaranteed) {
            for (String guaranteedFileName : guaranteedFileNames) {
                Json json = ConfigManager.createJsonConfig(guaranteedFileName, guaranteedPath, true, false);

                Set<String> jsonKeySet = json.keySet();
                jsonKeySetAmount = jsonKeySetAmount + jsonKeySet.size();

                for (String key : jsonKeySet)
                    update(guaranteedFileName.split(".json")[0], key, json.get(key), MongoCollections.PlayerGuaranteed);
            }
        } else CC.sendConsoleMessage("&c未发现需要进行迁移的玩家许愿 Json 数据，跳过玩家许愿数据迁移。");

        if (!notTransformationLogs) {
            for (String logsFileName : logsFileNames) {
                Json json = ConfigManager.createJsonConfig(logsFileName, logsPath, true, false);

                Set<String> jsonKeySet = json.keySet();
                jsonKeySetAmount = jsonKeySetAmount + jsonKeySet.size();

                for (String key : jsonKeySet)
                    update(logsFileName.split(".json")[0], key, json.get(key), MongoCollections.PlayerLogs);
            }
        } else CC.sendConsoleMessage("&c未发现需要进行迁移的玩家许愿日志 Json 数据，跳过玩家许愿日志数据迁移。");

        CC.sendConsoleMessage("&a已成功迁移 Json 数据至 Mongo 数据库，此次迁移总文件数: &e" + guaranteedFileNames.size() + logsFileNames.size() + "&a，迁移数据数: &e" + jsonKeySetAmount + "&a，即将关闭服务器，已迁移的 Json 不会被删除，请手动关闭迁移选项!");

        return JsonTransformationMongoState.Completed;
    }
}
